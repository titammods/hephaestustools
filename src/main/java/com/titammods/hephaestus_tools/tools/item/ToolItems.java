package com.titammods.hephaestus_tools.tools.item;

import com.titammods.hephaestus_tools.tools.aoe.IAoeTool;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import com.titammods.hephaestus_tools.tools.nbt.ToolPropertiesData;
import com.titammods.hephaestus_tools.tools.stat.HarvestTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ToolItems {

    private ToolItems() {}

    public static class PickaxeItem extends ModifiableItem {
        public PickaxeItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.PICKAXE); }
    }

    public static class SledgeHammerItem extends ModifiableItem implements IAoeTool {
        public SledgeHammerItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.PICKAXE); }
        @Override public int aoeWidth(ItemStack s) { return 1; }
        @Override public int aoeHeight(ItemStack s) { return 1; }
    }

    public static class VeinHammerItem extends ModifiableItem implements IAoeTool {
        public VeinHammerItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.PICKAXE); }
        @Override
        public List<BlockPos> getExtraBlocks(Level world, BlockHitResult rt, Player player, ItemStack stack) {
            return veinExtraBlocks(world, rt, player, stack, 2);
        }
    }

    public static class MattockItem extends ModifiableItem {
        public MattockItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.PICKAXE, ToolCategory.SHOVEL); }
    }

    public static class PaxelItem extends ModifiableItem {
        public PaxelItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.PICKAXE, ToolCategory.AXE, ToolCategory.SHOVEL); }
    }

    public static class ExcavatorItem extends ModifiableItem implements IAoeTool {
        public ExcavatorItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.SHOVEL); }
        @Override public int aoeWidth(ItemStack s) { return 1; }
        @Override public int aoeHeight(ItemStack s) { return 1; }
    }

    public static class HandAxeItem extends ModifiableItem {
        public HandAxeItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.AXE); }
    }

    public static class BroadAxeItem extends ModifiableItem implements IAoeTool {
        public BroadAxeItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.AXE); }
        @Override
        public List<BlockPos> getExtraBlocks(Level world, BlockHitResult rt, Player player, ItemStack stack) {
            if (rt != null && rt.getType() == HitResult.Type.BLOCK
                    && world.getBlockState(rt.getBlockPos()).is(BlockTags.LOGS)) {
                return treeExtraBlocks(world, rt, player, stack);
            }
            return IAoeTool.super.getExtraBlocks(world, rt, player, stack);
        }
    }

    public static class KamaItem extends ModifiableItem {
        public KamaItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.HOE); }
    }

    public static class ScytheItem extends ModifiableItem {
        public ScytheItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.HOE); }
    }

    public static class DaggerItem extends ModifiableSwordItem {
        public DaggerItem(Properties props) { super(props); }
        @Override public float getAttackDamageBonus() { return 3.0f; }
        @Override public float getAttackDamageMultiplier() { return 0.65f; }
        @Override public float getBaseAttackSpeed() { return 2.0f; }
    }

    public static class SwordItem extends ModifiableSwordItem {
        public SwordItem(Properties props) { super(props); }
        @Override public float getAttackDamageBonus() { return 3.0f; }
        @Override public float getBaseAttackSpeed() { return 1.6f; }
    }

    public static class MaceItem extends ModifiableItem {
        public MaceItem(Properties props) { super(props); }
        @Override public Set<ToolCategory> categories() { return Set.of(); }
        @Override public float getAttackDamageBonus() { return 4.0f; }
        @Override public float getBaseAttackSpeed() { return 0.6f; }

        private static boolean smashes(LivingEntity attacker) {
            return ToolStack.isUsable(attacker.getWeaponItem()) && net.minecraft.world.item.MaceItem.canSmashAttack(attacker);
        }

        @Override
        public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
            if (!(source.getDirectEntity() instanceof LivingEntity attacker) || !smashes(attacker)) return 0f;
            return vanillaMace().getAttackDamageBonus(victim, damage, source);
        }

        @Override
        public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            boolean smash = smashes(attacker);
            super.hurtEnemy(stack, target, attacker);
            if (smash) vanillaMace().hurtEnemy(stack, target, attacker);
        }

        @Override
        public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            super.postHurtEnemy(stack, target, attacker);
            vanillaMace().postHurtEnemy(stack, target, attacker);
        }

        @Override
        public @Nullable DamageSource getItemDamageSource(LivingEntity attacker) {
            return smashes(attacker) ? vanillaMace().getItemDamageSource(attacker) : super.getItemDamageSource(attacker);
        }

        private static net.minecraft.world.item.MaceItem vanillaMace() {
            return (net.minecraft.world.item.MaceItem) Items.MACE;
        }
    }

    public static class SpearItem extends ModifiableItem {
        private record Profile(float delay, float dismountTime, float dismountThreshold,
                               float knockbackTime, float knockbackThreshold,
                               float damageTime, float damageThreshold) {}

        private static final Profile WOOD      = new Profile(0.75f, 5.0f, 14.0f, 10.0f, 5.1f, 15.0f, 4.6f);
        private static final Profile STONE     = new Profile(0.7f, 4.5f, 13.0f, 9.0f, 5.1f, 13.75f, 4.6f);
        private static final Profile COPPER    = new Profile(0.65f, 4.0f, 12.0f, 8.25f, 5.1f, 12.5f, 4.6f);
        private static final Profile IRON      = new Profile(0.6f, 2.5f, 11.0f, 6.75f, 5.1f, 11.25f, 4.6f);
        private static final Profile GOLD      = new Profile(0.7f, 3.5f, 13.0f, 8.5f, 5.1f, 13.75f, 4.6f);
        private static final Profile DIAMOND   = new Profile(0.5f, 3.0f, 10.0f, 6.5f, 5.1f, 10.0f, 4.6f);
        private static final Profile NETHERITE = new Profile(0.4f, 2.5f, 9.0f, 5.5f, 5.1f, 8.75f, 4.6f);

        public SpearItem(Properties props) { super(vanillaSpear(props)); }
        @Override public Set<ToolCategory> categories() { return Set.of(); }
        @Override public float getBaseAttackSpeed() { return 1.05f; }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (!ToolStack.isUsable(player.getItemInHand(hand))) return InteractionResult.PASS;
            return super.use(level, player, hand);
        }

        private static Properties vanillaSpear(Properties props) {
            return props
                    .delayedHolderComponent(DataComponents.DAMAGE_TYPE, DamageTypes.SPEAR)
                    .component(DataComponents.KINETIC_WEAPON, kinetic(IRON, 0.95f, false))
                    .component(DataComponents.PIERCING_WEAPON, piercing(false))
                    .component(DataComponents.ATTACK_RANGE, new AttackRange(2.0f, 4.5f, 2.0f, 6.5f, 0.125f, 0.5f))
                    .component(DataComponents.MINIMUM_ATTACK_CHARGE, 1.0f)
                    .component(DataComponents.SWING_ANIMATION, swing(1.05f))
                    .component(DataComponents.USE_EFFECTS, new UseEffects(true, false, 1.0f));
        }

        @Override
        public void applyToolComponents(ItemStack stack, ToolPropertiesData properties) {
            HarvestTier tier = properties.getHarvestTier();
            boolean wood = tier.id().getPath().equals("wood");
            float multiplier = 0.7f + 0.125f * Math.max(0f, properties.getAttackDamage() - getAttackDamageBonus());
            stack.set(DataComponents.KINETIC_WEAPON, kinetic(profile(tier), multiplier, wood));
            stack.set(DataComponents.PIERCING_WEAPON, piercing(wood));
            stack.set(DataComponents.SWING_ANIMATION, swing(properties.getAttackSpeed()));
        }

        private static Profile profile(HarvestTier tier) {
            return switch (tier.id().getPath()) {
                case "wood" -> WOOD;
                case "gold" -> GOLD;
                case "copper" -> COPPER;
                default -> switch (Math.max(0, tier.miningLevel())) {
                    case 0 -> WOOD;
                    case 1 -> STONE;
                    case 2 -> IRON;
                    case 3 -> DIAMOND;
                    default -> NETHERITE;
                };
            };
        }

        private static KineticWeapon kinetic(Profile p, float damageMultiplier, boolean wood) {
            return new KineticWeapon(10, (int) (p.delay() * 20.0f),
                    KineticWeapon.Condition.ofAttackerSpeed((int) (p.dismountTime() * 20.0f), p.dismountThreshold()),
                    KineticWeapon.Condition.ofAttackerSpeed((int) (p.knockbackTime() * 20.0f), p.knockbackThreshold()),
                    KineticWeapon.Condition.ofRelativeSpeed((int) (p.damageTime() * 20.0f), p.damageThreshold()),
                    0.38f, damageMultiplier,
                    Optional.of(wood ? SoundEvents.SPEAR_WOOD_USE : SoundEvents.SPEAR_USE),
                    Optional.of(wood ? SoundEvents.SPEAR_WOOD_HIT : SoundEvents.SPEAR_HIT));
        }

        private static PiercingWeapon piercing(boolean wood) {
            return new PiercingWeapon(true, false,
                    Optional.of(wood ? SoundEvents.SPEAR_WOOD_ATTACK : SoundEvents.SPEAR_ATTACK),
                    Optional.of(wood ? SoundEvents.SPEAR_WOOD_HIT : SoundEvents.SPEAR_HIT));
        }

        private static SwingAnimation swing(float attackSpeed) {
            return new SwingAnimation(SwingAnimationType.STAB, Math.max(1, Math.round(20.0f / Math.max(0.1f, attackSpeed))));
        }
    }

    public static class CleaverItem extends ModifiableSwordItem {
        public CleaverItem(Properties props) { super(props); }
        @Override public float getAttackDamageBonus() { return 3.0f; }
        @Override public float getAttackDamageMultiplier() { return 1.5f; }
        @Override public float getBaseAttackSpeed() { return 1.0f; }
    }
}