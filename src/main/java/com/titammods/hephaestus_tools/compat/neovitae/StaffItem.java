package com.titammods.hephaestus_tools.compat.neovitae;

import com.titammods.hephaestus_tools.table.MasteryLevel;
import com.titammods.hephaestus_tools.table.ToolMastery;
import com.titammods.hephaestus_tools.tools.item.ModifiableItem;
import com.titammods.hephaestus_tools.tools.item.ToolCategory;
import com.titammods.hephaestus_tools.tools.nbt.ToolStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class StaffItem extends ModifiableItem {
    public static final String HEMOMANCER = "hemomancer";
    public static final String SIGIL_WEAVER = "sigil_weaver";
    public static final String SOUL_REAVER = "soul_reaver";

    private static final String SIGIL_TYPE_KEY = "hephaestus_tools_staff_sigil";
    private static final String SIGIL_ITEM_KEY = "hephaestus_tools_staff_sigil_item";
    private static final String FORM_KEY = "hephaestus_tools_staff_form";
    private static final int USE_DURATION = 72000;

    public static final List<String> FORMS = List.of("bolt", "nova", "drain");

    public StaffItem(Properties props) { super(props); }

    @Override public Set<ToolCategory> categories() { return Set.of(); }
    @Override public float getAttackDamageBonus() { return 1.0f; }

    public static int chargeTicks(ItemStack stack) {
        float speed = ToolStack.isInitialized(stack) ? Math.max(0.5f, ToolStack.getAttackSpeed(stack)) : 1f;
        int ticks = Math.round(30f / speed);
        if (ToolMastery.selected(stack).equals(SIGIL_WEAVER) && MasteryLevel.tier(stack) >= 2) ticks = Math.round(ticks * 0.7f);
        ticks = Math.round(ticks * (1f - 0.15f * StaffModifiers.level(stack, StaffModifiers.QUICKENING)));
        return Mth.clamp(ticks, 6, 40);
    }

    public static String form(ItemStack stack) {
        String form = data(stack).getStringOr(FORM_KEY, "");
        return FORMS.contains(form) ? form : FORMS.getFirst();
    }

    public static String cycleForm(ItemStack stack) {
        String next = FORMS.get((FORMS.indexOf(form(stack)) + 1) % FORMS.size());
        CompoundTag tag = data(stack);
        tag.putString(FORM_KEY, next);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return next;
    }

    public static Component formName(String form) {
        return Component.translatable("tooltip.hephaestus_tools.staff.blood_" + form);
    }

    public static @Nullable Identifier sigilType(ItemStack stack) {
        return Identifier.tryParse(data(stack).getStringOr(SIGIL_TYPE_KEY, ""));
    }

    public static @Nullable Identifier sigilItem(ItemStack stack) {
        return Identifier.tryParse(data(stack).getStringOr(SIGIL_ITEM_KEY, ""));
    }

    public static boolean hasSigil(ItemStack stack) {
        return !data(stack).getStringOr(SIGIL_TYPE_KEY, "").isEmpty();
    }

    public static void setSigil(ItemStack stack, @Nullable Identifier type, @Nullable Identifier item) {
        CompoundTag tag = data(stack);
        if (type == null || item == null) {
            tag.remove(SIGIL_TYPE_KEY);
            tag.remove(SIGIL_ITEM_KEY);
        } else {
            tag.putString(SIGIL_TYPE_KEY, type.toString());
            tag.putString(SIGIL_ITEM_KEY, item.toString());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!ToolStack.isUsable(stack)) return InteractionResult.FAIL;
        if (player.isShiftKeyDown()) {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (level instanceof ServerLevel serverLevel) StaffCasting.imprint(serverLevel, player, stack);
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel) StaffCasting.begin(serverLevel, player);
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            StaffCasting.chargeTick(serverLevel, player, stack, USE_DURATION - ticksRemaining);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            StaffCasting.release(serverLevel, player, stack, USE_DURATION - remainingTime);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        if (ToolStack.isInitialized(stack)) {
            Identifier item = sigilItem(stack);
            Component spell = hasSigil(stack) && item != null
                    ? Component.translatable(BuiltInRegistries.ITEM.getValue(item).getDescriptionId())
                    : formName(form(stack));
            tooltip.accept(Component.translatable("tooltip.hephaestus_tools.staff.spell", spell)
                    .withStyle(ChatFormatting.DARK_RED));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
