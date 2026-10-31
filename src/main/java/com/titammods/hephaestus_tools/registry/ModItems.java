package com.titammods.hephaestus_tools.registry;

import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.tools.item.ToolItems;
import com.titammods.hephaestus_tools.tools.part.ToolPartItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Function;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HephaestusTools.MOD_ID);

    private static Item.Properties toolProps(Identifier key) {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, key))
                .stacksTo(1)
                .durability(100);
    }

    private static Item.Properties plainProps(Identifier key) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key));
    }

    private static <T extends Item> DeferredItem<T> tool(String name, Function<Item.Properties, T> factory) {
        return ITEMS.register(name, k -> factory.apply(toolProps(k)));
    }

    private static DeferredItem<ToolPartItem> part(String name, int slot) {
        return ITEMS.register(name, k -> new ToolPartItem(plainProps(k), slot));
    }

    private static DeferredItem<Item> simple(String name) {
        return ITEMS.register(name, k -> new Item(plainProps(k)));
    }

    private static DeferredItem<Item> stackable(String name, int max) {
        return ITEMS.register(name, k -> new Item(plainProps(k).stacksTo(max)));
    }

    public static final DeferredItem<ToolItems.PickaxeItem> PICKAXE =
            tool("pickaxe", ToolItems.PickaxeItem::new);

    public static final DeferredItem<ToolItems.SledgeHammerItem> SLEDGE_HAMMER =
            tool("sledge_hammer", ToolItems.SledgeHammerItem::new);

    public static final DeferredItem<ToolItems.VeinHammerItem> VEIN_HAMMER =
            tool("vein_hammer", ToolItems.VeinHammerItem::new);

    public static final DeferredItem<ToolItems.MattockItem> MATTOCK =
            tool("mattock", ToolItems.MattockItem::new);

    public static final DeferredItem<ToolItems.PaxelItem> PAXEL =
            tool("paxel", ToolItems.PaxelItem::new);

    public static final DeferredItem<ToolItems.ExcavatorItem> EXCAVATOR =
            tool("excavator", ToolItems.ExcavatorItem::new);

    public static final DeferredItem<ToolItems.HandAxeItem> HAND_AXE =
            tool("hand_axe", ToolItems.HandAxeItem::new);

    public static final DeferredItem<ToolItems.BroadAxeItem> BROAD_AXE =
            tool("broad_axe", ToolItems.BroadAxeItem::new);

    public static final DeferredItem<ToolItems.KamaItem> KAMA =
            tool("kama", ToolItems.KamaItem::new);

    public static final DeferredItem<ToolItems.ScytheItem> SCYTHE =
            tool("scythe", ToolItems.ScytheItem::new);

    public static final DeferredItem<ToolItems.DaggerItem> DAGGER =
            tool("dagger", ToolItems.DaggerItem::new);

    public static final DeferredItem<ToolItems.SwordItem> SWORD =
            tool("sword", ToolItems.SwordItem::new);

    public static final DeferredItem<ToolItems.CleaverItem> CLEAVER =
            tool("cleaver", ToolItems.CleaverItem::new);

    public static final DeferredItem<ToolItems.MaceItem> MACE =
            tool("mace", ToolItems.MaceItem::new);

    public static final DeferredItem<ToolPartItem> PICK_HEAD       = part("pick_head", 0);
    public static final DeferredItem<ToolPartItem> HAMMER_HEAD     = part("hammer_head", 0);
    public static final DeferredItem<ToolPartItem> SMALL_AXE_HEAD  = part("small_axe_head", 0);
    public static final DeferredItem<ToolPartItem> ADZE_HEAD       = part("adze_head", 0);
    public static final DeferredItem<ToolPartItem> LARGE_PLATE     = part("large_plate", 0);
    public static final DeferredItem<ToolPartItem> BROAD_AXE_HEAD  = part("broad_axe_head", 0);
    public static final DeferredItem<ToolPartItem> SMALL_BLADE     = part("small_blade", 0);
    public static final DeferredItem<ToolPartItem> LARGE_BLADE     = part("large_blade", 0);
    public static final DeferredItem<ToolPartItem> TOOL_HANDLE     = part("tool_handle", 1);
    public static final DeferredItem<ToolPartItem> TOUGH_HANDLE    = part("tough_handle", 1);
    public static final DeferredItem<ToolPartItem> TOOL_BINDING    = part("tool_binding", 2);
    public static final DeferredItem<ToolPartItem> TOUGH_BINDING   = part("tough_binding", 2);
    public static final DeferredItem<ToolPartItem> MACE_HEAD       = part("mace_head", 0);

    public static final DeferredItem<Item> PATTERN = stackable("pattern", 64);
    public static final DeferredItem<Item> FORGE_KEY = ITEMS.register("forge_key", k -> new Item(plainProps(k).stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> PICK_HEAD_CAST      = simple("pick_head_cast");
    public static final DeferredItem<Item> HAMMER_HEAD_CAST    = simple("hammer_head_cast");
    public static final DeferredItem<Item> SMALL_AXE_HEAD_CAST = simple("small_axe_head_cast");
    public static final DeferredItem<Item> BROAD_AXE_HEAD_CAST = simple("broad_axe_head_cast");
    public static final DeferredItem<Item> ADZE_HEAD_CAST      = simple("adze_head_cast");
    public static final DeferredItem<Item> LARGE_PLATE_CAST    = simple("large_plate_cast");
    public static final DeferredItem<Item> SMALL_BLADE_CAST    = simple("small_blade_cast");
    public static final DeferredItem<Item> LARGE_BLADE_CAST    = simple("large_blade_cast");
    public static final DeferredItem<Item> TOOL_HANDLE_CAST    = simple("tool_handle_cast");
    public static final DeferredItem<Item> TOUGH_HANDLE_CAST   = simple("tough_handle_cast");
    public static final DeferredItem<Item> TOOL_BINDING_CAST   = simple("tool_binding_cast");
    public static final DeferredItem<Item> TOUGH_BINDING_CAST  = simple("tough_binding_cast");
    public static final DeferredItem<Item> MACE_HEAD_CAST      = simple("mace_head_cast");

    public static final List<DeferredItem<Item>> CASTS = List.of(
            PICK_HEAD_CAST, HAMMER_HEAD_CAST, SMALL_AXE_HEAD_CAST, BROAD_AXE_HEAD_CAST,
            ADZE_HEAD_CAST, LARGE_PLATE_CAST, SMALL_BLADE_CAST, LARGE_BLADE_CAST,
            TOOL_HANDLE_CAST, TOUGH_HANDLE_CAST, TOOL_BINDING_CAST, TOUGH_BINDING_CAST, MACE_HEAD_CAST);

    public static final List<DeferredItem<? extends Item>> TOOLS = List.of(
            PICKAXE, SLEDGE_HAMMER, VEIN_HAMMER, MATTOCK, PAXEL, EXCAVATOR, HAND_AXE,
            BROAD_AXE, KAMA, SCYTHE, DAGGER, SWORD, CLEAVER, MACE);

    public static final List<DeferredItem<ToolPartItem>> PARTS = List.of(
            PICK_HEAD, HAMMER_HEAD, SMALL_AXE_HEAD, BROAD_AXE_HEAD, ADZE_HEAD, LARGE_PLATE,
            SMALL_BLADE, LARGE_BLADE, TOOL_HANDLE, TOUGH_HANDLE, TOOL_BINDING, TOUGH_BINDING, MACE_HEAD);
}