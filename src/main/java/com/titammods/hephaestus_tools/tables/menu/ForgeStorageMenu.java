package com.titammods.hephaestus_tools.tables.menu;

import com.titammods.hephaestus_tools.registry.ModMenus;
import com.titammods.hephaestus_tools.tables.block.ArsenalTableBlock;
import com.titammods.hephaestus_tools.tables.blockentity.ArsenalTableBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ForgeStorageMenu extends AbstractContainerMenu {

    public static final int SIZE = ArsenalTableBlockEntity.STORAGE_SIZE;
    public static final int COLUMNS = 9;

    private final Container storage;
    private final @Nullable ArsenalTableBlockEntity blockEntity;

    public ForgeStorageMenu(int id, Inventory inv, @Nullable ArsenalTableBlockEntity blockEntity) {
        this(id, inv, blockEntity, blockEntity != null && !inv.player.level().isClientSide()
                ? blockEntity.getStorage() : new SimpleContainer(SIZE));
    }

    private ForgeStorageMenu(int id, Inventory inv, @Nullable ArsenalTableBlockEntity blockEntity, Container storage) {
        super(ModMenus.FORGE_STORAGE.get(), id);
        this.storage = storage;
        this.blockEntity = blockEntity;
        storage.startOpen(inv.player);
        for (int i = 0; i < SIZE; i++) {
            final int index = i;
            addSlot(new Slot(storage, i, 8 + (i % COLUMNS) * 18, 18 + (i / COLUMNS) * 18) {
                @Override public boolean isActive() { return index < visibleSlots(); }
                @Override public boolean mayPlace(ItemStack stack) { return index < visibleSlots() && ArsenalTableBlockEntity.isStorable(stack); }
            });
        }
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, 8 + col * 18, 198));
    }

    public @Nullable ArsenalTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int storedCount() {
        int count = 0;
        for (int i = 0; i < SIZE; i++) if (!storage.getItem(i).isEmpty()) count++;
        return count;
    }

    public int visibleSlots() {
        return Math.min(SIZE, storedCount() + 1);
    }

    public int visibleRows() {
        return Math.max(1, (visibleSlots() + COLUMNS - 1) / COLUMNS);
    }

    private void compact() {
        if (blockEntity != null && storage == blockEntity.getStorage()) blockEntity.compactStorage();
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        super.clicked(slotIndex, button, input, player);
        compact();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < SIZE) {
            if (!moveItemStackTo(stack, SIZE, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!ArsenalTableBlockEntity.isStorable(stack)) return ItemStack.EMPTY;
            if (!moveIntoStorage(stack)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        compact();
        return copy;
    }

    private boolean moveIntoStorage(ItemStack stack) {
        boolean moved = false;
        for (int i = 0; i < SIZE && !stack.isEmpty(); i++) {
            ItemStack existing = storage.getItem(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) continue;
            int room = Math.min(storage.getMaxStackSize(existing), existing.getMaxStackSize()) - existing.getCount();
            if (room <= 0) continue;
            int add = Math.min(room, stack.getCount());
            existing.grow(add);
            stack.shrink(add);
            storage.setChanged();
            moved = true;
        }
        while (!stack.isEmpty()) {
            int free = -1;
            for (int i = 0; i < SIZE; i++) if (storage.getItem(i).isEmpty()) { free = i; break; }
            if (free < 0) break;
            int add = Math.min(stack.getCount(), Math.min(storage.getMaxStackSize(stack), stack.getMaxStackSize()));
            storage.setItem(free, stack.split(add));
            moved = true;
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) return true;
        if (blockEntity.isRemoved() || blockEntity.getLevel() == null) return false;
        BlockState state = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
        if (!(state.getBlock() instanceof ArsenalTableBlock) || !state.getValue(ArsenalTableBlock.OPEN)) return false;
        return storage.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        storage.stopOpen(player);
        compact();
    }
}