package com.titammods.hephaestus_tools.effect.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.titammods.hephaestus_tools.effect.DoubleLevelValue;
import com.titammods.hephaestus_tools.effect.EffectCondition;
import com.titammods.hephaestus_tools.effect.EffectContext;
import com.titammods.hephaestus_tools.effect.ToolEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record PullItemsEffect(int interval, DoubleLevelValue radius, DoubleLevelValue strength,
                              Optional<EffectCondition> condition) implements ToolEffect {

    public static final MapCodec<PullItemsEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("interval").forGetter(PullItemsEffect::interval),
                    DoubleLevelValue.CODEC.fieldOf("radius").forGetter(PullItemsEffect::radius),
                    DoubleLevelValue.CODEC.fieldOf("strength").forGetter(PullItemsEffect::strength),
                    EffectCondition.CODEC.optionalFieldOf("condition").forGetter(PullItemsEffect::condition)
            ).apply(instance, PullItemsEffect::new));

    @Override
    public MapCodec<PullItemsEffect> codec() {
        return CODEC;
    }

    @Override
    public void inventoryTick(EffectContext context, int level) {
        Level world = context.level();
        if (world.isClientSide() || world.getGameTime() % interval != 0L) return;
        if (condition.isPresent() && !condition.get().test(context)) return;
        LivingEntity holder = context.holder();
        double pullStrength = strength.get(level);
        for (ItemEntity item : world.getEntitiesOfClass(ItemEntity.class, holder.getBoundingBox().inflate(radius.get(level)))) {
            if (item.hasPickUpDelay() || !item.isAlive()) continue;
            Vec3 pull = holder.position().add(0, 0.5, 0).subtract(item.position()).normalize().scale(pullStrength);
            item.setDeltaMovement(item.getDeltaMovement().add(pull));
        }
    }
}
