package org.sploder.potioncraft.common.meta.templates.conditional;

import com.google.gson.JsonArray;
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
import org.sploder.potioncraft.common.meta.templates.TemplateResolver;

// shortcircuit eval &&
public class And extends MetaEffectTemplate {

    protected final TemplateResolver resolver;

    public And(TemplateResolver resolver) {
        super();
        this.resolver = resolver;
    }

    @Argument(key = "short_circuit", optional = true)
    boolean shortCircuit = true;

    @Argument(key = "conditions")
    JsonArray conditions;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "conditional/and");
    }

    @Override
    public MetaEffect apply(String id) {
        final boolean finalSS = shortCircuit;
        final var effects = EffectParser.parseEffects(resolver, conditions, id);
        if (effects.isEmpty()) {
            Log.warn("AND has no conditions! " + id);
            return new Pass().apply(id);
        }

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            boolean success = true;

            for (MetaEffect effect : effects) {
                ActionResult cond = effect.interact(ActionResult.success(world.isClient), data, world, pos, player, hand, stack);

                if (cond == ActionResult.PASS) {
                    success = false;
                    if (finalSS) {
                        break;
                    }
                }
            }

            if (success) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    }
}
