package com.titammods.hephaestus_tools.effect.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.titammods.hephaestus_tools.effect.EffectContext;
import com.titammods.hephaestus_tools.effect.LevelValue;
import com.titammods.hephaestus_tools.effect.ToolEffect;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Collection;
import java.util.Map;

public record BeheadEffect(LevelValue chance, Map<Identifier, Identifier> heads, boolean playerHeads) implements ToolEffect {

    public static final MapCodec<BeheadEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    LevelValue.CODEC.fieldOf("chance").forGetter(BeheadEffect::chance),
                    Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC).optionalFieldOf("heads", Map.of()).forGetter(BeheadEffect::heads),
                    Codec.BOOL.optionalFieldOf("player_heads", true).forGetter(BeheadEffect::playerHeads)
            ).apply(instance, BeheadEffect::new));

    @Override
    public MapCodec<BeheadEffect> codec() {
        return CODEC;
    }

    @Override
    public void modifyKillDrops(EffectContext context, LivingEntity victim, Collection<ItemEntity> drops, int level) {
        ItemStack head = head(victim);
        if (head.isEmpty()) return;
        for (ItemEntity drop : drops) if (drop.getItem().is(head.getItem())) return;
        if (victim.getRandom().nextFloat() >= chance.get(level)) return;
        drops.add(new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(), head));
    }

    private ItemStack head(LivingEntity victim) {
        if (victim instanceof Player player) {
            if (!playerHeads) return ItemStack.EMPTY;
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
            return stack;
        }
        Identifier head = heads.get(BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()));
        if (head == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getValue(head);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }
}