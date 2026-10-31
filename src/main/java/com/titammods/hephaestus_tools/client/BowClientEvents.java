package com.titammods.hephaestus_tools.client;

import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.tools.item.ToolItems;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

@EventBusSubscriber(modid = HephaestusTools.MOD_ID, value = Dist.CLIENT)
public final class BowClientEvents {
    private BowClientEvents() {}

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (!player.isUsingItem()) return;
        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof ToolItems.BowItem)) return;
        float scale = Math.min(player.getTicksUsingItem() * ToolItems.BowItem.drawSpeed(stack) / 20.0F, 1.0F);
        float zoom = 1.0F - Mth.square(scale) * 0.15F;
        event.setNewFovModifier(event.getNewFovModifier() * Mth.lerp(event.getFovScale(), 1.0F, zoom));
    }
}
