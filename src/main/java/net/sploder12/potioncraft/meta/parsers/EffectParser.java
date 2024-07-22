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
    public static Collection<MetaEffect> parseEffects(JsonArray effects, String id) {
        ArrayList<MetaEffect> out = new ArrayList<>();

        for (JsonElement elem : effects) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();

                JsonElement eid = obj.get("id");
                if (eid == null || !eid.isJsonPrimitive()) {
                    Main.log("WARNING: id does not exist in effect " + id);
                    continue;
                }

                JsonPrimitive eprim = eid.getAsJsonPrimitive();
                if (!eprim.isString()) {
                    Main.log("WARNING: effect id must be a string " + id);
                    continue;
                }

                MetaEffectTemplate template = MetaEffectTemplate.templates.get(eprim.getAsString());
                if (template == null) {
                    Main.log("WARNING: " + eprim.getAsString() + " does not name an effect template " + id);
                    continue;
                }

                Optional<ActionResult> quickfail = Json.getActionResult(obj.get("quickfail"));

                JsonObject params = Json.getObj(obj.get("params"));

                if (params == null) {
                    params = new JsonObject();
                }

                MetaEffect effect = template.apply(params, id);
                if (quickfail.isPresent()) {

                    ActionResult finalQuickfail = quickfail.get();
                    out.add((ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                        if (finalQuickfail == prev) {
                            return ActionResult.PASS;
                        }

                        return effect.interact(prev, data, world, pos, player, hand, stack);
                    });
                }
                else {
                    out.add(effect);
                }

            }
        }
        return out;
    }
}
