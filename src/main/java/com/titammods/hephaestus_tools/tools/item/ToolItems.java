package com.titammods.hephaestus_tools.tools.item;

import com.titammods.hephaestus_tools.tools.aoe.IAoeTool;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
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
        @Override public Set<ToolCategory> categories() { return Set.of(ToolCategory.AXE, ToolCategory.SHOVEL); }
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

    public static class CleaverItem extends ModifiableSwordItem {
        public CleaverItem(Properties props) { super(props); }
        @Override public float getAttackDamageBonus() { return 3.0f; }
        @Override public float getAttackDamageMultiplier() { return 1.5f; }
        @Override public float getBaseAttackSpeed() { return 1.0f; }
    }
}
