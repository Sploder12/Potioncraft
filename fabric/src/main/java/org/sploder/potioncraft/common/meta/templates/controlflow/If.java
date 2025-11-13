package org.sploder.potioncraft.common.meta.templates.controlflow;

import com.google.gson.JsonArray;
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
import org.sploder.potioncraft.common.meta.MetaMixing;
import org.sploder.potioncraft.common.meta.parsers.EffectParser;
import org.sploder.potioncraft.common.meta.templates.Argument;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;
import org.sploder.potioncraft.common.meta.templates.TemplateResolver;
import org.sploder.potioncraft.common.meta.templates.conditional.Pass;

import java.util.List;
import java.util.Optional;

public class If extends MetaEffectTemplate {
    final TemplateResolver resolver;

    public If(TemplateResolver resolver) {
        super();
        this.resolver = resolver;
    }

    @Argument(key = "condition")
    JsonObject conditionE;

    @Argument(key = "then")
    JsonArray thenE;

    @Argument(key = "else", optional = true)
    JsonArray elseE = null;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "controlflow/if");
    }

    @Override
    public MetaEffect apply(String id) {
        final MetaEffect condition = EffectParser.parseEffect(resolver, conditionE, id + "-condition");
        if (condition == null) {
            return new Pass().apply(id);
        }

        final List<MetaEffect> thens = EffectParser.parseEffects(resolver, thenE, id + "-then");
        if (thens.isEmpty()) {
            Log.warn("IF has empty \"then\" field " + id);
        }

        Optional<List<MetaEffect>> elses = Optional.empty();
        if (elseE != null) {
            elses = Optional.of(EffectParser.parseEffects(resolver, elseE, id + "-else"));

            if (elses.get().isEmpty()) {
                Log.warn("IF has empty \"else\" field " + id);
            }
        }

        final var finalElse = elses;

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            ActionResult cond = condition.interact(prev, data, world, pos, player, hand, stack);

            if (cond != ActionResult.PASS) { // then
                ActionResult res = ActionResult.success(world.isClient);

                for (MetaEffect effect : thens) {
                    res = effect.interact(res, data, world, pos, player, hand, stack);
                }

                return res;
            }
            else if (finalElse.isPresent()) { // else
                ActionResult res = ActionResult.success(world.isClient);

                for (MetaEffect effect : finalElse.get()) {
                    res = effect.interact(res, data, world, pos, player, hand, stack);
                }

                return res;
            }

            return cond; // always ActionResult.PASS
        };
    }
}
