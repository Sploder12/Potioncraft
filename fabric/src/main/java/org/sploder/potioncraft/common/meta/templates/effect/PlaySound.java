package org.sploder.potioncraft.common.meta.templates.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.Argument;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

public class PlaySound extends MetaEffectTemplate {
    // sound to play
    @Argument(key = "id")
    SoundEvent sound;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/play_sound");
    }

    @Override
    public MetaEffect apply(String id) {
        final var finalSound = sound;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (!world.isClient) {
                world.playSound(null, pos, finalSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return ActionResult.success(world.isClient);
        };
    }
}
