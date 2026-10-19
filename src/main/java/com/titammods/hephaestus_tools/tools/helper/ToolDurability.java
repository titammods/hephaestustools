package com.titammods.hephaestus_tools.tools.helper;

import com.titammods.hephaestus_tools.effect.WearCause;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class ToolDurability {
    private static final ThreadLocal<WearCause> CAUSE = new ThreadLocal<>();

    private ToolDurability() {}

    public static void hurt(ItemStack tool, int amount, ServerLevel level, LivingEntity owner) {
        hurt(tool, amount, level, owner, WearCause.OTHER);
    }

    public static void hurt(ItemStack tool, int amount, ServerLevel level, LivingEntity owner, WearCause cause) {
        if (amount <= 0 || !ToolStack.isInitialized(tool) || ToolStack.isBroken(tool)
                || !tool.isDamageableItem() || (owner != null && owner.hasInfiniteMaterials())) return;
        int damage;
        CAUSE.set(cause);
        try {
            damage = tool.getItem().damageItem(tool, amount, owner, item -> {});
        } finally {
            CAUSE.remove();
        }
        damage = EnchantmentHelper.processDurabilityChange(level, tool, damage);
        if (damage <= 0) return;
        int next = Math.min(tool.getMaxDamage(), tool.getDamageValue() + damage);
        if (owner instanceof ServerPlayer player) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(player, tool, next);
        }
        tool.setDamageValue(next);
    }

    public static WearCause takeCause() {
        WearCause cause = CAUSE.get();
        CAUSE.remove();
        return cause == null ? WearCause.OTHER : cause;
    }
}
