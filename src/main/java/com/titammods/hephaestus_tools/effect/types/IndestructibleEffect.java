package com.titammods.hephaestus_tools.effect.types;

import com.mojang.serialization.MapCodec;
import com.titammods.hephaestus_tools.effect.ToolEffect;
import com.titammods.hephaestus_tools.tools.modifier.ModifierStatContext;

public record IndestructibleEffect() implements ToolEffect {

    public static final IndestructibleEffect INSTANCE = new IndestructibleEffect();
    public static final MapCodec<IndestructibleEffect> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<IndestructibleEffect> codec() {
        return CODEC;
    }

    @Override
    public void modifyStats(ModifierStatContext stats, int level) {
        stats.indestructible = true;
    }
}