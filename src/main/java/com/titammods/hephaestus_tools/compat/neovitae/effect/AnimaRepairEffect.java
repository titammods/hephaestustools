package com.titammods.hephaestus_tools.compat.neovitae.effect;

import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.titammods.hephaestus_tools.effect.EffectContext;
import com.titammods.hephaestus_tools.effect.LevelValue;
import com.titammods.hephaestus_tools.effect.ToolEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record AnimaRepairEffect(int interval, LevelValue cost, LevelValue amount) implements ToolEffect {
    public static final MapCodec<AnimaRepairEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("interval").forGetter(AnimaRepairEffect::interval),
                    LevelValue.CODEC.fieldOf("cost").forGetter(AnimaRepairEffect::cost),
                    LevelValue.CODEC.optionalFieldOf("amount", LevelValue.of(1f)).forGetter(AnimaRepairEffect::amount)
            ).apply(instance, AnimaRepairEffect::new));

    @Override
    public MapCodec<AnimaRepairEffect> codec() {
        return CODEC;
    }

    @Override
    public void inventoryTick(EffectContext context, int level) {
        ItemStack tool = context.tool();
        if (!(context.level() instanceof ServerLevel) || !(context.holder() instanceof Player player)) return;
        if (!tool.isDamaged() || context.level().getGameTime() % interval != 0L || player.isUsingItem()) return;
        int price = Math.max(1, (int) cost.get(level));
        IAnima anima = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
        if (anima == null || anima.getCurrentEV() < price) return;
        if (anima.syphon(AnimaTicket.create(price)) < price) return;
        tool.setDamageValue(Math.max(0, tool.getDamageValue() - Math.max(1, (int) amount.get(level))));
    }
}
