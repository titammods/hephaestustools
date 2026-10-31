package com.titammods.hephaestus_tools.client.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModifierModelRegistry {

    private ModifierModelRegistry() {}

    private static final Map<Identifier, Identifier> TEXTURE_OVERRIDES = new HashMap<>();
    private static final Map<String, List<String>> FALLBACKS = Map.of(
            "looting", List.of("fortune"),
            "silk_touch", List.of("silky"),
            "silky", List.of("silk_touch"));

    public static List<Identifier> candidates(Item toolItem, Identifier modifierId) {
        List<Identifier> out = new ArrayList<>();
        Identifier main = getTexture(toolItem, modifierId);
        if (main != null) out.add(main);
        for (String alias : FALLBACKS.getOrDefault(modifierId.getPath(), List.of())) {
            Identifier tex = getTexture(toolItem, Identifier.fromNamespaceAndPath(modifierId.getNamespace(), alias));
            if (tex != null) out.add(tex);
        }
        return out;
    }

    @Nullable
    public static Identifier getTexture(Item toolItem, Identifier modifierId) {
        Identifier override = TEXTURE_OVERRIDES.get(modifierId);
        if (override != null) {
            return override;
        }

        Identifier toolId = BuiltInRegistries.ITEM.getKey(toolItem);
        if (toolId == null) {
            return null;
        }

        String toolPath = toolId.getPath();
        String modifierSuffix = modifierId.getNamespace() + "_" + modifierId.getPath();

        return Identifier.fromNamespaceAndPath(
                "hephaestus_tools",
                "item/tool/" + toolPath + "/modifiers/" + modifierSuffix
        );
    }

    public static void registerOverride(Identifier modifierId, Identifier texture) {
        TEXTURE_OVERRIDES.put(modifierId, texture);
    }
}