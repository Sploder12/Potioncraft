package net.sploder12.potioncraft.common.meta.templates;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.common.Log;
import net.sploder12.potioncraft.common.meta.CauldronData;
import net.sploder12.potioncraft.common.meta.MetaEffect;
import net.sploder12.potioncraft.common.meta.parsers.EffectParser;

import java.util.Collection;
import java.util.Optional;

public interface ControlFlow {

    // if statement that has a condition, then, and else
    MetaEffectTemplate IF = (params, file) -> {
        JsonElement conditionE = params.get("condition");

        if (conditionE == null || !conditionE.isJsonObject()) {
            Log.warn("IF has no condition! " + file);
            return Conditional.PASS.apply(params, file);
        }

        final MetaEffect condition = EffectParser.parseEffect(conditionE.getAsJsonObject(), file + "-condition");
        if (condition == null) {
            return Conditional.PASS.apply(params, file);
        }

        JsonElement thenE = params.get("then");

        if (thenE == null || !thenE.isJsonArray()) {
            Log.warn("IF must have a \"then\" effects array field! " + file);
            return Conditional.PASS.apply(params, file);
        }

        final Collection<MetaEffect> thens = EffectParser.parseEffects(thenE.getAsJsonArray(), file + "-then");
        if (thens.isEmpty()) {
            Log.warn("IF has empty \"then\" field " + file);
        }

        JsonElement elseE = params.get("else");
        Optional<Collection<MetaEffect>> elses = Optional.empty();

        if (elseE != null && elseE.isJsonArray()) {
            elses = Optional.of(EffectParser.parseEffects(elseE.getAsJsonArray(), file + "-else"));

            if (elses.get().isEmpty()) {
                Log.warn("IF has empty \"else\" field " + file);
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
    };
}
