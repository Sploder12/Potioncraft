package net.sploder12.potioncraft.mixin;

import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.fluid.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LeveledCauldronBlock.class)
public
interface LeveledAccessor {

    // Easiest way to check if a leveled cauldron is filled with not water.

    @Invoker
    boolean callCanBeFilledByDripstone(Fluid fluid);
}
