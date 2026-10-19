package com.titammods.hephaestus_tools.effect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;

import java.util.List;

public record DoubleLevelValue(List<Double> values) {

    public static final Codec<DoubleLevelValue> CODEC = Codec.either(Codec.DOUBLE, Codec.DOUBLE.listOf(1, Integer.MAX_VALUE))
            .xmap(either -> either.map(DoubleLevelValue::of, DoubleLevelValue::new),
                    value -> value.values().size() == 1 ? Either.left(value.values().getFirst()) : Either.right(value.values()));

    public DoubleLevelValue {
        values = List.copyOf(values);
    }

    public static DoubleLevelValue of(double value) {
        return new DoubleLevelValue(List.of(value));
    }

    public double get(int level) {
        return values.get(Mth.clamp(level, 1, values.size()) - 1);
    }
}
