package net.sploder12.potioncraft.common.meta;

import net.minecraft.block.BlockState;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.common.PotionCauldronBlockEntity;
import net.sploder12.potioncraft.common.config.Config;
import net.sploder12.potioncraft.common.meta.parsers.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MetaMixing {

    // The logic and parsing for meta mixing files.

    //public static final CauldronBehavior.CauldronBehaviorMap interactions = CauldronBehavior.createMap("potion");
    public static final Map<Item, CauldronBehavior> interactions = CauldronBehavior.createMap();

    public static final LinkedHashMap<String, Parser> parsers = new LinkedHashMap<>();



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

        parsers.put("templates", new Parser(TemplatesParser::parse));
        parsers.put("fluids", new Parser(FluidsParser::parse));
        parsers.put("cauldrons", new Parser(CauldronsParser::parse));
        parsers.put("inversions", new Parser(InversionsParser::parse));
        parsers.put("heats", new Parser(HeatsParser::parse));
        parsers.put("recipes", new Parser(RecipesParser::parse));
    }
}
