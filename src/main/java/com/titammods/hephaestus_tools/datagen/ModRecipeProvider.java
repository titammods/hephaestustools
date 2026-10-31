package com.titammods.hephaestus_tools.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.titammods.hephaestus_tools.HephaestusTools;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider implements DataProvider {

    private static final String NS = HephaestusTools.MOD_ID;
    private static final String PATTERN = NS + ":pattern";
    private static final String WOOD = NS + ":wood";

    private final PackOutput.PathProvider recipePath;

    public ModRecipeProvider(PackOutput output) {
        this.recipePath = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    private record Part(String name, int cost) {}

    private static final List<Part> PARTS = List.of(
            new Part("pick_head", 2),
            new Part("hammer_head", 8),
            new Part("small_axe_head", 2),
            new Part("broad_axe_head", 8),
            new Part("adze_head", 2),
            new Part("large_plate", 4),
            new Part("small_blade", 2),
            new Part("large_blade", 8),
            new Part("tool_handle", 1),
            new Part("tough_handle", 3),
            new Part("tool_binding", 1),
            new Part("tough_binding", 3),
            new Part("mace_head", 8)
    );

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Part part : PARTS) {
            futures.add(DataProvider.saveStable(cache, woodStonecutting(part),
                    recipePath.json(id("wood_" + part.name()))));
        }

        futures.add(DataProvider.saveStable(cache, arsenalTable(), recipePath.json(id("arsenal_table"))));
        futures.add(DataProvider.saveStable(cache, pattern(), recipePath.json(id("pattern"))));
        futures.add(DataProvider.saveStable(cache, forgeKey(), recipePath.json(id("forge_key"))));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private JsonObject woodStonecutting(Part part) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:stonecutting");
        json.addProperty("ingredient", PATTERN);

        JsonObject components = new JsonObject();
        components.addProperty(NS + ":part_material", WOOD);

        JsonObject result = new JsonObject();
        result.addProperty("id", NS + ":" + part.name());
        result.addProperty("count", 1);
        result.add("components", components);

        json.add("result", result);
        return json;
    }

    private JsonObject arsenalTable() {
        JsonObject key = new JsonObject();
        key.addProperty("m", "minecraft:anvil");
        key.addProperty("a", "minecraft:smithing_table");
        key.addProperty("s", "#minecraft:planks");
        return shaped(List.of("m  ", "sas", "sss"), key, NS + ":arsenal_table", 1);
    }

    private JsonObject pattern() {
        JsonObject key = new JsonObject();
        key.addProperty("m", "#c:rods/wooden");
        key.addProperty("s", "#minecraft:planks");
        return shaped(List.of("ms", "sm"), key, PATTERN, 1);
    }

    private JsonObject forgeKey() {
        return shapeless(List.of("#c:ingots/gold", "#c:nuggets/gold", "minecraft:tripwire_hook"), NS + ":forge_key", 1);
    }

    private static JsonObject shapeless(List<String> ingredients, String result, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray list = new JsonArray();
        for (String ingredient : ingredients) list.add(ingredient);
        json.add("ingredients", list);

        JsonObject out = new JsonObject();
        out.addProperty("id", result);
        out.addProperty("count", count);
        json.add("result", out);
        return json;
    }

    private static JsonObject shaped(List<String> rows, JsonObject key, String result, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        json.addProperty("category", "misc");

        JsonArray pattern = new JsonArray();
        for (String row : rows) pattern.add(row);
        json.add("pattern", pattern);
        json.add("key", key);

        JsonObject out = new JsonObject();
        out.addProperty("id", result);
        out.addProperty("count", count);
        json.add("result", out);
        return json;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NS, path);
    }

    @Override
    public String getName() {
        return "Hephaestus Tools - Recipes";
    }
}