package com.titammods.hephaestus_tools.materials;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record MaterialId(Identifier id) {

    public static final MaterialId EMPTY = new MaterialId(Identifier.withDefaultNamespace("empty"));

    public static final Codec<MaterialId> CODEC = Identifier.CODEC.xmap(MaterialId::new, MaterialId::id);

    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialId> STREAM_CODEC =
            StreamCodec.of(
                    (buf, mat) -> buf.writeIdentifier(mat.id()),
                    buf -> new MaterialId(buf.readIdentifier())
            );

    public static MaterialId of(String namespace, String path) {
        return new MaterialId(Identifier.fromNamespaceAndPath(namespace, path));
    }

    public static MaterialId of(String id) {
        return new MaterialId(Identifier.parse(id));
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public String translationKey() {
        return id.toLanguageKey("material");
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
