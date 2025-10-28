package org.sploder.potioncraft.common.meta.templates.conditional;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.parsers.EffectParser;
import org.sploder.potioncraft.common.meta.templates.Argument;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

public class Not implements MetaEffectTemplate {

    @Argument(key = "condition")
    JsonObject condition;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "conditional/NOT");
    }

    @Override
    public MetaEffect apply(String id) {
        final var effect = EffectParser.parseEffect(condition, id + "-condition");
        if (effect == null) {
            Log.warn("NOT has bad condition! " + id);
            return new Pass().apply(id);
        }

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            ActionResult res = effect.interact(prev, data, world, pos, player, hand, stack);
            if (res == ActionResult.PASS) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    }
}
