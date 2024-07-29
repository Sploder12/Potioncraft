package net.sploder12.potioncraft.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldBlock {
    final public BlockState state;
    final public World world;
    final public BlockPos pos;

    public WorldBlock(BlockState state, World world, BlockPos pos) {
        this.state = state;
        this.world = world;
        this.pos = pos;
    }

    public BlockEntity getBlockEntity() {
        return world.getBlockEntity(pos);
    }

    public Block getBlock() {
        return state.getBlock();
    }
}
