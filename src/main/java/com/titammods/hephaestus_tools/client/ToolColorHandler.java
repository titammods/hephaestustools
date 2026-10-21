package com.titammods.hephaestus_tools.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.titammods.hephaestus_tools.materials.Material;
import com.titammods.hephaestus_tools.materials.MaterialAnimation;
import com.titammods.hephaestus_tools.materials.MaterialId;
import com.titammods.hephaestus_tools.materials.MaterialManager;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ToolColorHandler {

    public static final ToolColorHandler INSTANCE = new ToolColorHandler();

    private static final int NO_TINT = 0xFFFFFFFF;

    private ToolColorHandler() {}

    public static float animationTicks() {
        return Util.getMillis() / 50f;
    }

    private static @Nullable Material material(ItemStack stack, int tintIndex) {
        if (tintIndex < 0 || !ToolStack.isInitialized(stack)) return null;
        int materialIndex = ToolLayerMap.materialIndexForLayer(stack.getItem(), tintIndex);
        if (materialIndex < 0) return null;
        MaterialId materialId = ToolStack.getMaterial(stack, materialIndex);
        if (materialId == null || materialId.isEmpty()) return null;
        return MaterialManager.getInstance().getMaterial(materialId);
    }

    public @Nullable MaterialAnimation getAnimation(ItemStack stack, int tintIndex) {
        Material material = material(stack, tintIndex);
        return material == null ? null : material.animation().orElse(null);
    }

    public int getAnimatedColor(ItemStack stack, int tintIndex) {
        int color = getColor(stack, tintIndex);
        MaterialAnimation animation = getAnimation(stack, tintIndex);
        return animation == null ? color : animation.uniform(color, animationTicks());
    }

    public int getColor(ItemStack stack, int tintIndex) {
        Material material = material(stack, tintIndex);
        if (material == null) {
            return NO_TINT;
        }

        int color = material.color();
        if ((color & 0xFF000000) == 0) {
            color |= 0xFF000000;
        }
        return color;
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