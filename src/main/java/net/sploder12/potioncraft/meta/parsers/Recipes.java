package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.meta.MetaEffect;
import net.sploder12.potioncraft.meta.MetaMixing;
import net.sploder12.potioncraft.util.DefaultedHashSet;
import net.sploder12.potioncraft.util.FluidHelper;
import net.sploder12.potioncraft.util.Json;

import java.util.Collection;
import java.util.Map;

public interface Recipes {
    private static boolean parseBlockRecipes(JsonObject recipes, String blockId, String id) {
        Identifier bid = Identifier.tryParse(blockId);

        if (bid == null) {
            Main.log("WARNING: " + blockId + " is not an identifier " + id);
            return false;
        }

        Block block = Registries.BLOCK.get(bid);

        if (block instanceof AbstractCauldronBlock cauldronBlock) {
            Map<Item, CauldronBehavior> behaviorMap = MetaMixing.getBehavior(cauldronBlock);

            if (behaviorMap != null) {
                parseBlockRecipes(behaviorMap, recipes, id);
                return true;
            }
        }

        // maybe the id names a fluid?
        Fluid fluid = Registries.FLUID.get(bid);
        if (fluid == Fluids.EMPTY) {
            Main.log("WARNING: " + blockId + " does not have cauldron behavior " + id);
            return false;
        }

        DefaultedHashSet<AbstractCauldronBlock> blocks = FluidHelper.getBlocks(fluid);
        if (blocks == null) {
            Main.log("WARNING: " + blockId + " fluid does not have any associated cauldrons " + id);
            return false;
        }

        blocks.forEach((AbstractCauldronBlock cauldronBlock) -> {
            Map<Item, CauldronBehavior> behavior = MetaMixing.getBehavior(cauldronBlock);
            if (behavior == null) {
                return;
            }

            parseBlockRecipes(behavior, recipes, id);
        });

        return true;
    }

    private static void parseBlockRecipes(Map<Item, CauldronBehavior> behaviorMap, JsonObject recipes, String id) {
        recipes.asMap().forEach((String item, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Main.log("WARNING: " + item + " does not have a JSON object " + id);
                return;
            }

            Identifier idi = Identifier.tryParse(item);
            if (idi == null) {
                Main.log("WARNING: " + item + " is not a valid identifier " + id);
                return;
            }

            Item itemT = Registries.ITEM.get(idi);
            if (itemT == Items.AIR) {
                Main.log("WARNING: " + item + " is not a valid item " + id);
                return;
            }

            parseRecipe(itemT, behaviorMap, elem.getAsJsonObject(), id);
        });
    }

    private static boolean parseRecipe(Item item, Map<Item, CauldronBehavior> behaviorMap, JsonObject recipe, String id) {
        JsonElement effectsObj = recipe.get("effects");
        if (effectsObj == null || !effectsObj.isJsonArray()) {
            Main.log("WARNING: " + item.toString() + " does not have a effects array " + id);
            return false;
        }

        JsonArray effects = effectsObj.getAsJsonArray();
        Collection<MetaEffect> vals = EffectParser.parseEffects(effects, id);
        if (vals.isEmpty()) {
            return false;
        }

        boolean keepOld = Json.getBoolOr(recipe.get("keepOld"), false);

        int potency = Json.getIntOr(recipe.get("potency"), 0);

        CauldronBehavior old = MetaMixing.addInteraction(item, behaviorMap, vals, keepOld, potency);
        return true;
    }

    static void parse(JsonElement recipesE, String file) {
        if (recipesE == null) {
            return;
        }

        if (!recipesE.isJsonObject()) {
            Main.log("WARNING: recipes resource is not an object " + file);
            return;
        }

        JsonObject recipes = recipesE.getAsJsonObject();
        recipes.asMap().forEach((String blockId, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Main.log("WARNING: recipes for " + blockId + " not JSON object " + file);
                return;
            }

            parseBlockRecipes(elem.getAsJsonObject(), blockId, file);
        });
    }
}
