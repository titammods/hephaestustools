package com.titammods.hephaestus_tools.compat.neovitae.effect;

import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.stream.StreamEffect;
import com.breakinblocks.neovitae.common.NVSounds;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.titammods.hephaestus_tools.effect.EffectContext;
import com.titammods.hephaestus_tools.effect.HitDetails;
import com.titammods.hephaestus_tools.effect.LevelValue;
import com.titammods.hephaestus_tools.effect.ToolEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record AnimaSiphonEffect(LevelValue perDamage) implements ToolEffect {
    public static final MapCodec<AnimaSiphonEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    LevelValue.CODEC.fieldOf("per_damage").forGetter(AnimaSiphonEffect::perDamage)
            ).apply(instance, AnimaSiphonEffect::new));

    @Override
    public MapCodec<AnimaSiphonEffect> codec() {
        return CODEC;
    }

    @Override
    public void afterHit(EffectContext context, HitDetails hit, int level) {
        LivingEntity target = context.target();
        if (!(context.level() instanceof ServerLevel serverLevel) || !(context.holder() instanceof Player player) || target == null) return;
        int gained = (int) (hit.damage() * perDamage.get(level));
        if (gained <= 0) return;
        IAnima anima = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (anima == null) return;
        anima.add(AnimaTicket.create(gained), Integer.MAX_VALUE);
        serverLevel.playSound(null, player.blockPosition(), NVSounds.BLOOD_SIPHON.get(), SoundSource.PLAYERS, 0.3f, 1.0f);
        StreamEffect.builder(target).toTracked(player)
                .color(0x990011).scale(0.05f).speed(2.0f).gravity(0.25f)
                .wobble(0.03f).wobbleFrequency(0.8f)
                .spiralInto(true).spiralRadius(0.35f).spiralSpeed(0.35f)
                .approachHeight(1.0f).alphaStart(0.08f).alphaEnd(0.95f)
                .build().sendToNearby(serverLevel, player.blockPosition(), 32);
    }
}
