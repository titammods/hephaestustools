package com.titammods.hephaestus_tools.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ToolXpEvent extends Event implements ICancellableEvent {

    private final ItemStack stack;
    private final ServerPlayer player;
    private int amount;

    public ToolXpEvent(ItemStack stack, ServerPlayer player, int amount) {
        this.stack = stack;
        this.player = player;
        this.amount = amount;
    }

    public ItemStack getStack() {
        return stack;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
