package com.titammods.hephaestus_tools.client;

import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.client.renderer.ArsenalTableRenderer;
import com.titammods.hephaestus_tools.client.renderer.ToolItemRenderer;
import com.titammods.hephaestus_tools.client.workbench.AnimHammer;
import com.titammods.hephaestus_tools.client.workbench.WorkbenchHandLayer;
import com.titammods.hephaestus_tools.client.workbench.WorkbenchPlayerAnim;
import com.titammods.hephaestus_tools.registry.ModBlocks;
import com.titammods.hephaestus_tools.registry.ModItems;
import com.titammods.hephaestus_tools.registry.ModMenus;
import com.titammods.hephaestus_tools.tables.screen.ArsenalTableScreen;
import com.titammods.hephaestus_tools.tables.screen.ForgeStorageScreen;
import com.titammods.hephaestus_tools.tables.screen.WorkbenchScreen;
import com.titammods.hephaestus_tools.tools.helper.ToolEnchantments;
import com.titammods.hephaestus_tools.tools.item.ModifiableItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = HephaestusTools.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    private static Item[] tools() {
        return ModItems.TOOLS.stream().map(d -> (Item) d.get()).toArray(Item[]::new);
    }

    private static Item[] parts() {
        return ModItems.PARTS.stream().map(d -> (Item) d.get()).toArray(Item[]::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(WorkbenchPlayerAnim::register);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(AnimHammer.MODEL);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ARSENAL_TABLE.get(), ArsenalTableScreen::new);
        event.register(ModMenus.WORKBENCH.get(), WorkbenchScreen::new);
        event.register(ModMenus.FORGE_STORAGE.get(), ForgeStorageScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.ARSENAL_TABLE_BE.get(), ArsenalTableRenderer::new);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<? extends Player> renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer player) player.addLayer(new WorkbenchHandLayer(player));
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(ToolColorHandler.INSTANCE, tools());
        event.register(PartColorHandler.INSTANCE, parts());
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ToolItemRenderer renderer = new ToolItemRenderer();
        IClientItemExtensions ext = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        };
        event.registerItem(ext, tools());
        event.registerBlock(ArsenalTableParticles.INSTANCE, ModBlocks.ARSENAL_TABLE.get());
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        ResourceManagerReloadListener listener = manager -> ToolItemRenderer.clearTextureCache();
        event.registerReloadListener(listener);
    }

    @EventBusSubscriber(modid = HephaestusTools.MOD_ID, value = Dist.CLIENT)
    public static class GameEvents {
        private GameEvents() {}

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onItemTooltip(ItemTooltipEvent event) {
            if (!(event.getItemStack().getItem() instanceof ModifiableItem)) return;
            event.getToolTip().removeIf(ToolEnchantments::isMergedLine);
        }
    }
}