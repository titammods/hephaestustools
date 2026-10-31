package com.titammods.hephaestus_tools.client;

import com.titammods.hephaestus_tools.registry.ModItems;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public final class ToolLayerMap {

    private ToolLayerMap() {}

    private static final Map<Item, int[]> LAYER_TO_MATERIAL = new HashMap<>();

    private static final int[] IDENTITY = {0, 1, 2, 3};

    private static volatile boolean initialized = false;

    private static void ensureInit() {
        if (initialized) return;
        synchronized (ToolLayerMap.class) {
            if (initialized) return;
            put(ModItems.PICKAXE.get(),       1, 0, 2);
            put(ModItems.SLEDGE_HAMMER.get(), 1, 0, 2, 3);
            put(ModItems.VEIN_HAMMER.get(),  1, 0, 3, 2);
            put(ModItems.MATTOCK.get(),      1, 0, 2);
            put(ModItems.PAXEL.get(),        1, 0);
            put(ModItems.EXCAVATOR.get(),    1, 3, 2, 0);
            put(ModItems.HAND_AXE.get(),      1, 0, 2);
            put(ModItems.BROAD_AXE.get(),    1, 0, 3, 2);
            put(ModItems.KAMA.get(),          1, 0, 2);
            put(ModItems.SCYTHE.get(),       1, 3, 2, 0);
            put(ModItems.DAGGER.get(),       1, 1, 1, 0);
            put(ModItems.SWORD.get(),        1, 0, 2);
            put(ModItems.MACE.get(),         1, 0);
            put(ModItems.SPEAR.get(),        1, 0, 2);
            put(ModItems.BOW.get(),          1, 0, 2);
            put(ModItems.CLEAVER.get(),      1, 0, 3, 3);
            initialized = true;
        }
    }

    private static void put(Item item, int... layerToMaterial) {
        LAYER_TO_MATERIAL.put(item, layerToMaterial);
    }

    public static int materialIndexForLayer(Item item, int tintIndex) {
        ensureInit();
        int[] map = LAYER_TO_MATERIAL.getOrDefault(item, IDENTITY);
        if (tintIndex < 0 || tintIndex >= map.length) {
            return -1;
        }
        return map[tintIndex];
    }
}
