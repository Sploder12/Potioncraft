package org.sploder.potioncraft.common.meta.templates.conditional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

// hand swinging is controlled by the LAST event, thus why FORCE_SWING_HAND exists.
// FORCE_SWING_HAND can also be used to generate a guaranteed SUCCESS
public class ForceSwingHand extends MetaEffectTemplate {
    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "conditional/force_swing_hand");
    }

    @Override
    public MetaEffect apply(String id) {
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.success(world.isClient);
    }
}
