package com.titammods.hephaestus_tools.compat.neovitae.effect;

import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.titammods.hephaestus_tools.effect.EffectContext;
import com.titammods.hephaestus_tools.effect.LevelValue;
import com.titammods.hephaestus_tools.effect.ToolEffect;
import com.titammods.hephaestus_tools.effect.WearCause;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public record BloodPactEffect(LevelValue cost) implements ToolEffect {
    public static final MapCodec<BloodPactEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    LevelValue.CODEC.fieldOf("cost").forGetter(BloodPactEffect::cost)
            ).apply(instance, BloodPactEffect::new));

    @Override
    public MapCodec<BloodPactEffect> codec() {
        return CODEC;
    }

    @Override
    public int modifyWear(EffectContext context, int amount, WearCause cause, int level) {
        if (amount <= 0 || !(context.level() instanceof ServerLevel) || !(context.holder() instanceof Player player)) return amount;
        int price = Math.max(1, (int) (amount * cost.get(level)));
        IAnima anima = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (anima == null || anima.getCurrentEV() < price) return amount;
        return anima.syphon(AnimaTicket.create(price)) >= price ? 0 : amount;
    }
}
