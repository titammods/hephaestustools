package com.titammods.hephaestus_tools.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PartCastingMeltingProvider implements DataProvider {

    private static final String TOOLS = "hephaestus_tools";
    private static final String FORGE = "hephaestus";

    private final PackOutput.PathProvider recipePath;

    public PartCastingMeltingProvider(PackOutput output) {
        this.recipePath = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    private record Part(String name, int cost) {}

    private record Metal(String name, int temp, List<String> mods, String fluid) {
        Metal(String name, int temp, List<String> mods) {
            this(name, temp, mods, FORGE + ":molten_" + name);
        }

        boolean vanilla() {
            return mods.isEmpty();
        }
    }

    private static final List<String> NO_MODS = List.of();
    private static final List<String> ORE_MODS = List.of("alltheores", "ftbmaterials");
    private static final List<String> ARMORY_MODS = List.of("ftbarmory");
    private static final List<String> STEEL_MODS = List.of("ftbmaterials", "ftbic", "energizedpower", "oritech", "mffs");
    private static final List<String> IRIDIUM_MODS = List.of("ftbmaterials", "ftbic");
    private static final List<String> TITANIUM_MODS = List.of("ftbmaterials", "ltxi", "stellaris");
    private static final List<String> CHROMIUM_MODS = List.of("ftbmaterials", "colossal_reactors");
    private static final List<String> ENDERIO_MODS = List.of("enderio");
    private static final List<String> REDSTONE_ALLOY_MODS = List.of("enderio", "energizedpower");
    private static final List<String> ENDERIO_EVOLUTION_MODS = List.of("enderio_evolution");
    private static final List<String> ENERGIZEDPOWER_MODS = List.of("energizedpower");
    private static final List<String> ADVANCED_ALLOY_MODS = List.of("energizedpower", "ftbic");
    private static final List<String> LTXI_MODS = List.of("ltxi");
    private static final List<String> STELLARIS_MODS = List.of("stellaris");
    private static final List<String> JUSTDIRETHINGS_MODS = List.of("justdirethings");
    private static final List<String> REPLICATION_MODS = List.of("replication");
    private static final List<String> THAUMATURGE_MODS = List.of("thaumaturge");
    private static final List<String> NEOVITAE_MODS = List.of("neovitae");
    private static final List<String> NAUTEC_MODS = List.of("nautec");
    private static final List<String> ICEANDFIRE_MODS = List.of("iceandfire");
    private static final List<String> ORITECH_MODS = List.of("oritech");
    private static final List<String> OCCULTISM_MODS = List.of("occultism");
    private static final List<String> RUBY_MODS = List.of("ftbmaterials", "irons_jewelry");
    private static final List<String> SAPPHIRE_MODS = List.of("ftbmaterials", "iceandfire", "irons_jewelry");
    private static final List<String> IRONS_JEWELRY_MODS = List.of("irons_jewelry");
    private static final List<String> GEMSTONE_MODS = List.of("irons_jewelry", "arsmagicalegacy");
    private static final List<String> AE2_MODS = List.of("ae2");
    private static final List<String> EXTENDEDAE_MODS = List.of("extendedae");
    private static final List<String> XYCRAFT_MODS = List.of("xycraft_world");
    private static final List<String> EVILCRAFT_MODS = List.of("evilcraft");
    private static final List<String> NODEWORKS_MODS = List.of("nodeworks");
    private static final List<String> FTBMATERIALS_MODS = List.of("ftbmaterials");
    private static final List<String> AGRADDITIONS_MODS = List.of("mysticalagradditions");
    private static final List<String> COLOSSAL_MODS = List.of("colossal_reactors");

    private static final List<Part> PARTS = List.of(
            new Part("pick_head", 180),
            new Part("hammer_head", 720),
            new Part("small_axe_head", 180),
            new Part("broad_axe_head", 720),
            new Part("adze_head", 180),
            new Part("large_plate", 360),
            new Part("small_blade", 180),
            new Part("large_blade", 720),
            new Part("tool_handle", 90),
            new Part("tough_handle", 270),
            new Part("tool_binding", 90),
            new Part("tough_binding", 270)
    );

    private static final List<Metal> METALS = List.of(
            new Metal("iron", 900, NO_MODS),
            new Metal("copper", 900, NO_MODS),
            new Metal("aluminum", 660, ORE_MODS),
            new Metal("brass", 930, ORE_MODS),
            new Metal("bronze", 950, ORE_MODS),
            new Metal("constantan", 1220, ORE_MODS),
            new Metal("electrum", 1000, ORE_MODS),
            new Metal("enderium", 1450, ORE_MODS),
            new Metal("invar", 1420, ORE_MODS),
            new Metal("lead", 327, ORE_MODS),
            new Metal("lumium", 1000, ORE_MODS),
            new Metal("nickel", 1450, ORE_MODS),
            new Metal("osmium", 3000, ORE_MODS),
            new Metal("platinum", 1768, ORE_MODS),
            new Metal("signalum", 1000, ORE_MODS),
            new Metal("silver", 960, ORE_MODS),
            new Metal("tin", 230, ORE_MODS),
            new Metal("uranium", 1130, ORE_MODS),
            new Metal("zinc", 419, ORE_MODS),
            new Metal("adamantite", 2000, ARMORY_MODS),
            new Metal("aeternium", 2000, ARMORY_MODS),
            new Metal("aurichalcum", 2000, ARMORY_MODS),
            new Metal("gold", 1060, NO_MODS),
            new Metal("netherite", 2000, NO_MODS),
            new Metal("steel", 1370, STEEL_MODS),
            new Metal("iridium", 2440, IRIDIUM_MODS),
            new Metal("iesnium", 1200, OCCULTISM_MODS),
            new Metal("titanium", 1000, TITANIUM_MODS),
            new Metal("chromium", 1000, CHROMIUM_MODS),
            new Metal("niobium", 1000, LTXI_MODS),
            new Metal("conductive_alloy", 800, ENDERIO_MODS),
            new Metal("redstone_alloy", 800, REDSTONE_ALLOY_MODS),
            new Metal("energetic_alloy", 800, ENDERIO_MODS),
            new Metal("pulsating_alloy", 800, ENDERIO_MODS),
            new Metal("soularium", 800, ENDERIO_MODS),
            new Metal("vibrant_alloy", 1000, ENDERIO_MODS),
            new Metal("dark_steel", 1000, ENDERIO_MODS),
            new Metal("end_steel", 1600, ENDERIO_MODS),
            new Metal("energetic_silver", 800, ENDERIO_EVOLUTION_MODS),
            new Metal("vivid_alloy", 1000, ENDERIO_EVOLUTION_MODS),
            new Metal("energized_copper", 800, ENERGIZEDPOWER_MODS),
            new Metal("energized_gold", 800, ENERGIZEDPOWER_MODS),
            new Metal("advanced_alloy", 1000, ADVANCED_ALLOY_MODS),
            new Metal("energized_alloy", 1000, ENERGIZEDPOWER_MODS),
            new Metal("crystallized_alloy", 1000, ENERGIZEDPOWER_MODS),
            new Metal("slatesteel", 1000, LTXI_MODS),
            new Metal("tungsten_slatesteel", 1600, LTXI_MODS),
            new Metal("desh", 1000, STELLARIS_MODS),
            new Metal("ferricore", 800, JUSTDIRETHINGS_MODS),
            new Metal("blazegold", 1000, JUSTDIRETHINGS_MODS),
            new Metal("eclipse_alloy", 1600, JUSTDIRETHINGS_MODS),
            new Metal("replica", 1000, REPLICATION_MODS),
            new Metal("thaumium", 1000, THAUMATURGE_MODS),
            new Metal("void_metal", 1600, THAUMATURGE_MODS),
            new Metal("hellforged", 1600, NEOVITAE_MODS),
            new Metal("atlantic_gold", 1200, NAUTEC_MODS),
            new Metal("aquarine_steel", 1200, NAUTEC_MODS),
            new Metal("dragonsteel_fire", 1600, ICEANDFIRE_MODS),
            new Metal("dragonsteel_ice", 1600, ICEANDFIRE_MODS),
            new Metal("dragonsteel_lightning", 1600, ICEANDFIRE_MODS),
            new Metal("adamant", 1000, ORITECH_MODS),
            new Metal("duratium", 1600, ORITECH_MODS),
            new Metal("prometheum", 1600, ORITECH_MODS),
            new Metal("fluxite", 1000, ORITECH_MODS),
            new Metal("ruby", 1000, RUBY_MODS),
            new Metal("sapphire", 1000, SAPPHIRE_MODS),
            new Metal("peridot", 1000, IRONS_JEWELRY_MODS),
            new Metal("onyx", 1000, IRONS_JEWELRY_MODS),
            new Metal("garnet", 1000, IRONS_JEWELRY_MODS),
            new Metal("topaz", 1000, GEMSTONE_MODS),
            new Metal("moonstone", 800, GEMSTONE_MODS),
            new Metal("olivine", 800, LTXI_MODS),
            new Metal("certus_quartz", 800, AE2_MODS),
            new Metal("fluix", 1000, AE2_MODS),
            new Metal("entro", 1000, EXTENDEDAE_MODS),
            new Metal("xychorium", 800, XYCRAFT_MODS),
            new Metal("spirit_attuned", 1000, OCCULTISM_MODS),
            new Metal("dark_gem", 800, EVILCRAFT_MODS),
            new Metal("celestine", 800, NODEWORKS_MODS),
            new Metal("dimensional_shard", 1000, FTBMATERIALS_MODS),
            new Metal("tertium", 800, AGRADDITIONS_MODS, "mysticalagradditions:molten_tertium"),
            new Metal("prudentium", 800, AGRADDITIONS_MODS, "mysticalagradditions:molten_prudentium"),
            new Metal("prosperity", 800, AGRADDITIONS_MODS, "mysticalagradditions:molten_prosperity"),
            new Metal("soulium", 800, AGRADDITIONS_MODS, "mysticalagradditions:molten_soulium"),
            new Metal("imperium", 1000, AGRADDITIONS_MODS, "mysticalagradditions:molten_imperium"),
            new Metal("supremium", 1000, AGRADDITIONS_MODS, "mysticalagradditions:molten_supremium"),
            new Metal("awakened_supremium", 1600, AGRADDITIONS_MODS, "mysticalagradditions:molten_awakened_supremium"),
            new Metal("insanium", 1600, AGRADDITIONS_MODS, "mysticalagradditions:molten_insanium"),
            new Metal("stainless_steel", 1000, COLOSSAL_MODS, "colossal_reactors:molten_stainless_steel"),
            new Metal("tough_alloy", 1000, COLOSSAL_MODS, "colossal_reactors:molten_tough_alloy")
    );

    private static int timeFor(int cost) {
        return 80 + (cost * 2) / 9;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Part part : PARTS) {
            futures.add(DataProvider.saveStable(cache, castCreation(part),
                    recipePath.json(id(TOOLS, "casting/casts/" + part.name() + "_cast"))));
            for (Metal metal : METALS) {
                String folder = metal.vanilla() ? metal.name() : "compat/" + metal.name();

                futures.add(DataProvider.saveStable(cache, casting(part, metal),
                        recipePath.json(id(TOOLS, "casting/" + folder + "/" + part.name()))));

                futures.add(DataProvider.saveStable(cache, melting(part, metal),
                        recipePath.json(id(TOOLS, "melting/" + folder + "/" + part.name()))));
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private JsonObject casting(Part part, Metal metal) {
        JsonObject json = new JsonObject();
        addConditions(json, metal.mods());
        json.addProperty("type", FORGE + ":casting_table");

        json.addProperty("cast", TOOLS + ":" + part.name() + "_cast");
        json.addProperty("cast_consumed", false);
        json.add("fluid", fluid(metal.fluid(), part.cost()));
        json.add("result", item(TOOLS + ":" + part.name(), 1));
        json.addProperty("cooling_time", timeFor(part.cost()));
        return json;
    }

    private JsonObject castCreation(Part part) {
        JsonObject json = new JsonObject();
        addConditions(json, NO_MODS);
        json.addProperty("type", FORGE + ":casting_table");

        json.addProperty("cast", TOOLS + ":" + part.name());
        json.addProperty("cast_consumed", true);
        json.add("fluid", fluid(FORGE + ":molten_copper", 90));
        json.add("result", item(TOOLS + ":" + part.name() + "_cast", 1));
        json.addProperty("cooling_time", 100);
        return json;
    }

    private JsonObject melting(Part part, Metal metal) {
        JsonObject json = new JsonObject();
        addConditions(json, metal.mods());
        json.addProperty("type", FORGE + ":melting");

        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("neoforge:ingredient_type", "neoforge:components");
        JsonObject components = new JsonObject();
        components.addProperty(TOOLS + ":part_material", TOOLS + ":" + metal.name());
        ingredient.add("components", components);
        ingredient.addProperty("items", TOOLS + ":" + part.name());
        ingredient.addProperty("strict", false);
        json.add("ingredient", ingredient);

        json.add("result", fluid(metal.fluid(), part.cost()));
        json.add("fuel", fuelForTemp(metal.temp()));
        json.addProperty("temperature", metal.temp());
        json.addProperty("time", timeFor(part.cost()));
        return json;
    }

    private static JsonObject item(String id, int count) {
        JsonObject i = new JsonObject();
        i.addProperty("id", id);
        i.addProperty("count", count);
        return i;
    }

    private static JsonObject fluid(String id, int amount) {
        JsonObject f = new JsonObject();
        f.addProperty("id", id);
        f.addProperty("amount", amount);
        return f;
    }

    private static JsonObject fuelForTemp(int temp) {
        return temp <= 1000 ? fluid("minecraft:lava", 50) : fluid(FORGE + ":molten_blaze", 50);
    }

    private static void addConditions(JsonObject json, List<String> mods) {
        JsonArray conditions = new JsonArray();
        conditions.add(modLoaded(FORGE));
        if (mods.size() == 1) {
            conditions.add(modLoaded(mods.get(0)));
        } else if (mods.size() > 1) {
            JsonArray or = new JsonArray();
            for (String modid : mods) {
                or.add(modLoaded(modid));
            }
            JsonObject orCond = new JsonObject();
            orCond.addProperty("type", "neoforge:or");
            orCond.add("values", or);
            conditions.add(orCond);
        }
        json.add("neoforge:conditions", conditions);
    }

    private static JsonObject modLoaded(String modid) {
        JsonObject c = new JsonObject();
        c.addProperty("type", "neoforge:mod_loaded");
        c.addProperty("modid", modid);
        return c;
    }

    private static Identifier id(String ns, String path) {
        return Identifier.fromNamespaceAndPath(ns, path);
    }

    @Override
    public String getName() {
        return "Hephaestus Tools - Part Casting & Melting";
    }
}