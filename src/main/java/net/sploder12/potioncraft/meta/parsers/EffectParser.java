package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.meta.CauldronData;
import net.sploder12.potioncraft.meta.MetaEffect;
import net.sploder12.potioncraft.meta.templates.MetaEffectTemplate;
import net.sploder12.potioncraft.util.Json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public interface EffectParser {
    public static MetaEffect parseEffect(JsonObject effectObj, String location) {
        String id = Json.getString(effectObj.get("id"));

        if (id == null) {
            Main.warn("id field missing or malformed! " + location);
            return null;
        }

        MetaEffectTemplate template = MetaEffectTemplate.templates.get(id);
        if (template == null) {
            Main.warn(id + " does not name an effect template! " + location);
            return null;
        }

        Optional<ActionResult> quickfail = Json.getActionResult(effectObj.get("quickfail"), location + "-" + id);

        JsonObject params = Json.getObj(effectObj.get("params"));

        if (params == null) {
            params = new JsonObject();
        }

        MetaEffect effect = template.apply(params, location + "-" + id);
        if (quickfail.isPresent()) {

            ActionResult finalQuickfail = quickfail.get();
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (finalQuickfail == prev) {
                    return ActionResult.PASS;
                }

                return effect.interact(prev, data, world, pos, player, hand, stack);
            };
        }
        else {
            return effect;
        }
    }

    public static Collection<MetaEffect> parseEffects(JsonArray effects, String id) {
        ArrayList<MetaEffect> out = new ArrayList<>();

        for (JsonElement elem : effects) {
            if (elem.isJsonObject()) {
                MetaEffect effect = parseEffect(elem.getAsJsonObject(), id);
                if (effect != null) {
                    out.add(effect);
                }
            }
        }

        return out;
    }
}
