package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.AnnotationProcessor;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;
import org.sploder.potioncraft.common.util.Json;

import java.util.ArrayList;
import java.util.List;

public interface EffectParser {
    public static MetaEffect parseEffect(JsonObject effectObj, String location) {
        String id = Json.getString(effectObj.get("id"));

        Log.debug("Parsing " + location + "-" + id);
        if (id == null) {
            Log.warn("id field missing or malformed! " + location);
            return null;
        }

        MetaEffectTemplate template = MetaEffectTemplate.templates.get(id);
        if (template == null) {
            Log.warn(id + " does not name an effect template! " + location);
            return null;
        }

        var quickfail = Json.getActionResult(effectObj.get("quickfail"));

        JsonObject params = Json.getObj(effectObj.get("params"));

        if (params == null) {
            params = new JsonObject();
        }

        try {
            AnnotationProcessor.populateTemplate(template, params);
            MetaEffect effect = template.apply(location);
            if (quickfail != null) {

                final ActionResult finalQuickfail = quickfail;
                return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                    if (finalQuickfail == prev) {
                        return ActionResult.PASS;
                    }

                    return effect.interact(prev, data, world, pos, player, hand, stack);
                };
            } else {
                return effect;
            }
        }
        catch (IllegalArgumentException e) {
            Log.warn(e.getMessage() + ": " + location);
        }
        catch (RuntimeException e) {
            Log.error(e.getMessage() + ": " + location);
        }
        return null;
    }

    private static void parseEffects(JsonArray effects, String id, ArrayList<MetaEffect> out) {
        for (int i = 0; i < effects.size(); ++i) {
            JsonElement elem = effects.get(i);
            String location = id + "-" + i;

            if (elem.isJsonObject()) {
                MetaEffect effect = parseEffect(elem.getAsJsonObject(), location);
                if (effect != null) {
                    out.add(effect);
                }
            }
            else if (elem.isJsonArray()) {
                parseEffects(elem.getAsJsonArray(), location, out);
            }
            else if (elem.isJsonPrimitive()) {
                try {
                    String templateId = elem.getAsString();

                    MetaEffectTemplate template = MetaEffectTemplate.templates.get(templateId);
                    if (template == null) {
                        Log.warn(templateId + " does not name an effect template! " + location);
                        continue;
                    }

                    AnnotationProcessor.populateTemplate(template, new JsonObject());
                    out.add(template.apply(location));
                }
                catch (AssertionError err) {
                    Log.warn("template id malformed! " + location);
                }
                catch (IllegalArgumentException e) {
                    Log.warn(e + ": " + location);
                }
                catch (RuntimeException e) {
                    Log.error(e + ": " + location);
                }
            }
        }
    }

    public static List<MetaEffect> parseEffects(JsonArray effects, String id) {
        ArrayList<MetaEffect> out = new ArrayList<>();

        parseEffects(effects, id, out);

        return out;
    }
}
