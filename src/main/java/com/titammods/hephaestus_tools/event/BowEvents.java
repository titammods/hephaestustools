package com.titammods.hephaestus_tools.event;

import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.tools.item.ToolItems;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HephaestusTools.MOD_ID)
public final class BowEvents {
    private BowEvents() {}

    @SubscribeEvent
    public static void onArrowHit(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;
        ItemStack weapon = arrow.getWeaponItem();
        if (weapon == null || !(weapon.getItem() instanceof ToolItems.BowItem)) return;
        event.setAmount(event.getAmount() * ToolItems.BowItem.damageMultiplier(weapon));
    }
}
