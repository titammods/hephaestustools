package com.titammods.hephaestus_tools.materials.trait;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

public record ToolTraitDefinition(Identifier tool, List<TraitEntry> traits) {

    public static final Codec<ToolTraitDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("tool").forGetter(ToolTraitDefinition::tool),
            TraitEntry.CODEC.listOf().fieldOf("traits").forGetter(ToolTraitDefinition::traits)
    ).apply(instance, ToolTraitDefinition::new));
}