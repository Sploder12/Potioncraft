package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.PotionCauldronBlock;
import org.sploder.potioncraft.common.meta.MetaMixing;
import org.sploder.potioncraft.common.meta.templates.TemplateResolver;
import org.sploder.potioncraft.common.mixin.BehaviorAccessor;
import org.sploder.potioncraft.common.util.DefaultedHashSet;
import org.sploder.potioncraft.common.util.FluidHelper;
import org.sploder.potioncraft.common.util.Json;

import java.util.Map;

public interface RecipesParser {
    private static boolean parseBlockRecipes(TemplateResolver resolver, JsonObject recipes, String blockId, String id) {
        Identifier bid = Identifier.tryParse(blockId);

        if (bid == null) {
            Log.warn(blockId + " is not an identifier " + id);
            return false;
        }

        var block = Registries.BLOCK.get(bid);

        if (block instanceof AbstractCauldronBlock cauldronBlock) {
            var behaviorMap = PotionCauldronBlock.getBehaviorMap(cauldronBlock);

            if (behaviorMap != null) {
                parseBlockRecipes(resolver, behaviorMap, recipes, id);
                return true;
            }
        }

        // maybe the id names a fluid?
        var fluid = Registries.FLUID.get(bid);
        if (fluid == Fluids.EMPTY) {
            Log.warn(blockId + " does not have cauldron behavior " + id);
            return false;
        }

        DefaultedHashSet<AbstractCauldronBlock> blocks = FluidHelper.getBlocks(fluid);
        if (blocks == null) {
            Log.warn(blockId + " fluid does not have any associated cauldrons " + id);
            return false;
        }

        blocks.forEach((AbstractCauldronBlock cauldronBlock) -> {
            var behavior = PotionCauldronBlock.getBehaviorMap(cauldronBlock);
            if (behavior == null) {
                return;
            }

            parseBlockRecipes(resolver, behavior, recipes, id + "[" + Registries.BLOCK.getId(cauldronBlock) + "]");
        });

        return true;
    }

    private static void parseBlockRecipes(TemplateResolver resolver, Map<Item, CauldronBehavior> behaviorMap, JsonObject recipes, String id) {
        recipes.asMap().forEach((String item, JsonElement elem) -> {

            if (!elem.isJsonObject()) {
                Log.warn(item + " does not have a JSON object " + id);
                return;
            }

            Identifier idi = Identifier.tryParse(item);
            if (idi == null) {
                Log.warn(item + " is not a valid identifier " + id);
                return;
            }

            var itemT = Registries.ITEM.get(idi);
            if (itemT == Items.AIR) {
                Log.warn(item + " is not a valid item " + id);
                return;
            }

            parseRecipe(resolver, itemT, behaviorMap, elem.getAsJsonObject(), id + "-" + item);
        });
    }

    private static boolean parseRecipe(TemplateResolver resolver, Item item, Map<Item, CauldronBehavior> behaviorMap, JsonObject recipe, String id) {
        JsonElement effectsObj = recipe.get("effects");
        if (effectsObj == null || !effectsObj.isJsonArray()) {
            Log.warn("effects array not present! " + id);
            return false;
        }

        JsonArray effects = effectsObj.getAsJsonArray();
        var vals = EffectParser.parseEffects(resolver, effects, id);
        if (vals.isEmpty()) {
            return false;
        }

        boolean keepOld = Json.getBoolOr(recipe.get("keepOld"), false);

        int potency = Json.getIntOr(recipe.get("potency"), 0);

        var old = MetaMixing.addInteraction(item, behaviorMap, vals, keepOld, potency);
        return true;
    }

    static void parse(TemplateResolver resolver, JsonElement recipesE, String file) {
        if (recipesE == null) {
            Log.debug("recipes resource not present " + file);
            return;
        }

        if (!recipesE.isJsonObject()) {
            Log.warn("recipes resource is not an object " + file);
            return;
        }

        JsonObject recipes = recipesE.getAsJsonObject();
        recipes.asMap().forEach((String blockId, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Log.warn("recipes for " + blockId + " not JSON object " + file);
                return;
            }

            parseBlockRecipes(resolver, elem.getAsJsonObject(), blockId, file + "-" + blockId);
        });
    }
}
