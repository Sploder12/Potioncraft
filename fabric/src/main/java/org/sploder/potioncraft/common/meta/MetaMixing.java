package org.sploder.potioncraft.common.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.minecraft.block.BlockState;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.PotionCauldronBlockEntity;
import org.sploder.potioncraft.common.config.Config;
import org.sploder.potioncraft.common.meta.data.HeatMapping;
import org.sploder.potioncraft.common.meta.data.InversionMapping;
import org.sploder.potioncraft.common.meta.parsers.*;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;
import org.sploder.potioncraft.common.util.FluidHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MetaMixing {

    // The logic and parsing for meta mixing files.

    //public static final CauldronBehavior.CauldronBehaviorMap interactions = CauldronBehavior.createMap("potion");
    public static final Map<Item, CauldronBehavior> interactions = CauldronBehavior.createMap();

    public static InversionMapping inversionMapping = null;
    public static HeatMapping heatMapping = null;

    public static final LinkedHashMap<String, BiConsumer<JsonElement, String>> parsers = new LinkedHashMap<>();

    public static CauldronBehavior addInteraction(Item item, Map<Item, CauldronBehavior> behaviorMap, Collection<MetaEffect> effects, boolean keepOld, int potency) {
        CauldronBehavior prevBehavior = behaviorMap.get(item);
        if (prevBehavior == null) {
            keepOld = false;
        }

        final boolean keepOldFinal = keepOld;
        CauldronBehavior behavior = (BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack itemStack) -> {

            CauldronData data = CauldronData.from(state, world, pos);
            if (data == null) {
                if (keepOldFinal) {
                    return prevBehavior.interact(state, world, pos, player, hand, itemStack);
                }
                return ActionResult.PASS;
            }

            int initLevel = data.entity.getLevel();
            var initFluid = data.entity.getFluid();

            int tmpPotency = getTmpPotency(potency, itemStack, data);

            final int maxPotency = PotionCauldronBlockEntity.getMaxPotency();
            final int newPotency = data.entity.getPotency() + tmpPotency;
            if (maxPotency >= 0 && newPotency > maxPotency) {
                return ActionResult.PASS;
            }

            ActionResult prev = ActionResult.success(world.isClient);
            for (var effect : effects) {
                prev = effect.interact(prev, data, world, pos, player, hand, itemStack);
            }

            if (prev != ActionResult.PASS) {
                data.entity.setPotency(newPotency);
            }

            data.transformBlock(world, initLevel, initFluid);

            if (keepOldFinal && prev == ActionResult.PASS) {
                return prevBehavior.interact(state, world, pos, player, hand, itemStack);
            }

            return prev;
        };

        return behaviorMap.put(item, behavior);
    }

    private static int getTmpPotency(int potency, ItemStack itemStack, CauldronData data) {
        int tmpPotency = potency;

        // if using the item's potency, it uses the max of current and held
        if (tmpPotency == -1337) {
            NbtCompound nbt = itemStack.getNbt();
            if (nbt != null && nbt.contains("potency")) {
                tmpPotency = nbt.getInt("potency");
            }
            else {
                tmpPotency = Config.getInteger(Config.FieldID.DEFAULT_POTION_POTENCY);
            }

            int resultPotency = Math.max(tmpPotency, data.entity.getPotency());
            tmpPotency = resultPotency - data.entity.getPotency();
        }
        return tmpPotency;
    }

    public static void register() {
        parsers.clear();

        parsers.put("templates", TemplatesParser::parse);
        parsers.put("fluids", FluidsParser::parse);
        parsers.put("cauldrons", CauldronsParser::parse);

        parsers.put("inversions", ((JsonElement elem, String str) -> {
            inversionMapping = InversionsParser.parse(elem, str);
        }));

        parsers.put("heats", ((JsonElement elem, String str) -> {
            heatMapping = HeatsParser.parse(elem, str);
        }));

        parsers.put("recipes", RecipesParser::parse);
    }

    public static void reload(ResourceManager manager) {
        // Clear Caches Here

        FluidHelper.reset();

        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.clear();
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.clear();
        CauldronBehavior.LAVA_CAULDRON_BEHAVIOR.clear();
        CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR.clear();
        CauldronBehavior.registerBehavior();

        interactions.clear();

        MetaEffectTemplate.register();

        // @TODO clear custom behaviors

        Config.loadConfig(Common.instance.getConfigFile()); // test this

        Map<Identifier, Resource> resources = manager.findResources("metamixing", id -> id.toString().endsWith(".json"));
        resources.forEach((id, resource) -> {
            try (InputStream stream = resource.getInputStream(); JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
                // gson kinda blows ngl

                JsonParser parser = new JsonParser();

                JsonElement rootE = parser.parse(reader);
                if (rootE == null || !rootE.isJsonObject()) {
                    Log.warn("Encountered malformed resource " + id);
                    return;
                }

                JsonObject root = rootE.getAsJsonObject();
                String file = id.toString();

                MetaMixing.parsers.forEach((String elemId, BiConsumer<JsonElement, String> elemParser) ->
                        elemParser.accept(root.get(elemId), file));
            }
            catch (Exception e) {
                Log.error("Error occurred while loading resource " + id + "\n" + e.getMessage());
            }
        });
    }
}
