package com.titammods.hephaestus_tools;

import com.titammods.hephaestus_tools.config.HephaestusConfig;
import com.titammods.hephaestus_tools.datagen.ModDataGenerators;
import com.titammods.hephaestus_tools.effect.EffectRegistries;
import com.titammods.hephaestus_tools.registry.ModBlocks;
import com.titammods.hephaestus_tools.registry.ModComponents;
import com.titammods.hephaestus_tools.registry.ModCreativeTabs;
import com.titammods.hephaestus_tools.registry.ModDataMaps;
import com.titammods.hephaestus_tools.registry.ModItems;
import com.titammods.hephaestus_tools.registry.ModMenus;
import com.titammods.hephaestus_tools.registry.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(HephaestusTools.MOD_ID)
public class HephaestusTools {

    public static final String MOD_ID = "hephaestus_tools";
    public static final String FORGE_MOD_ID = "hephaestus";

    public HephaestusTools(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, HephaestusConfig.SPEC, HephaestusConfig.FILE_NAME);
        ModComponents.REGISTRAR.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.BLOCK_ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        EffectRegistries.register(modBus);
        modBus.addListener(ModDataMaps::register);

        ModDataGenerators.register(modBus);
    }
}
