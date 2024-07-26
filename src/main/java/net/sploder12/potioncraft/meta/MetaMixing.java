package net.sploder12.potioncraft.meta;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.*;
import net.sploder12.potioncraft.meta.parsers.*;
import net.sploder12.potioncraft.meta.templates.MetaEffectTemplate;
import net.sploder12.potioncraft.util.BehaviorAccessor;
import net.sploder12.potioncraft.util.FluidHelper;
import net.sploder12.potioncraft.util.HeatHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MetaMixing {

    // The logic and parsing for meta mixing files.

    //public static final CauldronBehavior.CauldronBehaviorMap interactions = CauldronBehavior.createMap("potion");
    public static final Map<Item, CauldronBehavior> interactions = CauldronBehavior.createMap();

    public static final LinkedHashMap<String, Parser> parsers = new LinkedHashMap<>();

    public static Map<Item, CauldronBehavior> getBehavior(AbstractCauldronBlock id) {
        if (id == null) {
            return null;
        }

        return BehaviorAccessor.getBehaviorMap(id);
    }

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

            int tmpPotency = getTmpPotency(potency, itemStack, data);

            final int maxPotency = PotionCauldronBlockEntity.getMaxPotency();
            final int newPotency = data.entity.getPotency() + tmpPotency;
            if (maxPotency >= 0 && newPotency > maxPotency) {
                return ActionResult.PASS;
            }

            ActionResult prev = ActionResult.success(world.isClient);
            for (MetaEffect effect : effects) {
                prev = effect.interact(prev, data, world, pos, player, hand, itemStack);
            }

            if (prev != ActionResult.PASS) {
                data.entity.setPotency(newPotency);
            }

            data.transformBlock(world, initLevel);

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

        parsers.put("templates", new Parser(TemplatesParser::parse));
        parsers.put("fluids", new Parser(FluidsParser::parse));
        parsers.put("cauldrons", new Parser(CauldronsParser::parse));
        parsers.put("inversions", new Parser(InversionsParser::parse));
        parsers.put("heats", new Parser(HeatsParser::parse));
        parsers.put("recipes", new Parser(RecipesParser::parse));

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("potioncraft", "metamixing");
            }

            @Override
            public void reload(ResourceManager manager) {
                // Clear Caches Here

                FluidHelper.reset();
                HeatHelper.reset();

                CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.WATER_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.LAVA_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.registerBehavior();

                interactions.clear();

                InversionsParser.clear();
                MetaEffectTemplate.register();

                // @TODO clear custom behaviors

                Config.loadConfig(); // test this

                Map<Identifier, Resource> resources = manager.findResources("metamixing", id -> id.toString().endsWith(".json"));
                resources.forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream(); JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
                        // gson kinda blows ngl

                        JsonParser parser = new JsonParser();

                        JsonElement rootE = parser.parse(reader);
                        if (rootE == null || !rootE.isJsonObject()) {
                            Main.warn("Encountered malformed resource " + id);
                            return;
                        }

                        JsonObject root = rootE.getAsJsonObject();
                        String file = id.toString();

                        parsers.forEach((String elemId, Parser elemParser) -> {
                            elemParser.parse(root.get(elemId), file);
                        });
                    }
                    catch (Exception e) {
                        Main.error("Error occurred while loading resource " + id + "\n" + e.getMessage());
                    }
                });
            }
        });
    }
}
