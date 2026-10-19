package com.titammods.hephaestus_tools.table;

import com.titammods.hephaestus_tools.api.event.ToolXpEvent;
import com.titammods.hephaestus_tools.tools.item.ModifiableItem;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.NeoForge;

public final class ToolXp {

    private ToolXp() {}

    public static final int MAX_LEVEL = 30;
    private static final String XP_KEY = "hephaestus_tools_xp";

    public static int xpToNext(int level) { return 100 + level * 20; }

    public static int getXp(ItemStack tool) {
        return tool.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(XP_KEY, 0);
    }

    private static void setXp(ItemStack tool, int xp) {
        CompoundTag tag = tool.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(XP_KEY, Math.max(0, xp));
        tool.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getLevel(ItemStack tool) {
        int xp = getXp(tool), lvl = 0;
        while (lvl < MAX_LEVEL && xp >= xpToNext(lvl)) { xp -= xpToNext(lvl); lvl++; }
        return lvl;
    }

    public static int xpIntoLevel(ItemStack tool) {
        int xp = getXp(tool), lvl = 0;
        while (lvl < MAX_LEVEL && xp >= xpToNext(lvl)) { xp -= xpToNext(lvl); lvl++; }
        return lvl >= MAX_LEVEL ? 0 : xp;
    }

    public static int xpForLevel(ItemStack tool) {
        int lvl = getLevel(tool);
        return lvl >= MAX_LEVEL ? 1 : xpToNext(lvl);
    }

    public static void addXp(ItemStack tool, ServerPlayer player, int amount) {
        if (amount <= 0 || !(tool.getItem() instanceof ModifiableItem) || !ToolStack.isInitialized(tool)) return;
        int oldLvl = getLevel(tool);
        if (oldLvl >= MAX_LEVEL) return;
        ToolXpEvent event = NeoForge.EVENT_BUS.post(new ToolXpEvent(tool, player, amount));
        if (event.isCanceled() || event.getAmount() <= 0) return;
        setXp(tool, getXp(tool) + event.getAmount());
        int newLvl = getLevel(tool);
        if (newLvl > oldLvl) onLevelUp(tool, player, oldLvl, newLvl);
    }

    private static void onLevelUp(ItemStack tool, ServerPlayer p, int from, int to) {
        ToolStack.recalculate(tool);
        p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.5f);
        p.sendSystemMessage(Component.translatable("msg.hephaestus_tools.levelup",
                tool.getHoverName(), to, Component.translatable(rankKey(to))));
        if (from < 10 && to >= 10) p.sendSystemMessage(Component.translatable("msg.hephaestus_tools.mastery_unlock"));
        if (from < 20 && to >= 20) p.sendSystemMessage(Component.translatable("msg.hephaestus_tools.mastery_evolve"));
        if (from < 30 && to >= 30) p.sendSystemMessage(Component.translatable("msg.hephaestus_tools.mastery_final"));
    }

    public static String rankKey(int level) {
        String r = level >= 30 ? "master" : level >= 25 ? "veteran" : level >= 20 ? "expert"
                : level >= 15 ? "skilled" : level >= 10 ? "adept" : level >= 5 ? "apprentice" : "novice";
        return "rank.hephaestus_tools." + r;
    }
}