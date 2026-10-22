package com.titammods.hephaestus_tools.materials.trait;

import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.effect.DoubleLevelValue;
import com.titammods.hephaestus_tools.effect.EffectOperation;
import com.titammods.hephaestus_tools.effect.ScaledAmount;
import com.titammods.hephaestus_tools.effect.StatType;
import com.titammods.hephaestus_tools.effect.ToolEffect;
import com.titammods.hephaestus_tools.effect.conditions.EquippedCondition;
import com.titammods.hephaestus_tools.effect.conditions.TargetInTagCondition;
import com.titammods.hephaestus_tools.effect.conditions.TargetWetCondition;
import com.titammods.hephaestus_tools.effect.scales.ColdScale;
import com.titammods.hephaestus_tools.effect.scales.DepthScale;
import com.titammods.hephaestus_tools.effect.scales.DurabilityLeftScale;
import com.titammods.hephaestus_tools.effect.scales.TargetArmorScale;
import com.titammods.hephaestus_tools.effect.scales.WearScale;
import com.titammods.hephaestus_tools.effect.types.BonusDamageEffect;
import com.titammods.hephaestus_tools.effect.types.BreakSpeedEffect;
import com.titammods.hephaestus_tools.effect.types.BeheadEffect;
import com.titammods.hephaestus_tools.effect.types.IndestructibleEffect;
import com.titammods.hephaestus_tools.effect.types.PullItemsEffect;
import com.titammods.hephaestus_tools.effect.types.RepairEffect;
import com.titammods.hephaestus_tools.effect.types.StatAddEffect;
import com.titammods.hephaestus_tools.effect.types.StatMultiplyEffect;
import com.titammods.hephaestus_tools.effect.LevelValue;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EquipmentSlotGroup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BuiltInTraits {

    private BuiltInTraits() {}

    public static Map<Identifier, TraitDefinition> definitions() {
        Map<Identifier, TraitDefinition> out = new LinkedHashMap<>();
        add(out, "lightweight",
                new StatMultiplyEffect(StatType.MINING_SPEED, 0.05f),
                new StatAddEffect(StatType.ATTACK_SPEED, 0.5f));
        add(out, "featherweight",
                new StatMultiplyEffect(StatType.MINING_SPEED, 0.07f),
                new StatAddEffect(StatType.ATTACK_SPEED, 0.3f));
        add(out, "solid", new StatMultiplyEffect(StatType.DURABILITY, 0.25f));
        add(out, "dense", new StatMultiplyEffect(StatType.DURABILITY, 0.40f));
        add(out, "heavy",
                new StatMultiplyEffect(StatType.ATTACK_DAMAGE, 0.20f),
                new StatAddEffect(StatType.ATTACK_SPEED, -0.4f));
        add(out, "dwarven", new BreakSpeedEffect(EffectOperation.MULTIPLY,
                ScaledAmount.scaled(0.004f, new DepthScale(0), 0.5f)));
        add(out, "stonebound", new BreakSpeedEffect(EffectOperation.MULTIPLY,
                ScaledAmount.scaled(0.5f, WearScale.INSTANCE)));
        add(out, "maintained", new BreakSpeedEffect(EffectOperation.MULTIPLY,
                ScaledAmount.scaled(0.25f, DurabilityLeftScale.INSTANCE)));
        add(out, "smite", new BonusDamageEffect(EffectOperation.ADD, ScaledAmount.of(2.5f),
                new TargetInTagCondition(EntityTypeTags.SENSITIVE_TO_SMITE)));
        add(out, "jagged", new BonusDamageEffect(EffectOperation.ADD,
                ScaledAmount.scaled(3.0f, WearScale.INSTANCE)));
        add(out, "pierce", new BonusDamageEffect(EffectOperation.ADD,
                ScaledAmount.scaled(0.2f, TargetArmorScale.INSTANCE, 4.0f)));
        add(out, "cultivated", new RepairEffect(40, LevelValue.of(1f), true));
        add(out, "magnetic", new PullItemsEffect(5, DoubleLevelValue.of(5.0), DoubleLevelValue.of(0.12),
                Optional.of(new EquippedCondition(EquipmentSlotGroup.HAND))));
        add(out, "shock", new BonusDamageEffect(EffectOperation.ADD, ScaledAmount.of(2.5f),
                TargetWetCondition.INSTANCE));
        add(out, "temperate",
                new BreakSpeedEffect(EffectOperation.ADD, ScaledAmount.scaled(4.0f, new ColdScale(0.75f))),
                new BonusDamageEffect(EffectOperation.ADD, ScaledAmount.scaled(1.5f, new ColdScale(0.75f))));
        add(out, "unbreakable", IndestructibleEffect.INSTANCE);
        add(out, "beheading", new BeheadEffect(LevelValue.of(0.25f), beheadingHeads(), true));
        return out;
    }

    private static Map<Identifier, Identifier> beheadingHeads() {
        Map<Identifier, Identifier> heads = new LinkedHashMap<>();
        for (String mob : List.of("zombie", "husk", "drowned", "zombie_villager")) heads.put(Identifier.withDefaultNamespace(mob), Identifier.withDefaultNamespace("zombie_head"));
        for (String mob : List.of("skeleton", "stray", "bogged", "parched")) heads.put(Identifier.withDefaultNamespace(mob), Identifier.withDefaultNamespace("skeleton_skull"));
        heads.put(Identifier.withDefaultNamespace("wither_skeleton"), Identifier.withDefaultNamespace("wither_skeleton_skull"));
        heads.put(Identifier.withDefaultNamespace("creeper"), Identifier.withDefaultNamespace("creeper_head"));
        heads.put(Identifier.withDefaultNamespace("piglin"), Identifier.withDefaultNamespace("piglin_head"));
        heads.put(Identifier.withDefaultNamespace("piglin_brute"), Identifier.withDefaultNamespace("piglin_head"));
        heads.put(Identifier.withDefaultNamespace("ender_dragon"), Identifier.withDefaultNamespace("dragon_head"));
        return heads;
    }

    private static void add(Map<Identifier, TraitDefinition> out, String path, ToolEffect... effects) {
        out.put(Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID, path), new TraitDefinition(1, List.of(effects)));
    }
}