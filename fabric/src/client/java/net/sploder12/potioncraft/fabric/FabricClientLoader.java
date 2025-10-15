package net.sploder12.potioncraft.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.common.ClientLoader;
import net.sploder12.potioncraft.common.Log;

import java.util.Optional;

@Environment(EnvType.CLIENT)
class FabricClientLoader implements ClientLoader {
    @Override
    public Optional<Sprite> getFluidStillSprite(World world, BlockPos pos, Fluid fluid) {
        var fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);

        if (fluidRenderHandler == null) {
            if (fluid != Fluids.EMPTY) {
                Log.warn("Could not create fluid render handler for " + Registries.FLUID.getId(fluid) + "!");
            }

            return Optional.empty();
        }

        return Optional.of(fluidRenderHandler.getFluidSprites(world, pos, fluid.getDefaultState())[0]);
    }

    @Override
    public int getFluidColor(World world, BlockPos pos, FluidState fluid) {
        var fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid.getFluid());

        if (fluidRenderHandler == null) {
            if (fluid.getFluid() != Fluids.EMPTY) {
                Log.warn("Could not create fluid render handler for " + Registries.FLUID.getId(fluid.getFluid()) + "!");
            }

            return -1;
        }

        return fluidRenderHandler.getFluidColor(world, pos, fluid);
    }
}
