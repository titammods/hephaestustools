package com.titammods.hephaestus_tools.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifierModelRegistry {

    private ModifierModelRegistry() {}

    private static final Map<ResourceLocation, ResourceLocation> TEXTURE_OVERRIDES = new HashMap<>();
    private static final Map<String, List<String>> FALLBACKS = Map.of(
            "looting", List.of("fortune"),
            "silk_touch", List.of("silky"),
            "silky", List.of("silk_touch"));

    public static List<ResourceLocation> candidates(Item toolItem, ResourceLocation modifierId) {
        List<ResourceLocation> out = new ArrayList<>();
        ResourceLocation main = getTexture(toolItem, modifierId);
        if (main != null) out.add(main);
        for (String alias : FALLBACKS.getOrDefault(modifierId.getPath(), List.of())) {
            ResourceLocation tex = getTexture(toolItem, ResourceLocation.fromNamespaceAndPath(modifierId.getNamespace(), alias));
            if (tex != null) out.add(tex);
        }
        return out;
    }

    @Nullable
    public static ResourceLocation getTexture(Item toolItem, ResourceLocation modifierId) {
        ResourceLocation override = TEXTURE_OVERRIDES.get(modifierId);
        if (override != null) {
            return override;
        }

        ResourceLocation toolId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(toolItem);
        if (toolId == null) {
            return null;
        }

        String toolPath = toolId.getPath();
        String modifierSuffix = modifierId.getNamespace() + "_" + modifierId.getPath();

        return ResourceLocation.fromNamespaceAndPath(
                "hephaestus_tools",
                "item/tool/" + toolPath + "/modifiers/" + modifierSuffix
        );
    }

    public static void registerOverride(ResourceLocation modifierId, ResourceLocation texture) {
        TEXTURE_OVERRIDES.put(modifierId, texture);
    }
}
