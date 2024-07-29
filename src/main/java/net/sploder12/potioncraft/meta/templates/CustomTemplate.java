package net.sploder12.potioncraft.meta.templates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.meta.CauldronData;
import net.sploder12.potioncraft.meta.MetaEffect;
import net.sploder12.potioncraft.meta.parsers.EffectParser;
import net.sploder12.potioncraft.util.Json;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CustomTemplate implements MetaEffectTemplate {

    protected static class ParameterEntry {
        public JsonElement defaultValue = null;

        // stores entries to modify, would be cleaner if Java had proper references
        final private ArrayList<Pair<JsonElement, Object>> locations = new ArrayList<>();

        public void add(JsonElement parent, Object idx) {
            locations.add(new Pair<>(parent, idx));
        }

        public void apply(JsonElement value, String file) {
            if (value == null && defaultValue != null) {
                value = defaultValue;
            }

            if (value == null) {
                Main.warn("templated recipe failed to give a value to an argument! " + file);
                return;
            }

            final JsonElement finalValue = value;
            locations.forEach((Pair<JsonElement, Object> entry) -> {
                JsonElement cur = entry.getLeft();
                Object obj = entry.getRight();

                if (cur.isJsonObject() && obj instanceof String idx) {
                    JsonObject object = cur.getAsJsonObject();

                    object.add(idx, finalValue.deepCopy());
                }
                else if (cur.isJsonArray() && obj instanceof Integer idx) {
                    JsonArray arr = cur.getAsJsonArray();
                    arr.set(idx, finalValue.deepCopy());
                }
            });
        }
    }

    final protected String name;
    final protected JsonArray effects;

    final protected HashMap<String, ParameterEntry> parameters;

    protected CustomTemplate(JsonArray arr, String name) {
        this.name = name;
        this.effects = arr.deepCopy();
        this.parameters = new HashMap<>();
    }

    public MetaEffect apply(JsonObject params, String file) {
        final String fileLocation = file;

        try {
            parameters.forEach((String id, ParameterEntry entry) -> {
                JsonElement elem = params.get(id);

                entry.apply(elem, fileLocation + "-" + id);
            });

            final Collection<MetaEffect> effects = EffectParser.parseEffects(this.effects, fileLocation);

            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                for (MetaEffect effect : effects) {
                    prev = effect.interact(prev, data, world, pos, player, hand, stack);
                }

                return prev;
            };
        }
        catch (StackOverflowError err) {
            Main.error("Infinite template detected! Check for recursion! " + fileLocation);
            return Conditional.PASS.apply(params, file);
        }
    }

    protected void add(String id, JsonElement parent, Object idx) {
        ParameterEntry elems = parameters.get(id);
        if (elems == null) {
            elems = new ParameterEntry();
            elems.add(parent, idx);
            parameters.put(id, elems);
        }
        else {
            elems.add(parent, idx);
        }
    }

    @Nullable
    protected static String parseId(String potentialId) {
        if (potentialId != null && potentialId.startsWith("@{") && potentialId.endsWith("}")) {
            return potentialId.substring(2, potentialId.length() - 1);
        }

        return null;
    }

    protected static void parseRecurse(JsonElement element, CustomTemplate out, JsonElement parent, Object idx) {
        if (element.isJsonObject()) {
            element.getAsJsonObject().asMap().forEach((String id, JsonElement elem) ->
                parseRecurse(elem, out, element, id));
        }
        else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();

            for (int i = 0; i < arr.size(); ++i) {
                parseRecurse(arr.get(i), out, element, i);
            }
        }
        else if (element.isJsonPrimitive()) {
            String id = parseId(Json.asString(element));

            if (id != null) {
                out.add(id, parent, idx);
            }
        }
    }

    @Nullable
    public static CustomTemplate parse(JsonObject template, String name, String file) {
        JsonElement effectsE = template.get("effects");

        if (effectsE != null && effectsE.isJsonArray()) {
            JsonArray effects = effectsE.getAsJsonArray();

            CustomTemplate out = new CustomTemplate(effects, name);

            for (JsonElement elem : out.effects) {
                parseRecurse(elem, out, effects, null);
            }

            JsonElement defaultsE = template.get("defaults");

            if (defaultsE != null && defaultsE.isJsonObject()) {
                JsonObject defaults = defaultsE.getAsJsonObject();

                defaults.asMap().forEach((String arg, JsonElement defaultVal) -> {
                    ParameterEntry entry = out.parameters.get(arg);

                    // indicates that the argument is never referenced!
                    if (entry == null) {
                        Main.warn("template " + name + " tries to default unused argument " + arg + " " + file);
                        return;
                    };

                    entry.defaultValue = defaultVal.deepCopy();
                });
            }

            return out;
        }

        Main.warn("template " + name + " has no effects! " + file);
        return null;
    }
}
