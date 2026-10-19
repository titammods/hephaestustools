package com.titammods.hephaestus_tools.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Posted on the NeoForge event bus when a Hephaestus tool is about to gain tool experience, before the
 * experience is added.
 * <p>
 * Listeners can change the amount, for example to scale the experience for an addon's trait, or cancel
 * the event so the tool gains nothing. An amount of 0 or less also adds nothing. The amount the tool
 * gains after every listener has run decides whether it levels up.
 * <p>
 * It fires on the logical server, for the player whose action earned the experience: breaking a block,
 * dealing damage or making a kill. It doesn't fire for a tool that has no parts yet or is already at
 * the highest level.
 */
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
