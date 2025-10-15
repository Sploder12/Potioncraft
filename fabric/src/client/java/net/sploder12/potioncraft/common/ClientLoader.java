package net.sploder12.potioncraft.common;

import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public interface ClientLoader {
    Optional<Sprite> getFluidStillSprite(World world, BlockPos pos, Fluid fluid);

    int getFluidColor(World world, BlockPos pos, FluidState fluid);
}
