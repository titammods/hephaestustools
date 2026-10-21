package com.titammods.hephaestus_tools.materials;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

public record MaterialAnimation(Type type, int color, int duration, int pause, float width, float strength) {

    public enum Type implements StringRepresentable {
        SWEEP("sweep"),
        PULSE("pulse");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final Codec<MaterialAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.optionalFieldOf("type", Type.SWEEP).forGetter(MaterialAnimation::type),
            Codec.INT.fieldOf("color").forGetter(MaterialAnimation::color),
            Codec.intRange(1, 72000).optionalFieldOf("duration", 24).forGetter(MaterialAnimation::duration),
            Codec.intRange(0, 72000).optionalFieldOf("pause", 0).forGetter(MaterialAnimation::pause),
            Codec.floatRange(0.5f, 64f).optionalFieldOf("width", 3f).forGetter(MaterialAnimation::width),
            Codec.floatRange(0f, 1f).optionalFieldOf("strength", 1f).forGetter(MaterialAnimation::strength)
    ).apply(instance, MaterialAnimation::new));

    public static final StreamCodec<ByteBuf, MaterialAnimation> STREAM_CODEC = StreamCodec.of(
            (buf, a) -> {
                buf.writeByte(a.type.ordinal());
                buf.writeInt(a.color);
                ByteBufCodecs.VAR_INT.encode(buf, a.duration);
                ByteBufCodecs.VAR_INT.encode(buf, a.pause);
                buf.writeFloat(a.width);
                buf.writeFloat(a.strength);
            },
            buf -> new MaterialAnimation(Type.values()[buf.readByte()], buf.readInt(),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), buf.readFloat(), buf.readFloat())
    );

    private float progress(float ticks) {
        float cycle = ticks % (duration + pause);
        return cycle < duration ? cycle / duration : -1f;
    }

    public int pixel(int base, float ticks, int x, int y, int w, int h) {
        if (type == Type.PULSE) return uniform(base, ticks);
        float p = progress(ticks);
        if (p < 0f) return base;
        float span = w + h - 2;
        float diagonal = x + (h - 1 - y);
        float center = -width + p * (span + 2f * width);
        float t = Mth.clamp(1f - Math.abs(diagonal - center) / width, 0f, 1f);
        return mix(base, t * t * (3f - 2f * t) * strength);
    }

    public int uniform(int base, float ticks) {
        float p = progress(ticks);
        if (p < 0f) return base;
        return mix(base, Mth.sin(p * Mth.PI) * strength * (type == Type.PULSE ? 1f : 0.6f));
    }

    private int mix(int base, float t) {
        if (t <= 0f) return base;
        int a = base & 0xFF000000;
        int r = Math.round(Mth.lerp(t, base >> 16 & 255, color >> 16 & 255));
        int g = Math.round(Mth.lerp(t, base >> 8 & 255, color >> 8 & 255));
        int b = Math.round(Mth.lerp(t, base & 255, color & 255));
        return a | r << 16 | g << 8 | b;
    }
}