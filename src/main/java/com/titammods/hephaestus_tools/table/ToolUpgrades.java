package com.titammods.hephaestus_tools.table;

import com.titammods.hephaestus_tools.registry.ModItems;
import com.titammods.hephaestus_tools.tables.menu.WorkbenchMenu;
import com.titammods.hephaestus_tools.tools.item.ModifiableItem;
import com.titammods.hephaestus_tools.tools.modifier.ModifierEffects;
import com.titammods.hephaestus_tools.tools.nbt.ToolConstructionData;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ToolUpgrades {

    private ToolUpgrades() {}

    public static final List<ToolUpgrade> REGISTRY = new ArrayList<>();
    public static final int MAX_MODIFIER_SLOTS = 5;
    public static final int[] SLOT_UNLOCK_LEVEL = {0, 0, 10, 20, 30};

    public static int unlockedSlots(ItemStack tool) {
        int lvl = ToolXp.getLevel(tool), n = 0;
        for (int need : SLOT_UNLOCK_LEVEL) if (lvl >= need) n++;
        return Math.min(MAX_MODIFIER_SLOTS, Math.max(n, usedSlots(tool)));
    }

    public static int unlockLevel(int slotIndex) {
        return SLOT_UNLOCK_LEVEL[Math.max(0, Math.min(slotIndex, SLOT_UNLOCK_LEVEL.length - 1))];
    }

    public static boolean slotLocked(ItemStack tool, ToolUpgrade up) {
        return currentLevel(tool, up) == 0 && usedSlots(tool) >= unlockedSlots(tool);
    }

    private static final Map<String, Set<String>> INCOMPATIBLE = Map.of(
            "silk_touch", Set.of("fortune", "looting"),
            "fortune",    Set.of("silk_touch"),
            "looting",    Set.of("silk_touch"));

    private static final Map<Item, Set<ToolRole>> TOOL_ROLES = new HashMap<>();
    private static boolean built = false;

    public static boolean hasIncompatible(ItemStack tool, ToolUpgrade up) {
        Set<String> banned = INCOMPATIBLE.get(up.id().getPath());
        if (banned == null) return false;
        for (ToolConstructionData.ModifierEntry e : ToolStack.getModifiers(tool))
            if (e.level() > 0 && banned.contains(e.id().getPath())) return true;
        return false;
    }

    public static int usedSlots(ItemStack tool) {
        return (int) ToolStack.getModifiers(tool).stream().filter(e -> e.level() > 0)
                .map(ToolConstructionData.ModifierEntry::id).distinct().count();
    }

    private static Identifier icon(String p) {
        return Identifier.fromNamespaceAndPath("hephaestus_tools",
                "textures/gui/arsenal_table/recipe_icons/" + p);
    }

    private static void ensure() {
        if (built) return;
        built = true;

        REGISTRY.add(new ToolUpgrade(ModifierEffects.REINFORCED, "upgrade.hephaestus_tools.reinforced",
                EnumSet.of(ToolRole.MINING, ToolRole.COMBAT, ToolRole.HARVEST), 5, Items.OBSIDIAN, 6, icon("upgrades/reach.png")));
        REGISTRY.add(new ToolUpgrade(ModifierEffects.HASTE, "upgrade.hephaestus_tools.haste",
                EnumSet.of(ToolRole.MINING), 10, Items.REDSTONE_BLOCK, 4, icon("upgrades/attack_speed.png")));
        REGISTRY.add(new ToolUpgrade(ModifierEffects.FORTUNE, "upgrade.hephaestus_tools.fortune",
                EnumSet.of(ToolRole.MINING, ToolRole.HARVEST), 10, Items.DIAMOND, 3, icon("upgrades/critical.png")));
        REGISTRY.add(new ToolUpgrade(ModifierEffects.SHARPNESS, "upgrade.hephaestus_tools.sharpness",
                EnumSet.of(ToolRole.COMBAT), 10, Items.DIAMOND, 2, icon("upgrades/damage.png")));
        REGISTRY.add(new ToolUpgrade(ModifierEffects.LOOTING, "upgrade.hephaestus_tools.looting",
                EnumSet.of(ToolRole.COMBAT), 10, Items.EMERALD, 4, icon("upgrades/critical.png")));
        REGISTRY.add(new ToolUpgrade(ModifierEffects.SILK_TOUCH, "modifier.hephaestus_tools.silk_touch",
                EnumSet.of(ToolRole.MINING, ToolRole.HARVEST), 1, Items.ECHO_SHARD, 6, icon("upgrades/reach.png")));
        REGISTRY.add(new ToolUpgrade(ModifierEffects.FLAME, "modifier.hephaestus_tools.flame",
                EnumSet.of(ToolRole.COMBAT), 1, Items.BLAZE_ROD, 8, icon("upgrades/damage.png")));
        REGISTRY.add(new ToolUpgrade(ModifierEffects.SWEEPING, "modifier.hephaestus_tools.sweeping",
                EnumSet.of(ToolRole.COMBAT), 3, Items.DIAMOND, 2, icon("upgrades/attack_speed.png")));

        role(ModItems.PICKAXE.get(), ToolRole.MINING);
        role(ModItems.EXCAVATOR.get(), ToolRole.MINING);
        role(ModItems.PAXEL.get(), ToolRole.MINING);
        role(ModItems.SLEDGE_HAMMER.get(), ToolRole.MINING);
        role(ModItems.VEIN_HAMMER.get(), ToolRole.MINING);
        role(ModItems.MATTOCK.get(), ToolRole.MINING, ToolRole.HARVEST);
        role(ModItems.HAND_AXE.get(), ToolRole.MINING, ToolRole.COMBAT);
        role(ModItems.BROAD_AXE.get(), ToolRole.MINING, ToolRole.COMBAT);
        role(ModItems.KAMA.get(), ToolRole.HARVEST, ToolRole.COMBAT);
        role(ModItems.SCYTHE.get(), ToolRole.HARVEST, ToolRole.COMBAT);
        role(ModItems.DAGGER.get(), ToolRole.COMBAT);
        role(ModItems.SWORD.get(), ToolRole.COMBAT);
        role(ModItems.CLEAVER.get(), ToolRole.COMBAT);
        role(ModItems.MACE.get(), ToolRole.COMBAT);
        role(ModItems.SPEAR.get(), ToolRole.COMBAT);
        role(ModItems.BOW.get(), ToolRole.COMBAT);
    }

    private static void role(Item tool, ToolRole... roles) {
        TOOL_ROLES.put(tool, EnumSet.copyOf(List.of(roles)));
    }

    public static Set<ToolRole> rolesOf(Item tool) {
        ensure();
        return TOOL_ROLES.getOrDefault(tool, Set.of());
    }

    public static List<ToolUpgrade> availableFor(Item tool) {
        ensure();
        Set<ToolRole> roles = rolesOf(tool);
        List<ToolUpgrade> out = new ArrayList<>();
        if (roles.isEmpty()) return out;
        for (ToolUpgrade u : REGISTRY) {
            for (ToolRole r : u.roles()) {
                if (roles.contains(r)) { out.add(u); break; }
            }
        }
        return out;
    }

    public static Optional<ToolUpgrade> find(Identifier id) {
        ensure();
        return REGISTRY.stream().filter(up -> up.id().equals(id)).findFirst();
    }

    public static int currentLevel(ItemStack tool, ToolUpgrade up) {
        for (ToolConstructionData.ModifierEntry e : ToolStack.getModifiers(tool)) {
            if (e.id().equals(up.id())) return e.level();
        }
        return 0;
    }

    public static int countIn(Player p, Item item) {
        if (p == null) return 0;
        int n = 0;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == item) n += s.getCount();
        }
        if (p.containerMenu instanceof WorkbenchMenu m) {
            ItemStack staged = m.input(0);
            if (!staged.isEmpty() && staged.getItem() == item) n += staged.getCount();
            ItemStack held = m.getCarried();
            if (!held.isEmpty() && held.getItem() == item) n += held.getCount();
        }
        return n;
    }

    public static int inventoryCount(Player p, Item item) {
        if (p == null) return 0;
        int n = 0;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == item) n += s.getCount();
        }
        return n;
    }

    public static int benchCount(Player p, Item item) {
        return stagedCount(p, item) + inventoryCount(p, item);
    }

    public static int stagedExtraCount(Player p, Item item) {
        if (p == null || !(p.containerMenu instanceof WorkbenchMenu m)) return 0;
        int n = 0;
        for (int i = 1; i <= WorkbenchMenu.EXTRA_SLOTS; i++) {
            ItemStack s = m.input(i);
            if (!s.isEmpty() && s.getItem() == item) n += s.getCount();
        }
        return n;
    }

    public static int extraCount(Player p, Item item) {
        return stagedExtraCount(p, item) + inventoryCount(p, item);
    }

    public static boolean hasExtras(Player p, ToolUpgrade up, int targetLevel) {
        for (ItemStack extra : up.extraCosts(targetLevel))
            if (extraCount(p, extra.getItem()) < extra.getCount()) return false;
        return true;
    }

    public static int stagedCount(Player p, Item item) {
        if (p != null && p.containerMenu instanceof WorkbenchMenu m) {
            ItemStack staged = m.input(0);
            if (!staged.isEmpty() && staged.getItem() == item) return staged.getCount();
        }
        return 0;
    }

    public static boolean canApply(Player p, ItemStack tool, ToolUpgrade up) {
        return check(p, tool, up, false);
    }

    public static boolean canAfford(Player p, ItemStack tool, ToolUpgrade up) {
        return check(p, tool, up, true);
    }

    private static boolean check(Player p, ItemStack tool, ToolUpgrade up, boolean loose) {
        if (p == null || !(tool.getItem() instanceof ModifiableItem) || !ToolStack.isInitialized(tool)
                || tool.getCount() != 1 || !availableFor(tool.getItem()).contains(up)) return false;
        if (hasIncompatible(tool, up)) return false;
        int lvl = currentLevel(tool, up);
        if (lvl >= up.maxLevel()) return false;
        if (lvl == 0 && usedSlots(tool) >= unlockedSlots(tool)) return false;
        boolean bench = p.containerMenu instanceof WorkbenchMenu;
        if (!bench && p.getAbilities().instabuild) return true;
        int have = loose || !bench ? countIn(p, up.costItem()) : benchCount(p, up.costItem());
        return have >= up.costFor(lvl + 1) && hasExtras(p, up, lvl + 1);
    }

    public static boolean apply(Player p, ItemStack tool, ToolUpgrade up) {
        if (!canApply(p, tool, up)) return false;
        int next = currentLevel(tool, up) + 1;
        if (p.containerMenu instanceof WorkbenchMenu || !p.getAbilities().instabuild) {
            consume(p, up.costItem(), up.costFor(next));
            for (ItemStack extra : up.extraCosts(next)) consumeExtra(p, extra.getItem(), extra.getCount());
        }
        ToolStack.addModifier(tool, new ToolConstructionData.ModifierEntry(up.id(), next));
        ToolStack.recalculate(tool);
        return true;
    }

    private static void consume(Player p, Item item, int amount) {
        if (p.containerMenu instanceof WorkbenchMenu m) {
            ItemStack staged = m.input(0);
            if (!staged.isEmpty() && staged.getItem() == item) {
                int used = Math.min(amount, staged.getCount());
                ItemStack rest = staged.copy();
                rest.shrink(used);
                m.getBlockEntity().getInputSlots().setStackInSlot(0, rest);
                amount -= used;
            }
        }
        takeFromInventory(p, item, amount);
    }

    private static void consumeExtra(Player p, Item item, int amount) {
        if (p.containerMenu instanceof WorkbenchMenu m) {
            for (int i = 1; i <= WorkbenchMenu.EXTRA_SLOTS && amount > 0; i++) {
                ItemStack staged = m.input(i);
                if (staged.isEmpty() || staged.getItem() != item) continue;
                int used = Math.min(amount, staged.getCount());
                ItemStack rest = staged.copy();
                rest.shrink(used);
                m.getBlockEntity().getInputSlots().setStackInSlot(i, rest);
                amount -= used;
            }
        }
        takeFromInventory(p, item, amount);
    }

    private static void takeFromInventory(Player p, Item item, int amount) {
        if (amount <= 0) return;
        var inv = p.getInventory();
        int remaining = amount;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == item) {
                int take = Math.min(remaining, s.getCount());
                s.shrink(take);
                remaining -= take;
            }
        }
        if (remaining < amount) inv.setChanged();
    }
}