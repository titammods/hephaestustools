package com.titammods.hephaestus_tools.tables.block;

import com.mojang.serialization.MapCodec;
import com.titammods.hephaestus_tools.registry.ModSounds;
import com.titammods.hephaestus_tools.registry.ModItems;
import com.titammods.hephaestus_tools.tables.menu.ArsenalTableMenu;
import com.titammods.hephaestus_tools.tables.menu.ForgeStorageMenu;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import java.util.ArrayList;
import java.util.List;
import com.titammods.hephaestus_tools.table.TableStyle;
import com.titammods.hephaestus_tools.tables.blockentity.ArsenalTableBlockEntity;
import com.titammods.hephaestus_tools.tables.scene.WorkbenchOrigin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class ArsenalTableBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<TablePart> PART = EnumProperty.create("part", TablePart.class);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final MapCodec<ArsenalTableBlock> CODEC = simpleCodec(ArsenalTableBlock::new);
    private static final double[][] MAIN_BOXES = {
            {0, 0, 1, 3, 13, 4}, {0, 0, 12, 3, 13, 15}, {12, 0, 1, 15, 13, 4}, {12, 0, 12, 15, 13, 15},
            {1, 2, 2, 14, 13, 14}, {0, 13, 0, 16, 16, 16},
            {5, 16, 4, 11, 18, 12}, {6, 18, 5, 10, 20, 11}, {5, 20, 3, 11, 24, 13}};
    private static final double[][] SECOND_BOXES = {
            {1, 0, 1, 4, 13, 4}, {1, 0, 12, 4, 13, 15},
            {2, 2, 2, 16, 4, 14}, {3, 4, 13, 16, 13, 14}, {1, 13, 0, 16, 16, 16}};
    private static final Map<Direction, VoxelShape> MAIN_SHAPES = shapes(MAIN_BOXES), SECOND_SHAPES = shapes(SECOND_BOXES);

    private static Map<Direction, VoxelShape> shapes(double[][] boxes) {
        Map<Direction, VoxelShape> out = new EnumMap<>(Direction.class);
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int turns = 0; turns < 4; turns++) {
            VoxelShape shape = Shapes.empty();
            for (double[] b : boxes) {
                double x1 = b[0], z1 = b[2], x2 = b[3], z2 = b[5];
                for (int t = 0; t < turns; t++) {
                    double nx1 = 16 - z2, nx2 = 16 - z1, nz1 = x1, nz2 = x2;
                    x1 = nx1; x2 = nx2; z1 = nz1; z2 = nz2;
                }
                shape = Shapes.or(shape, Block.box(x1, b[1], z1, x2, b[4], z2));
            }
            out.put(facings[turns], shape.optimize());
        }
        return out;
    }

    public ArsenalTableBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(PART, TablePart.MAIN).setValue(OPEN, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == TablePart.MAIN ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return (s.getValue(PART) == TablePart.MAIN ? MAIN_SHAPES : SECOND_SHAPES).get(s.getValue(FACING));
    }

    @Override
    protected VoxelShape getVisualShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) { return true; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, PART, OPEN);
    }

    private static Direction secondDir(Direction facing) { return facing.getCounterClockWise(); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        BlockPos second = ctx.getClickedPos().relative(secondDir(facing));
        Level level = ctx.getLevel();
        if (level.getBlockState(second).canBeReplaced(ctx) && level.getWorldBorder().isWithinBounds(second))
            return this.defaultBlockState().setValue(FACING, facing).setValue(PART, TablePart.MAIN).setValue(OPEN, false);
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            BlockPos second = pos.relative(secondDir(facing));
            level.setBlock(second, state.setValue(PART, TablePart.SECOND), 3);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

    private static BlockPos mainPos(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return state.getValue(PART) == TablePart.MAIN ? pos : pos.relative(secondDir(facing).getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == TablePart.MAIN ? new ArsenalTableBlockEntity(pos, state) : null;
    }

    private static BlockPos secondPos(BlockPos mainPos, BlockState mainState) {
        return mainPos.relative(secondDir(mainState.getValue(FACING)));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.FORGE_KEY.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos mp = mainPos(state, pos);
        BlockState main = level.getBlockState(mp);
        if (!main.is(this) || !(level.getBlockEntity(mp) instanceof ArsenalTableBlockEntity arsenal)) return InteractionResult.FAIL;
        boolean open = !main.getValue(OPEN);
        if (open && tableInUse(level, arsenal)) {
            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.8F, 1.0F);
            return InteractionResult.FAIL;
        }
        if (!open) closeStorageViewers(level, arsenal);
        level.setBlock(mp, main.setValue(OPEN, open), 3);
        BlockPos sp = secondPos(mp, main);
        BlockState second = level.getBlockState(sp);
        if (second.is(this) && second.getValue(PART) == TablePart.SECOND) level.setBlock(sp, second.setValue(OPEN, open), 3);
        if (!open) arsenal.compactStorage();
        arsenal.setChanged();
        level.playSound(null, mp, open ? ModSounds.ARSENAL_TABLE_OPEN.get() : ModSounds.ARSENAL_TABLE_CLOSE.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
        level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, mp);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean tableInUse(Level level, ArsenalTableBlockEntity arsenal) {
        for (Player other : level.players())
            if (other.containerMenu instanceof ArsenalTableMenu menu && menu.getBlockEntity() == arsenal) return true;
        return false;
    }

    private static void closeStorageViewers(Level level, ArsenalTableBlockEntity arsenal) {
        for (Player other : List.copyOf(level.players()))
            if (other.containerMenu instanceof ForgeStorageMenu menu && menu.getBlockEntity() == arsenal) other.closeContainer();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockPos mp = mainPos(state, pos);
            BlockState main = level.getBlockState(mp);
            if (main.is(this) && main.getValue(OPEN) && level.getBlockEntity(mp) instanceof ArsenalTableBlockEntity arsenal) {
                final BlockPos storagePos = mp;
                player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ForgeStorageMenu(id, inv, arsenal),
                        arsenal.getDisplayName()), buf -> buf.writeBlockPos(storagePos));
                level.playSound(null, pos, ModSounds.ARSENAL_TABLE_OPEN.get(), SoundSource.BLOCKS, 0.5F, 1.2F);
                return InteractionResult.SUCCESS;
            }
            if (level.getBlockEntity(mp) instanceof ArsenalTableBlockEntity arsenal) {
                final BlockPos openPos = mp;
                final boolean modern = TableStyle.isModern(player);
                final WorkbenchOrigin origin = WorkbenchOrigin.of(player);
                player.openMenu(arsenal, buf -> {
                    buf.writeBlockPos(openPos);
                    if (modern) origin.write(buf);
                });
                level.playSound(null, pos, ModSounds.ARSENAL_TABLE_OPEN.get(), SoundSource.BLOCKS, 0.7F, 1.0F);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos mp = mainPos(state, pos);
            if (level.getBlockEntity(mp) instanceof ArsenalTableBlockEntity arsenal) {
                if (player.preventsBlockDrops()) {
                    arsenal.markNoDrops();
                    if (arsenal.hasStoredItems()) {
                        ItemStack item = new ItemStack(this);
                        item.applyComponents(arsenal.collectComponents());
                        ItemEntity entity = new ItemEntity(level, mp.getX() + 0.5, mp.getY() + 0.5, mp.getZ() + 0.5, item);
                        entity.setDefaultPickUpDelay();
                        level.addFreshEntity(entity);
                        arsenal.clearStorage();
                    }
                } else if (arsenal.hasStoredItems() && !player.hasCorrectToolForDrops(level.getBlockState(mp))) {
                    Containers.dropContents(level, mp, arsenal.getStorage());
                    arsenal.clearStorage();
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(PART) != TablePart.MAIN) return List.of();
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ArsenalTableBlockEntity arsenal && arsenal.hasStoredItems()) {
            boolean stored = false;
            for (ItemStack drop : drops) {
                if (!stored && drop.is(this.asItem())) {
                    drop.applyComponents(arsenal.collectComponents());
                    stored = true;
                }
            }
            if (!stored) {
                ItemStack item = new ItemStack(this);
                item.applyComponents(arsenal.collectComponents());
                drops = new ArrayList<>(drops);
                drops.add(item);
            }
        }
        return drops;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        TablePart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        BlockPos otherPos = part == TablePart.MAIN
                ? pos.relative(secondDir(facing))
                : pos.relative(secondDir(facing).getOpposite());
        BlockState other = level.getBlockState(otherPos);
        if (!other.is(this) || other.getValue(PART) == part) return;
        if (part == TablePart.SECOND && level.getBlockEntity(otherPos) instanceof ArsenalTableBlockEntity arsenal) {
            level.destroyBlock(otherPos, !arsenal.noDrops());
        } else {
            level.removeBlock(otherPos, false);
        }
    }
}