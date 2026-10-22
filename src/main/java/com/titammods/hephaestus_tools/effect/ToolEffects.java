package com.titammods.hephaestus_tools.effect;

import com.titammods.hephaestus_tools.materials.MaterialId;
import com.titammods.hephaestus_tools.materials.trait.ToolTraits;
import com.titammods.hephaestus_tools.tools.modifier.ModifierStatContext;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class ToolEffects {

    private ToolEffects() {}

    public static List<ActiveEffect> collect(Item tool, List<MaterialId> materials) {
        return ToolTraits.effects(tool, materials);
    }

    public static List<ActiveEffect> collect(ItemStack tool) {
        return collect(tool.getItem(), ToolStack.getMaterials(tool));
    }

    public static void modifyStats(Item tool, List<MaterialId> materials, ModifierStatContext stats) {
        for (ActiveEffect active : collect(tool, materials)) active.effect().modifyStats(stats, active.level());
    }

    public static float modifyBreakSpeed(LivingEntity holder, ItemStack tool, BlockState state, @Nullable BlockPos pos, float speed) {
        if (!ToolStack.isUsable(tool)) return speed;
        EffectContext context = EffectContext.mining(holder, tool, state, pos);
        for (ActiveEffect active : collect(tool)) speed = active.effect().modifyBreakSpeed(context, speed, active.level());
        return speed;
    }

    public static float modifyDamage(LivingEntity attacker, LivingEntity target, ItemStack tool, float amount) {
        if (!ToolStack.isUsable(tool)) return amount;
        EffectContext context = EffectContext.attacking(attacker, tool, target);
        for (ActiveEffect active : collect(tool)) amount = active.effect().modifyDamage(context, amount, active.level());
        return amount;
    }

    public static void afterHit(LivingEntity attacker, LivingEntity target, ItemStack tool, HitDetails hit) {
        if (!ToolStack.isUsable(tool)) return;
        EffectContext context = EffectContext.attacking(attacker, tool, target);
        for (ActiveEffect active : collect(tool)) active.effect().afterHit(context, hit, active.level());
    }

    public static void modifyKillDrops(LivingEntity killer, LivingEntity victim, ItemStack tool, Collection<ItemEntity> drops) {
        if (!ToolStack.isUsable(tool)) return;
        EffectContext context = EffectContext.attacking(killer, tool, victim);
        for (ActiveEffect active : collect(tool)) active.effect().modifyKillDrops(context, victim, drops, active.level());
    }

    public static int modifyWear(LivingEntity holder, ItemStack tool, int amount, WearCause cause) {
        if (!ToolStack.isUsable(tool)) return amount;
        EquipmentSlot slot = holder.getMainHandItem() == tool ? EquipmentSlot.MAINHAND
                : holder.getOffhandItem() == tool ? EquipmentSlot.OFFHAND : null;
        EffectContext context = EffectContext.holding(holder, tool, slot);
        for (ActiveEffect active : collect(tool)) {
            amount = Math.max(0, active.effect().modifyWear(context, amount, cause, active.level()));
        }
        return amount;
    }

    public static float brokenEfficiency(ItemStack tool) {
        float efficiency = 0f;
        for (ActiveEffect active : collect(tool)) efficiency = Math.max(efficiency, active.effect().brokenEfficiency(active.level()));
        return Math.min(1f, efficiency);
    }

    public static int modifyRepair(LivingEntity repairer, ItemStack tool, int amount) {
        if (!ToolStack.isInitialized(tool)) return amount;
        EffectContext context = EffectContext.holding(repairer, tool, null);
        for (ActiveEffect active : collect(tool)) amount = active.effect().modifyRepair(context, amount, active.level());
        return amount;
    }

    public static Optional<ItemUse> itemUse(ItemStack tool) {
        ActiveEffect active = useEffect(tool);
        return active == null ? Optional.empty() : active.effect().itemUse(active.level());
    }

    public static boolean canStartUse(LivingEntity user, ItemStack tool, InteractionHand hand) {
        ActiveEffect active = useEffect(tool);
        return active != null && active.effect().canStartUse(EffectContext.holding(user, tool, hand.asEquipmentSlot()), active.level());
    }

    public static void useTick(LivingEntity user, ItemStack tool, int remaining) {
        ActiveEffect active = useEffect(tool);
        if (active != null) active.effect().useTick(using(user, tool), remaining, active.level());
    }

    public static boolean finishUse(LivingEntity user, ItemStack tool) {
        ActiveEffect active = useEffect(tool);
        if (active == null) return false;
        active.effect().finishUse(using(user, tool), active.level());
        return true;
    }

    private static @Nullable ActiveEffect useEffect(ItemStack tool) {
        if (!ToolStack.isUsable(tool)) return null;
        for (ActiveEffect active : collect(tool)) {
            if (active.effect().itemUse(active.level()).isPresent()) return active;
        }
        return null;
    }

    private static EffectContext using(LivingEntity user, ItemStack tool) {
        return EffectContext.holding(user, tool, user.getUsedItemHand().asEquipmentSlot());
    }

    public static void inventoryTick(ItemStack tool, ServerLevel level, LivingEntity holder, @Nullable EquipmentSlot slot) {
        if (!ToolStack.isInitialized(tool)) return;
        EffectContext context = new EffectContext(level, holder, tool, null, null, null, slot);
        for (ActiveEffect active : collect(tool)) {
            if (ToolStack.isUsable(tool) || active.effect().ticksWhileBroken()) {
                active.effect().inventoryTick(context, active.level());
            }
        }
    }

    public static void modifyDrops(ServerLevel level, LivingEntity breaker, ItemStack tool, BlockState state, BlockPos pos,
                                   List<ItemEntity> drops) {
        if (!ToolStack.isUsable(tool)) return;
        EffectContext context = new EffectContext(level, breaker, tool, null, state, pos, EquipmentSlot.MAINHAND);
        for (ActiveEffect active : collect(tool)) active.effect().modifyDrops(context, drops, active.level());
    }
}