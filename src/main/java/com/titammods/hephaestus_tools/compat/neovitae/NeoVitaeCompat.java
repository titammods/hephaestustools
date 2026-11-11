package com.titammods.hephaestus_tools.compat.neovitae;

import com.titammods.hephaestus_tools.registry.ModItems;
import com.titammods.hephaestus_tools.tools.part.ToolPartItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public final class NeoVitaeCompat {
    public static final String MOD_ID = "neovitae";
    public static final boolean LOADED = ModList.get().isLoaded(MOD_ID);

    public static final DeferredItem<StaffItem> STAFF = LOADED
            ? ModItems.ITEMS.register("staff", key -> new StaffItem(props(key).stacksTo(1).durability(100)))
            : null;
    public static final DeferredItem<ToolPartItem> STAFF_FOCUS = LOADED
            ? ModItems.ITEMS.register("staff_focus", key -> new ToolPartItem(props(key), 0))
            : null;
    public static final DeferredItem<Item> STAFF_FOCUS_CAST = LOADED
            ? ModItems.ITEMS.register("staff_focus_cast", key -> new Item(props(key)))
            : null;

    private NeoVitaeCompat() {}

    private static Item.Properties props(Identifier key) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key));
    }

    public static void init() {
        if (LOADED) NeoVitaeEffects.register();
    }

    public static List<Item> tools() {
        return LOADED ? List.of(STAFF.get()) : List.of();
    }

    public static List<Item> parts() {
        return LOADED ? List.of(STAFF_FOCUS.get()) : List.of();
    }

    public static List<Item> casts() {
        return LOADED ? List.of(STAFF_FOCUS_CAST.get()) : List.of();
    }
}
