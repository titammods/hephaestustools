package com.titammods.hephaestus_tools.api.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ToolRecalculatedEvent extends Event {

    private final ItemStack stack;

    public ToolRecalculatedEvent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }
}
