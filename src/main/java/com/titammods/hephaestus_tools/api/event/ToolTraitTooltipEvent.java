package com.titammods.hephaestus_tools.api.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.function.Consumer;

public class ToolTraitTooltipEvent extends Event {

    private final ItemStack stack;
    private final Consumer<Component> tooltip;

    public ToolTraitTooltipEvent(ItemStack stack, Consumer<Component> tooltip) {
        this.stack = stack;
        this.tooltip = tooltip;
    }

    public ItemStack getStack() {
        return stack;
    }

    public Consumer<Component> getTooltip() {
        return tooltip;
    }

    public void add(Component line) {
        tooltip.accept(line);
    }
}
