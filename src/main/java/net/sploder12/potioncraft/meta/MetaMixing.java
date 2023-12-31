package net.sploder12.potioncraft.meta;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MetaMixing {

    // The logic and parsing for meta mixing files.

    // all the possible effects a meta file is capable of
    public static final HashMap<String, MetaEffectTemplate> templates = new HashMap<>();

    public static final Map<Item, CauldronBehavior> interactions = CauldronBehavior.createMap();

    public static final HashMap<StatusEffect, StatusEffect> inversions = new HashMap<>();

    public static void addMutualInversion(StatusEffect first, StatusEffect second) {
        inversions.put(first, second);
        inversions.put(second, first);
    }

    public static void addInversion(StatusEffect from, StatusEffect to) {
        inversions.put(from, to);
    }


    public static HashMap<Identifier, Map<Item, CauldronBehavior>> customBehaviors = new HashMap<>();
    public static Map<Item, CauldronBehavior> getBehavior(Identifier id) {
        if (id == null) {
            return null;
        }

        if (id.equals(PotionCauldronBlock.POTION_CAULDRON_ID)) {
            return interactions;
        }

        if (id.equals(Registries.BLOCK.getId(Blocks.CAULDRON))) {
            return CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR;
        }

        if (id.equals(Registries.BLOCK.getId(Blocks.WATER_CAULDRON))) {
            return CauldronBehavior.WATER_CAULDRON_BEHAVIOR;
        }

        if (id.equals(Registries.BLOCK.getId(Blocks.LAVA_CAULDRON))) {
            return CauldronBehavior.LAVA_CAULDRON_BEHAVIOR;
        }

        if (id.equals(Registries.BLOCK.getId(Blocks.POWDER_SNOW_CAULDRON))) {
            return CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR;
        }

        if (customBehaviors.containsKey(id)) {
            return customBehaviors.get(id);
        }

        return null;
    }

    public static CauldronBehavior addInteraction(Item item, Map<Item, CauldronBehavior> behaviorMap, Collection<MetaEffect> effects, boolean keepOld) {
        CauldronBehavior prevBehavior = behaviorMap.get(item);
        if (prevBehavior == null) {
            keepOld = false;
        }

        final boolean keepOldFinal = keepOld;
        CauldronBehavior behavior = (BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack itemStack) -> {

            BlockData data = BlockData.getBlockData(state, world, pos);
            if (!data.valid) {
                if (keepOldFinal) {
                    return prevBehavior.interact(state, world, pos, player, hand, itemStack);
                }
                return ActionResult.PASS;
            }

            int initLevel = data.level;

            ActionResult prev = ActionResult.success(world.isClient);
            for (MetaEffect effect : effects) {
                prev = effect.interact(prev, data, world, pos, player, hand, itemStack);
            }


            if (!Registries.BLOCK.getId(data.source).getNamespace().equalsIgnoreCase("minecraft")) {
                // turn potion cauldrons into vanilla cauldrons
                if (data.entity.getLevel() < PotionCauldronBlock.MIN_LEVEL) {
                    BlockState cauldron = Blocks.CAULDRON.getDefaultState();
                    world.setBlockState(pos, cauldron);
                } else if (!data.entity.hasEffects()) {
                    BlockState cauldron = Blocks.WATER_CAULDRON.getDefaultState();
                    world.setBlockState(pos, cauldron.with(LeveledCauldronBlock.LEVEL, data.entity.getLevel()));
                }
            }
            else if (data.entity.hasEffects() && data.entity.getLevel() >= PotionCauldronBlock.MIN_LEVEL) {
                // turn vanilla cauldrons into potion cauldrons
                world.setBlockState(pos, data.state);
                BlockEntity dest = world.getBlockEntity(pos);

                assert dest != null;

                dest.readNbt(data.entity.createNbt());
            }
            else if (data.level != initLevel) {
                if (data.level == 0) {
                    BlockState cauldron = Blocks.CAULDRON.getDefaultState();
                    world.setBlockState(pos, cauldron);
                } else {
                    world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, data.level));
                }
            }

            if (keepOldFinal && prev == ActionResult.PASS) {
                return prevBehavior.interact(state, world, pos, player, hand, itemStack);
            }

            return prev;
        };

        return behaviorMap.put(item, behavior);
    }

    private static Collection<MetaEffect> parseEffects(JsonArray effects, String id) {
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
                    Main.log("WARNING: id must be a string " + id);
                    continue;
                }

                MetaEffectTemplate template = templates.get(eprim.getAsString());
                if (template == null) {
                    Main.log("WARNING: id does not name an effect template " + id);
                    continue;
                }

                Optional<ActionResult> quickfail = Optional.empty();
                if (obj.has("quickfail")) {
                    JsonElement quick = obj.get("quickfail");
                    if (quick.isJsonPrimitive()) {
                        JsonPrimitive qprim = quick.getAsJsonPrimitive();
                        if (qprim.isString()) {
                            String quickfailStr = qprim.getAsString();
                            if (quickfailStr.equalsIgnoreCase("SUCCESS")) {
                                quickfail = Optional.of(ActionResult.SUCCESS);
                            }
                            else if (quickfailStr.equalsIgnoreCase("PASS")) {
                                quickfail = Optional.of(ActionResult.PASS);
                            }
                            else if (quickfailStr.equalsIgnoreCase("CONSUME")) {
                                quickfail = Optional.of(ActionResult.CONSUME);
                            }
                            else if (quickfailStr.equalsIgnoreCase("FAIL")) {
                                quickfail = Optional.of(ActionResult.FAIL);
                            }
                            else if (quickfailStr.equalsIgnoreCase("CONSUME_PARTIAL")) {
                                quickfail = Optional.of(ActionResult.CONSUME_PARTIAL);
                            }
                        }
                    }
                }

                JsonObject params = null;
                if (obj.has("params")) {
                    JsonElement pelem = obj.get("params");
                    if (pelem.isJsonObject()) {
                        params = pelem.getAsJsonObject();
                    }
                }

                if (params == null) {
                    params = new JsonObject();
                }

                out.add(template.apply(quickfail, params));
            }
        }
        return out;
    }

    private static void parseRecipe(Item item, Map<Item, CauldronBehavior> behaviorMap, JsonObject recipe, String id) {
        JsonElement effectsObj = recipe.get("effects");
        if (effectsObj == null || !effectsObj.isJsonArray()) {
            Main.log("WARNING: " + item.toString() + " does not have a effects array " + id);
            return;
        }

        JsonArray effects = effectsObj.getAsJsonArray();
        Collection<MetaEffect> vals = parseEffects(effects, id);
        if (vals.isEmpty()) {
            return;
        }

        boolean keepOld = MetaEffectTemplate.getBoolOr(recipe.get("keepOld"), false);

        CauldronBehavior old = addInteraction(item, behaviorMap, vals, keepOld);
    }

    private static void parseRecipes (Map<Item, CauldronBehavior> behaviorMap, JsonObject recipes, String id) {
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


    private static void parseInversions(JsonArray inversions, String id) {
        for (JsonElement inversionE : inversions) {
            if (inversionE.isJsonObject()) {
                JsonObject inversion = inversionE.getAsJsonObject();

                Identifier from = MetaEffectTemplate.getId(inversion.get("from"));

                Identifier to = MetaEffectTemplate.getId(inversion.get("to"));

                if (from == null || to == null || from.equals(to)) {
                    Main.log("WARNING: invalid inversion in " + id);
                    continue;
                }

                boolean mutual = MetaEffectTemplate.getBoolOr(inversion.get("mutual"), false);

                StatusEffect fromE = Registries.STATUS_EFFECT.get(from);
                StatusEffect toE = Registries.STATUS_EFFECT.get(to);

                // note: the default effect returned is luck.
                // therefore there is no way to determine if it is valid or not.

                if (mutual) {
                    addMutualInversion(fromE, toE);
                }
                else {
                    addInversion(fromE, toE);
                }
            }
        }
    }

    private static void parseHeats(JsonObject heats, String id) {
        heats.asMap().forEach((String blockStr, JsonElement obj) -> {
            if (!obj.isJsonPrimitive()) {
                return;
            }

            JsonPrimitive prim = obj.getAsJsonPrimitive();
            if (!prim.isNumber()) {
                return;
            }

            int heat = prim.getAsInt();

            Identifier blockId = Identifier.tryParse(blockStr);
            if (blockId == null) {
                Main.log("WARNING: block " + blockStr + " is not an identifier " + id);
                return;
            }

            Block block = Registries.BLOCK.get(blockId);
            if (block == Blocks.AIR && !blockId.getPath().equalsIgnoreCase("air")) {
                Main.log("WARNING: block " + blockStr + " is not a valid identifier " + id);
                return;
            }

            BlockData.blockHeats.put(block, heat);
        });
    }

    public static void register() {
        MetaEffectTemplate.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("potioncraft", "metamixing");
            }

            @Override
            public void reload(ResourceManager manager) {
                // Clear Caches Here

                CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.WATER_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.LAVA_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.registerBehavior();

                interactions.clear();
                inversions.clear();

                BlockData.blockHeats.clear();

                // @TODO clear custom behaviors

                //Config.loadConfig(); // test this

                Map<Identifier, Resource> resources = manager.findResources("metamixing", id -> id.toString().endsWith(".json"));
                resources.forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream(); JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
                        // gson kinda blows ngl
                        JsonParser parser = new JsonParser();

                        JsonElement rootE = parser.parse(reader);
                        if (rootE == null || !rootE.isJsonObject()) {
                            Main.log("Encountered malformed resource " + id.toString());
                            return;
                        }

                        JsonObject root = rootE.getAsJsonObject();

                        JsonElement recipesE = root.get("recipes");
                        if (recipesE != null && recipesE.isJsonObject()) {
                            // parse the recipes
                            JsonObject recipes = recipesE.getAsJsonObject();
                            recipes.asMap().forEach((String blockId, JsonElement elem) -> {
                                if (!elem.isJsonObject()) {
                                    Main.log("WARNING: recipes for " + blockId + " not JSON object " + id.toString());
                                    return;
                                }

                                Identifier bid = Identifier.tryParse(blockId);
                                Map<Item, CauldronBehavior> behaviorMap = getBehavior(bid);

                                if (behaviorMap == null) {
                                    Main.log("WARNING: " + blockId + " does not have cauldron behavior " + id.toString());
                                    return;
                                }

                                JsonObject obj = elem.getAsJsonObject();
                                parseRecipes(behaviorMap, obj, id.toString());
                            });
                        }
                        else {
                            Main.log("WARNING: recipes resource malformed " + id.toString());
                        }

                        JsonElement inversionsE = root.get("inversions");
                        if (inversionsE != null && inversionsE.isJsonArray()) {
                            parseInversions(inversionsE.getAsJsonArray(), id.toString());
                        }
                        else {
                            Main.log("WARNING: inversions resource malformed " + id.toString());
                        }

                        JsonElement heatsE = root.get("heats");
                        if (heatsE != null && heatsE.isJsonObject()) {
                            parseHeats(heatsE.getAsJsonObject(), id.toString());
                        }
                        else {
                            Main.log("WARNING: heats resource malformed " + id.toString());
                        }
                    }
                    catch (Exception e) {
                        Main.log("Error occurred while loading resource " + id.toString() + ' ' + e.toString());
                    }
                });
            }
        });
    }



}
