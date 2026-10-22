package com.titammods.hephaestus_tools.tables.blockentity;

import com.mojang.serialization.Codec;
import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.tools.part.ToolPartItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import com.titammods.hephaestus_tools.registry.ModBlocks;
import com.titammods.hephaestus_tools.table.TableStyle;
import com.titammods.hephaestus_tools.tables.menu.ArsenalTableMenu;
import com.titammods.hephaestus_tools.tables.menu.WorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArsenalTableBlockEntity extends BlockEntity implements MenuProvider {

    private static final Codec<List<ItemStack>> STACKS_CODEC = ItemStack.OPTIONAL_CODEC.listOf();

    private final ItemStackHandler upgradeSlot = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) { setChanged(); syncToClient(); }
        @Override public int getSlotLimit(int slot) { return 1; }
    };
    private final ItemStackHandler blueprintSlot = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) { setChanged(); syncToClient(); }
        @Override public int getSlotLimit(int slot) { return 1; }
    };
    private final ItemStackHandler inputSlots = new ItemStackHandler(4) {
        @Override protected void onContentsChanged(int slot) { setChanged(); syncToClient(); }
        @Override public int getSlotLimit(int slot) { return slot < 3 ? 99 : 1; }
    };
    private final ItemStackHandler outputSlot = new ItemStackHandler(1) {
        @Override protected void onContentsChanged(int slot) { setChanged(); syncToClient(); }
    };

    public static final int STORAGE_SIZE = 54;
    public static final TagKey<Item> STORABLE = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID, "forge_storage"));

    private final NonNullList<ItemStack> storageItems = NonNullList.withSize(STORAGE_SIZE, ItemStack.EMPTY);
    private final StorageContainer storage = new StorageContainer();

    public static boolean isStorable(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ToolPartItem || stack.is(STORABLE));
    }

    public ArsenalTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ARSENAL_TABLE_BE.get(), pos, state);
    }

    public ItemStackHandler getUpgradeSlot()   { return upgradeSlot; }
    public ItemStackHandler getBlueprintSlot() { return blueprintSlot; }
    public ItemStack getUpgradeItem()          { return upgradeSlot.getStackInSlot(0); }
    public ItemStackHandler getInputSlots()    { return inputSlots; }
    public ItemStackHandler getOutputSlot()    { return outputSlot; }
    public Container getStorage()              { return storage; }

    public int storedCount() {
        int count = 0;
        for (ItemStack stack : storageItems) if (!stack.isEmpty()) count++;
        return count;
    }

    public boolean hasStoredItems() {
        return storedCount() > 0;
    }

    private boolean noDrops;

    public void markNoDrops() {
        noDrops = true;
    }

    public boolean noDrops() {
        return noDrops;
    }

    public void compactStorage() {
        int write = 0;
        boolean moved = false;
        for (int read = 0; read < STORAGE_SIZE; read++) {
            ItemStack stack = storageItems.get(read);
            if (stack.isEmpty()) continue;
            if (read != write) {
                storageItems.set(write, stack);
                storageItems.set(read, ItemStack.EMPTY);
                moved = true;
            }
            write++;
        }
        if (moved) setChanged();
    }

    public void clearStorage() {
        for (int i = 0; i < STORAGE_SIZE; i++) storageItems.set(i, ItemStack.EMPTY);
        setChanged();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            dropContents(level, pos);
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer c = new SimpleContainer(7);
        c.setItem(0, upgradeSlot.getStackInSlot(0));
        c.setItem(1, blueprintSlot.getStackInSlot(0));
        for (int i = 0; i < 4; i++) c.setItem(2 + i, inputSlots.getStackInSlot(i));
        c.setItem(6, outputSlot.getStackInSlot(0));
        Containers.dropContents(level, pos, c);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        for (int i = 0; i < STORAGE_SIZE; i++) storageItems.set(i, ItemStack.EMPTY);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(storageItems);
        for (int i = 0; i < STORAGE_SIZE; i++) if (!isStorable(storageItems.get(i))) storageItems.set(i, ItemStack.EMPTY);
        compactStorage();
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (hasStoredItems()) components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(storageItems));
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("Storage");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hephaestus_tools.arsenal_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player player) {
        if (TableStyle.isModern(player)) return new WorkbenchMenu(containerId, inv, this);
        return new ArsenalTableMenu(containerId, inv, this);
    }

    private static List<ItemStack> snapshot(ItemStackHandler handler) {
        List<ItemStack> out = new ArrayList<>(handler.getSlots());
        for (int i = 0; i < handler.getSlots(); i++) out.add(handler.getStackInSlot(i).copy());
        return out;
    }

    private static void restore(ItemStackHandler handler, List<ItemStack> stacks) {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, i < stacks.size() ? stacks.get(i).copy() : ItemStack.EMPTY);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Upgrade", STACKS_CODEC, snapshot(upgradeSlot));
        output.store("Blueprint", STACKS_CODEC, snapshot(blueprintSlot));
        output.store("Inputs", STACKS_CODEC, snapshot(inputSlots));
        output.store("Output", STACKS_CODEC, snapshot(outputSlot));
        if (hasStoredItems()) output.store("Storage", STACKS_CODEC, new ArrayList<>(storageItems));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Upgrade", STACKS_CODEC).ifPresent(list -> restore(upgradeSlot, list));
        input.read("Blueprint", STACKS_CODEC).ifPresent(list -> restore(blueprintSlot, list));
        input.read("Inputs", STACKS_CODEC).ifPresent(list -> restore(inputSlots, list));
        input.read("Output", STACKS_CODEC).ifPresent(list -> restore(outputSlot, list));
        for (int i = 0; i < STORAGE_SIZE; i++) storageItems.set(i, ItemStack.EMPTY);
        input.read("Storage", STACKS_CODEC).ifPresent(list -> {
            for (int i = 0; i < Math.min(STORAGE_SIZE, list.size()); i++) storageItems.set(i, list.get(i).copy());
        });
    }

    private final class StorageContainer implements Container {

        @Override
        public int getContainerSize() {
            return STORAGE_SIZE;
        }

        @Override
        public boolean isEmpty() {
            return !hasStoredItems();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot >= 0 && slot < STORAGE_SIZE ? storageItems.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack out = ContainerHelper.removeItem(storageItems, slot, amount);
            if (!out.isEmpty()) setChanged();
            return out;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(storageItems, slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot < 0 || slot >= STORAGE_SIZE) return;
            storageItems.set(slot, stack);
            stack.limitSize(getMaxStackSize(stack));
            setChanged();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return isStorable(stack);
        }

        @Override
        public void setChanged() {
            ArsenalTableBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return !isRemoved() && Container.stillValidBlockEntity(ArsenalTableBlockEntity.this, player);
        }

        @Override
        public void clearContent() {
            clearStorage();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}