package com.titammods.hephaestus_tools.client;

import com.titammods.hephaestus_tools.tables.block.ArsenalTableBlock;
import com.titammods.hephaestus_tools.tables.block.TablePart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public class ArsenalTableParticles implements IClientBlockExtensions {
    public static final ArsenalTableParticles INSTANCE = new ArsenalTableParticles();

    private static boolean isSecond(BlockState state) {
        return state.getBlock() instanceof ArsenalTableBlock && state.getValue(ArsenalTableBlock.PART) == TablePart.SECOND;
    }

    private static BlockState mainLook(BlockState state) {
        return state.setValue(ArsenalTableBlock.PART, TablePart.MAIN);
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!isSecond(state) || !(level instanceof ClientLevel client)) return false;
        BlockState look = mainLook(state);
        VoxelShape shape = state.getShape(level, pos);
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            double dx = Math.min(1.0, x2 - x1);
            double dy = Math.min(1.0, y2 - y1);
            double dz = Math.min(1.0, z2 - z1);
            int nx = Math.max(2, Mth.ceil(dx / 0.25));
            int ny = Math.max(2, Mth.ceil(dy / 0.25));
            int nz = Math.max(2, Mth.ceil(dz / 0.25));
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    for (int k = 0; k < nz; k++) {
                        double fx = (i + 0.5) / nx;
                        double fy = (j + 0.5) / ny;
                        double fz = (k + 0.5) / nz;
                        manager.add(new TerrainParticle(client,
                                pos.getX() + fx * dx + x1, pos.getY() + fy * dy + y1, pos.getZ() + fz * dz + z1,
                                fx - 0.5, fy - 0.5, fz - 0.5, look, pos).updateSprite(look, pos));
                    }
                }
            }
        });
        return true;
    }

    @Override
    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        if (!isSecond(state) || !(level instanceof ClientLevel client) || !(target instanceof BlockHitResult hit)) return false;
        BlockPos pos = hit.getBlockPos();
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) return true;
        BlockState look = mainLook(state);
        AABB box = shape.bounds();
        RandomSource random = level.getRandom();
        double x = pos.getX() + random.nextDouble() * (box.maxX - box.minX - 0.2) + 0.1 + box.minX;
        double y = pos.getY() + random.nextDouble() * (box.maxY - box.minY - 0.2) + 0.1 + box.minY;
        double z = pos.getZ() + random.nextDouble() * (box.maxZ - box.minZ - 0.2) + 0.1 + box.minZ;
        Direction side = hit.getDirection();
        switch (side) {
            case DOWN -> y = pos.getY() + box.minY - 0.1;
            case UP -> y = pos.getY() + box.maxY + 0.1;
            case NORTH -> z = pos.getZ() + box.minZ - 0.1;
            case SOUTH -> z = pos.getZ() + box.maxZ + 0.1;
            case WEST -> x = pos.getX() + box.minX - 0.1;
            case EAST -> x = pos.getX() + box.maxX + 0.1;
        }
        manager.add(new TerrainParticle(client, x, y, z, 0.0, 0.0, 0.0, look, pos).updateSprite(look, pos).setPower(0.2F).scale(0.6F));
        return true;
    }
}