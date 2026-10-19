package com.titammods.hephaestus_tools.tools.helper;

import com.titammods.hephaestus_tools.event.MasteryEvents;
import com.titammods.hephaestus_tools.event.ToolXpEvents;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

public final class ToolCombat {
    private record Hit(LivingEntity target, DamageSource source, ItemStack tool, boolean critical) {}
    private record Swing(Player player, Entity target, boolean critical) {}
    private static final ThreadLocal<Hit> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SECONDARY = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Swing> SWING = new ThreadLocal<>();

    private ToolCombat() {}

    public static void startAttack() {
        SWING.remove();
    }

    public static void critical(Player player, Entity target, boolean critical) {
        SWING.set(new Swing(player, target, critical));
    }

    public static boolean hurt(LivingEntity target, DamageSource source, BooleanSupplier action) {
        Hit previous = CURRENT.get();
        ItemStack tool = ItemStack.EMPTY;
        boolean critical = false;
        if (!SECONDARY.get() && source.is(DamageTypes.PLAYER_ATTACK)
                && source.getEntity() instanceof Player player && source.getDirectEntity() == player
                && ToolStack.isUsable(player.getMainHandItem())) {
            tool = player.getMainHandItem();
            Swing swing = SWING.get();
            if (swing != null && swing.player() == player && swing.target() == target) {
                critical = swing.critical();
                SWING.remove();
            }
        }
        CURRENT.set(new Hit(target, source, tool, critical));
        try {
            return action.getAsBoolean();
        } finally {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
    }

    public static void killed(LivingEntity target, DamageSource source) {
        ItemStack tool = tool(target, source);
        if (!tool.isEmpty() && source.getEntity() instanceof ServerPlayer player) {
            ToolXpEvents.onMeleeKill(tool, player);
            MasteryEvents.onMeleeKill(tool, player);
        }
    }

    public static ItemStack tool(LivingEntity target, DamageSource source) {
        Hit hit = CURRENT.get();
        return hit != null && hit.target == target && hit.source == source && !SECONDARY.get()
                ? hit.tool : ItemStack.EMPTY;
    }

    public static boolean isCritical(LivingEntity target, DamageSource source) {
        Hit hit = CURRENT.get();
        return hit != null && hit.target == target && hit.source == source && !SECONDARY.get() && hit.critical;
    }

    public static void secondary(Runnable damage) {
        boolean previous = SECONDARY.get();
        SECONDARY.set(true);
        try {
            damage.run();
        } finally {
            if (previous) SECONDARY.set(true);
            else SECONDARY.remove();
        }
    }
}
