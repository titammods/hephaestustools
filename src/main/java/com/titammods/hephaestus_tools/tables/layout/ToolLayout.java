package com.titammods.hephaestus_tools.tables.layout;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ToolLayout(
        String id,
        Component displayName,
        Component description,
        Item iconItem,
        ToolSlotDef toolSlot,
        List<InputSlotDef> inputs
) {
    public record ToolSlotDef(int x, int y, boolean hidden) {
        public ToolSlotDef(int x, int y) { this(x, y, false); }
    }

    public record InputSlotDef(int x, int y, @Nullable Item expectedPart) {
        public InputSlotDef(int x, int y) { this(x, y, null); }
    }

    public int inputCount() { return inputs.size(); }
}
