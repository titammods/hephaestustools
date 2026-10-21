package com.titammods.hephaestus_tools.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.titammods.hephaestus_tools.materials.Material;
import com.titammods.hephaestus_tools.materials.MaterialId;
import com.titammods.hephaestus_tools.materials.MaterialManager;
import com.titammods.hephaestus_tools.tools.part.ToolPartItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class PartColorHandler {

    public static final PartColorHandler INSTANCE = new PartColorHandler();

    private static final int NO_TINT = 0xFFFFFFFF;

    private PartColorHandler() {}

    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) return NO_TINT;
        if (!(stack.getItem() instanceof ToolPartItem part)) return NO_TINT;

        MaterialId id = part.getMaterial(stack);
        if (id == null || id.isEmpty()) return NO_TINT;

        Material material = MaterialManager.getInstance().getMaterial(id);
        if (material == null) return NO_TINT;

        int color = material.color();
        if ((color & 0xFF000000) == 0) color |= 0xFF000000;
        int base = color;
        return material.animation().map(a -> a.uniform(base, ToolColorHandler.animationTicks())).orElse(color);
    }

    public record Tint(int index) implements ItemTintSource {

        public static final MapCodec<Tint> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("index", 0).forGetter(Tint::index)
                ).apply(instance, Tint::new)
        );

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return INSTANCE.getColor(stack, index);
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return MAP_CODEC;
        }
    }
}