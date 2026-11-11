package com.titammods.hephaestus_tools.compat.neovitae;

import com.titammods.hephaestus_tools.compat.neovitae.effect.AnimaRepairEffect;
import com.titammods.hephaestus_tools.compat.neovitae.effect.AnimaSiphonEffect;
import com.titammods.hephaestus_tools.compat.neovitae.effect.BloodPactEffect;
import com.titammods.hephaestus_tools.effect.EffectRegistries;

final class NeoVitaeEffects {
    private NeoVitaeEffects() {}

    static void register() {
        EffectRegistries.EFFECT_TYPES.register("anima_repair", () -> AnimaRepairEffect.CODEC);
        EffectRegistries.EFFECT_TYPES.register("anima_siphon", () -> AnimaSiphonEffect.CODEC);
        EffectRegistries.EFFECT_TYPES.register("blood_pact", () -> BloodPactEffect.CODEC);
    }
}
