package com.titammods.hephaestus_tools.tables.menu;

import com.titammods.hephaestus_tools.materials.MaterialId;
import com.titammods.hephaestus_tools.materials.MaterialManager;
import com.titammods.hephaestus_tools.registry.ModMenus;
import com.titammods.hephaestus_tools.table.ToolAssembly;
import com.titammods.hephaestus_tools.table.ToolUpgrades;
import com.titammods.hephaestus_tools.tables.blockentity.ArsenalTableBlockEntity;
import com.titammods.hephaestus_tools.tables.scene.WorkbenchOrigin;
import com.titammods.hephaestus_tools.tables.scene.WorkbenchScene;
import com.titammods.hephaestus_tools.tools.item.ModifiableItem;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import com.titammods.hephaestus_tools.tools.part.ToolPartItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorkbenchMenu extends ArsenalTableMenu {

    public static final int AUTOFILL = 170, REPAIR_FILL = 171, SWAP_BASE = 400, FILL_MOD_BASE = 500;

    private boolean cinematicServerSession;
    @Nullable private WorkbenchOrigin origin;
    private boolean inventoryVisible;
    private boolean inspecting;

    public WorkbenchMenu(int id, Inventory inv, ArsenalTableBlockEntity be) {
        this(id, inv, be, null);
    }

    public WorkbenchMenu(int id, Inventory inv, ArsenalTableBlockEntity be, @Nullable WorkbenchOrigin clientOrigin) {
        super(ModMenus.WORKBENCH.get(), id, inv, be);
        if (clientOrigin != null) origin = clientOrigin;
        beginCinematicServerSession(inv.player);
    }

    private final class InventorySlot extends Slot {
        private final boolean alwaysShown;

        InventorySlot(Inventory inv, int index, int x, int y, boolean alwaysShown) {
            super(inv, index, x, y);
            this.alwaysShown = alwaysShown;
        }

        @Override
        public boolean isActive() {
            return !inspecting && (alwaysShown || inventoryVisible);
        }
    }

    @Override
    protected Slot inventorySlot(Inventory inv, int index, int x, int y, boolean hotbar) {
        return new InventorySlot(inv, index, x, y, hotbar);
    }

    @Override
    protected int initialSelection(Player player) {
        if (player.level().isClientSide()) return -1;
        if (opensOnModify()) {
            activeTab = 1;
            return -1;
        }
        return guessSelection();
    }

    private boolean opensOnModify() {
        if (!output().isEmpty()) return false;
        if (!blockEntity.getUpgradeSlot().getStackInSlot(0).isEmpty()) return true;
        ItemStack staged = blockEntity.getInputSlots().getStackInSlot(0);
        if (!staged.isEmpty() && !(staged.getItem() instanceof ToolPartItem)) return true;
        for (int i = 1; i <= EXTRA_SLOTS; i++)
            if (!blockEntity.getInputSlots().getStackInSlot(i).isEmpty()
                    && !(blockEntity.getInputSlots().getStackInSlot(i).getItem() instanceof ToolPartItem)) return true;
        return false;
    }

    @Override
    protected boolean slotsVisible() {
        return !inspecting;
    }

    @Override
    protected boolean inputActive(int part) {
        if (activeTab == 1) return part <= EXTRA_SLOTS;
        return super.inputActive(part);
    }

    @Override
    protected boolean inputAccepts(int part, ItemStack stack) {
        if (activeTab == 1) return part == 0 ? acceptsStaged(stack) : acceptsExtra(stack);
        return accepts(part, stack);
    }

    @Override
    protected int inputLimit(int part) {
        if (activeTab == 0 && part == 0 && isRepair()) return 64;
        if (activeTab == 1) return part == 0 ? 99 : 64;
        return 1;
    }

    public static final int EXTRA_SLOTS = 2;

    public List<ItemStack> pendingExtras() {
        ItemStack tool = tool();
        List<ItemStack> out = new ArrayList<>();
        if (!ToolStack.isInitialized(tool)) return out;
        for (var up : ToolUpgrades.availableFor(tool.getItem())) {
            int lvl = ToolUpgrades.currentLevel(tool, up);
            if (lvl < up.maxLevel()) out.addAll(up.extraCosts(lvl + 1));
        }
        return out;
    }

    public boolean acceptsExtra(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ToolPartItem) return false;
        for (ItemStack extra : pendingExtras()) if (extra.getItem() == stack.getItem()) return true;
        return false;
    }

    public int repairPerUnit(ItemStack tool) {
        return repairPerMaterial(tool, user);
    }

    public int repairUnitsNeeded(ItemStack tool) {
        if (tool.isEmpty() || !ToolStack.isInitialized(tool)) return 0;
        int damage = ToolStack.getCurrentDamage(tool);
        if (damage <= 0) return 0;
        int per = repairPerUnit(tool);
        return (damage + per - 1) / per;
    }

    public int repairUnitsUsed() {
        if (!isRepair() || !isRepairMaterial(input(0))) return 0;
        return Math.min(input(0).getCount(), repairUnitsNeeded(output()));
    }

    private ItemStack repaired(ItemStack tool, int units) {
        ItemStack result = tool.copyWithCount(1);
        int restored = repairPerUnit(result) * units;
        result.setDamageValue(Math.max(0, ToolStack.getCurrentDamage(result) - restored));
        return result;
    }

    @Override
    protected int quickMoveTabEnd() {
        return activeTab == 1 ? 2 + EXTRA_SLOTS : 1;
    }

    public boolean isInspecting() { return inspecting; }

    public void setInspecting(boolean value) { inspecting = value; }

    public boolean isInventoryVisible() { return inventoryVisible; }

    public void setInventoryVisible(boolean visible) { inventoryVisible = visible; }

    @Nullable
    public WorkbenchOrigin getOrigin() { return origin; }

    private void beginCinematicServerSession(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || blockEntity.getLevel() == null) return;
        origin = WorkbenchOrigin.of(serverPlayer);
        cinematicServerSession = true;
        MinecraftServer server = serverPlayer.level().getServer();
        if (server == null) return;
        server.schedule(new TickTask(server.getTickCount() + 1, () -> {
            if (!cinematicServerSession || serverPlayer.containerMenu != this || !serverPlayer.isAlive()) return;
            Vec3 anchor = WorkbenchScene.playerPosition(blockEntity.getBlockPos(), blockEntity.getBlockState());
            float yaw = WorkbenchScene.playerYaw(blockEntity.getBlockState());
            serverPlayer.teleportTo(serverPlayer.level(), anchor.x, anchor.y, anchor.z, Set.of(), yaw, WorkbenchScene.PLAYER_PITCH, false);
            serverPlayer.setYHeadRot(yaw);
            serverPlayer.setYBodyRot(yaw);
        }));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!cinematicServerSession || !(player instanceof ServerPlayer serverPlayer)) return;
        cinematicServerSession = false;
        ServerLevel level = serverPlayer.level();
        if (origin == null || !serverPlayer.isAlive() || level != blockEntity.getLevel()) return;
        serverPlayer.teleportTo(level, origin.x(), origin.y(), origin.z(), Set.of(), origin.yaw(), origin.pitch(), false);
        serverPlayer.setYHeadRot(origin.yaw());
        serverPlayer.setYBodyRot(origin.yaw());
    }

    private int guessSelection() {
        boolean any = false;
        for (int i = 0; i < 4; i++) any |= !input(i).isEmpty();
        if (!any) return -1;
        for (int a = 0; a < ToolAssembly.REGISTRY.size(); a++) {
            List<Item> p = ToolAssembly.REGISTRY.get(a).parts();
            boolean fits = true;
            for (int i = 0; i < 4 && fits; i++)
                if (!input(i).isEmpty()) fits = i < p.size() && input(i).is(p.get(i));
            if (fits) return a;
        }
        return 0;
    }

    @Override
    public List<Item> parts() {
        return isRepair() || selectedTool < 0 ? List.of() : assembly().parts();
    }

    private boolean acceptsStaged(ItemStack stack) {
        ItemStack tool = tool();
        if (stack.isEmpty() || !ToolStack.isInitialized(tool)) return false;
        if (stack.getItem() instanceof ToolPartItem tp) return partIndexFor(tool, stack) >= 0 && !tp.getMaterial(stack).isEmpty();
        for (var up : ToolUpgrades.availableFor(tool.getItem()))
            if (up.costItem() == stack.getItem()) return true;
        return false;
    }

    @Override
    protected boolean accepts(int part, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (isRepair()) {
            if (part != 0 || !ToolStack.isInitialized(tool())) return false;
            if (stack.getItem() instanceof ToolPartItem) return canSwap(tool(), stack, partIndexFor(tool(), stack));
            return isRepairMaterial(stack);
        }
        return super.accepts(part, stack);
    }

    @Override
    public ItemStack preview() {
        if (activeTab != 0) return ItemStack.EMPTY;
        if (isRepair()) {
            if (!ToolStack.isInitialized(tool()) || !(tool().getItem() instanceof ModifiableItem)
                    || ToolStack.getCurrentDamage(tool()) <= 0 || !isRepairMaterial(input(0))) return ItemStack.EMPTY;
            for (int i = 1; i < 4; i++) if (!input(i).isEmpty()) return ItemStack.EMPTY;
            int units = repairUnitsUsed();
            return units <= 0 ? ItemStack.EMPTY : repaired(tool(), units);
        }
        if (selectedTool < 0) return ItemStack.EMPTY;
        return super.preview();
    }

    public static ToolAssembly assemblyOf(Item tool) {
        ToolAssembly.init();
        for (ToolAssembly a : ToolAssembly.REGISTRY) if (a.result() == tool) return a;
        return null;
    }

    public static int partIndexFor(ItemStack tool, ItemStack part) {
        if (tool.isEmpty() || part.isEmpty()) return -1;
        ToolAssembly a = assemblyOf(tool.getItem());
        return a == null ? -1 : a.parts().indexOf(part.getItem());
    }

    public static boolean canSwap(ItemStack tool, ItemStack held, int index) {
        if (!ToolStack.isInitialized(tool) || held.isEmpty() || !(held.getItem() instanceof ToolPartItem tp)) return false;
        ToolAssembly a = assemblyOf(tool.getItem());
        if (a == null || index < 0 || index >= a.parts().size() || a.parts().get(index) != held.getItem()) return false;
        MaterialId nm = tp.getMaterial(held);
        if (nm.isEmpty() || MaterialManager.getInstance().getMaterial(nm) == null) return false;
        var mats = ToolStack.getMaterials(tool);
        return index < mats.size() && !mats.get(index).equals(nm);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.level().isClientSide() || !stillValid(player)) return false;
        if ((activeTab == 1 || (activeTab == 0 && isRepair())) && id >= SWAP_BASE && id < SWAP_BASE + 4)
            return swapPart(player, id - SWAP_BASE);
        if (activeTab == 1 && id >= FILL_MOD_BASE && id < FILL_MOD_BASE + 100)
            return fillModifier(player, id - FILL_MOD_BASE);
        if (activeTab == 0) {
            if (id == AUTOFILL) { fillParts(player); return true; }
            if (id == REPAIR_FILL) return fillRepair(player);
            if (id == REPAIR) return attemptRepair();
            if (id == ASSEMBLE) {
                if (isRepair() || !canAssemble()) return false;
                ItemStack result = preview();
                for (int i = 0; i < parts().size(); i++) blockEntity.getInputSlots().extractItem(i, 1, false);
                blockEntity.getOutputSlot().setStackInSlot(0, result);
                blockEntity.setChanged();
                broadcastChanges();
                return true;
            }
        }
        return super.clickMenuButton(player, id);
    }

    private boolean swapPart(Player player, int index) {
        ItemStack tool = tool(), held = input(0);
        if (!canSwap(tool, held, index)) return false;
        ToolAssembly a = assemblyOf(tool.getItem());
        MaterialId old = ToolStack.getMaterial(tool, index);
        MaterialId nm = ((ToolPartItem) held.getItem()).getMaterial(held);
        List<MaterialId> mats = new ArrayList<>(ToolStack.getMaterials(tool));
        mats.set(index, nm);
        ItemStack result = tool.copyWithCount(1);
        int damage = ToolStack.getCurrentDamage(result);
        boolean broken = ToolStack.isBroken(result);
        ToolStack.setMaterials(result, mats);
        ToolStack.recalculate(result);
        int durability = ToolStack.getDurability(result);
        result.setDamageValue(broken ? damage : Math.min(damage, Math.max(0, durability - 1)));
        ItemStack rest = held.copy();
        rest.shrink(1);
        blockEntity.getInputSlots().setStackInSlot(0, rest);
        if (old != null && !old.isEmpty() && a.parts().get(index) instanceof ToolPartItem tp) {
            ItemStack back = tp.withMaterial(old);
            if (activeTab == 0 && rest.isEmpty()) blockEntity.getInputSlots().setStackInSlot(0, back);
            else if (!player.getInventory().add(back)) player.drop(back, false);
        }
        if (activeTab == 0) blockEntity.getOutputSlot().setStackInSlot(0, result);
        else blockEntity.getUpgradeSlot().setStackInSlot(0, result);
        blockEntity.setChanged();
        broadcastChanges();
        return true;
    }

    private boolean fillModifier(Player player, int index) {
        var ups = ToolUpgrades.availableFor(tool().getItem());
        if (index < 0 || index >= ups.size() || !ToolStack.isInitialized(tool())) return false;
        var up = ups.get(index);
        int lvl = ToolUpgrades.currentLevel(tool(), up);
        if (lvl >= up.maxLevel()) return false;
        ItemStack bench = input(0);
        if (!bench.isEmpty() && bench.getItem() != up.costItem()) { returnSlot(player, 1); bench = input(0); }
        int need = Math.min(99, up.costFor(lvl + 1)) - (bench.isEmpty() ? 0 : bench.getCount());
        var inv = player.getInventory();
        int taken = 0;
        for (int s = 0; s < inv.getContainerSize() && taken < need; s++) {
            ItemStack st = inv.getItem(s);
            if (st.isEmpty() || st.getItem() != up.costItem()) continue;
            taken += st.split(Math.min(need - taken, st.getCount())).getCount();
        }
        if (taken > 0) {
            ItemStack put = new ItemStack(up.costItem(), (bench.isEmpty() ? 0 : bench.getCount()) + taken);
            blockEntity.getInputSlots().setStackInSlot(0, put);
        }
        boolean extras = fillExtras(player, up.extraCosts(lvl + 1));
        if (taken == 0 && !extras) return false;
        inv.setChanged();
        blockEntity.setChanged();
        broadcastChanges();
        return true;
    }

    private boolean fillExtras(Player player, List<ItemStack> extras) {
        var inv = player.getInventory();
        boolean changed = false;
        for (ItemStack extra : extras) {
            int slot = -1;
            for (int i = 1; i <= EXTRA_SLOTS && slot < 0; i++)
                if (input(i).is(extra.getItem())) slot = i;
            for (int i = 1; i <= EXTRA_SLOTS && slot < 0; i++)
                if (input(i).isEmpty()) slot = i;
            if (slot < 0) continue;
            ItemStack bench = input(slot);
            int have = bench.isEmpty() ? 0 : bench.getCount();
            int need = Math.min(64, extra.getCount()) - have;
            if (need <= 0) continue;
            int taken = 0;
            for (int s = 0; s < inv.getContainerSize() && taken < need; s++) {
                ItemStack st = inv.getItem(s);
                if (st.isEmpty() || st.getItem() != extra.getItem()) continue;
                taken += st.split(Math.min(need - taken, st.getCount())).getCount();
            }
            if (taken == 0) continue;
            blockEntity.getInputSlots().setStackInSlot(slot, new ItemStack(extra.getItem(), have + taken));
            changed = true;
        }
        return changed;
    }

    private void fillParts(Player player) {
        if (isRepair() || !output().isEmpty()) return;
        var inv = player.getInventory();
        MaterialId prefer = null;
        for (int i = 0; i < parts().size() && i < 4; i++)
            if (!input(i).isEmpty() && input(i).getItem() instanceof ToolPartItem tp) { prefer = tp.getMaterial(input(i)); break; }
        boolean changed = false;
        for (int i = 0; i < parts().size() && i < 4; i++) {
            if (!input(i).isEmpty()) continue;
            int found = -1;
            for (int pass = 0; pass < 2 && found < 0; pass++) {
                for (int s = 0; s < inv.getContainerSize(); s++) {
                    ItemStack st = inv.getItem(s);
                    if (!accepts(i, st)) continue;
                    if (pass == 0 && (prefer == null || !prefer.equals(((ToolPartItem) st.getItem()).getMaterial(st)))) continue;
                    found = s;
                    break;
                }
            }
            if (found >= 0) {
                blockEntity.getInputSlots().setStackInSlot(i, inv.getItem(found).split(1));
                if (prefer == null && input(i).getItem() instanceof ToolPartItem tp) prefer = tp.getMaterial(input(i));
                changed = true;
            }
        }
        if (changed) {
            inv.setChanged();
            blockEntity.setChanged();
            broadcastChanges();
        }
    }

    private boolean attemptRepair() {
        ItemStack current = output();
        if (!isRepair() || current.isEmpty() || ToolStack.getCurrentDamage(current) <= 0) return false;
        int units = repairUnitsUsed();
        if (units <= 0) return false;
        blockEntity.getOutputSlot().setStackInSlot(0, repaired(current, units));
        blockEntity.getInputSlots().extractItem(0, units, false);
        blockEntity.setChanged();
        broadcastChanges();
        return true;
    }

    private boolean fillRepair(Player player) {
        if (!isRepair()) return false;
        int needed = repairUnitsNeeded(output());
        if (needed <= 0) return false;
        ItemStack bench = input(0);
        if (!bench.isEmpty() && !isRepairMaterial(bench)) { returnSlot(player, 1); bench = input(0); }
        int have = bench.isEmpty() ? 0 : bench.getCount();
        if (have >= needed) return true;
        var inv = player.getInventory();
        ItemStack put = bench.copy();
        for (int s = 0; s < inv.getContainerSize() && have < needed; s++) {
            ItemStack st = inv.getItem(s);
            if (st.isEmpty() || !isRepairMaterial(st)) continue;
            if (!put.isEmpty() && !ItemStack.isSameItemSameComponents(put, st)) continue;
            ItemStack taken = st.split(Math.min(needed - have, st.getCount()));
            if (put.isEmpty()) put = taken;
            else put.grow(taken.getCount());
            have = put.getCount();
        }
        if (put.isEmpty() || put.getCount() == bench.getCount()) return false;
        blockEntity.getInputSlots().setStackInSlot(0, put);
        inv.setChanged();
        blockEntity.setChanged();
        broadcastChanges();
        return true;
    }

    @Override
    protected boolean beforeClick(int slotId, int button, ContainerInput type, Player player) {
        if (slotId == 5 && output().isEmpty()) {
            return !getCarried().isEmpty() || (type == ContainerInput.SWAP && (button == 40
                    ? !player.getOffhandItem().isEmpty()
                    : button >= 0 && button < 9 && !player.getInventory().getItem(button).isEmpty()));
        }
        return true;
    }

    @Override
    protected void afterClick(int slotId, int button, ContainerInput type, Player player) {
    }
}