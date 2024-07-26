package net.sploder12.potioncraft.meta.templates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.meta.CauldronData;
import net.sploder12.potioncraft.meta.MetaEffect;
import net.sploder12.potioncraft.meta.parsers.EffectParser;
import net.sploder12.potioncraft.util.Json;

import java.util.Collection;
import java.util.HashSet;

public interface Conditional {
    // hand swinging is controlled by the LAST event, thus why FORCE_SWING_HAND exists.
    // FORCE_SWING_HAND can also be used to generate a guaranteed SUCCESS

    MetaEffectTemplate FORCE_SWING_HAND = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.success(world.isClient);

    // Predicate templates are useful for checking conditions!

    MetaEffectTemplate INVERT_COND = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (prev == ActionResult.PASS) {
            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    };

    // shortcircuit eval &&
    MetaEffectTemplate AND = (params, file) -> {
        JsonElement conditionsE = params.get("conditions");
        if (conditionsE == null || !conditionsE.isJsonArray()) {
            Main.warn("AND has no conditions! " + file);
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
        }

        final boolean shortCircuit = Json.getBoolOr(params.get("short_circuit"), true);

        final Collection<MetaEffect> effects = EffectParser.parseEffects(conditionsE.getAsJsonArray(), file);
        if (effects.isEmpty()) {
            Main.warn("AND has no conditions! " + file);
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
        }

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            boolean success = true;

            for (MetaEffect effect : effects) {
                ActionResult cond = effect.interact(ActionResult.success(world.isClient), data, world, pos, player, hand, stack);

                if (cond == ActionResult.PASS) {
                    success = false;
                    if (shortCircuit) {
                        break;
                    }
                }
            }

            if (success) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    // shortcircuit eval ||
    MetaEffectTemplate OR = (params, file) -> {
        JsonElement conditionsE = params.get("conditions");
        if (conditionsE == null || !conditionsE.isJsonArray()) {
            Main.warn("OR has no conditions! " + file);
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
        }

        final boolean shortCircuit = Json.getBoolOr(params.get("short_circuit"), true);

        final Collection<MetaEffect> effects = EffectParser.parseEffects(conditionsE.getAsJsonArray(), file);
        if (effects.isEmpty()) {
            Main.warn("OR has no conditions! " + file);
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
        }

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            boolean success = false;

            for (MetaEffect effect : effects) {
                ActionResult cond = effect.interact(ActionResult.success(world.isClient), data, world, pos, player, hand, stack);

                if (cond == ActionResult.success(world.isClient)) {
                    success = true;
                    if (shortCircuit) {
                        break;
                    }
                }
            }

            if (success) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    MetaEffectTemplate IS_FROM_VANILLA = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (Registries.BLOCK.getId(data.source).getNamespace().equalsIgnoreCase("minecraft")) {
            return ActionResult.success(world.isClient);
        }
        else {
            return ActionResult.PASS;
        }
    };

    // params: "level": int - if set will only succeed when data.getLevel() == int
    // else will succeed when data.getLevel() > 0
    MetaEffectTemplate HAS_LEVEL = (params, file) -> {
        final Integer finalTarget = Json.getInt(params.get("level"));

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (finalTarget == null) {
                if (data.getLevel() > 0) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            }

            if (finalTarget == data.getLevel()) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    // params: "heat": int - if set will only succeed when data.heat >= int
    // or == when int is 0, or <= when int is < 0
    // default is as if the parameter was 1
    MetaEffectTemplate HAS_HEAT = (params, file) -> {
        final int finalTarget = Json.getIntOr(params.get("heat"), 1);

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            // target heat is positive
            if (finalTarget > 0) {
                if (data.heat >= finalTarget) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            }

            // target heat is negative
            if (finalTarget < 0) {
                if (data.heat <= finalTarget) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            }

            // target heat is 0
            if (finalTarget == data.heat) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    // params: "fluids": array of identifiers to fluids
    MetaEffectTemplate HAS_FLUID = (params, file) -> {
        JsonElement fluidsElem = params.get("fluids");
        HashSet<Fluid> fluids = null;

        if (fluidsElem != null) {
            if (fluidsElem.isJsonArray()) {
                JsonArray fluidsArr = fluidsElem.getAsJsonArray();

                for (JsonElement fluidId : fluidsArr) {
                    Fluid fluid = Json.getRegistryEntry(fluidId, Registries.FLUID, file);

                    if (fluid != null) {
                        if (fluids == null) {
                            fluids = new HashSet<>();
                        }

                        fluids.add(fluid);
                    }
                }
            }
            else if (fluidsElem.isJsonPrimitive()) {
                Fluid fluid = Json.getRegistryEntry(fluidsElem, Registries.FLUID, file);

                if (fluid != null) {
                    fluids = new HashSet<>();
                    fluids.add(fluid);
                }
            }
        }

        if (fluids == null) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (data.getFluid() != Fluids.EMPTY) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            };
        }

        HashSet<Fluid> finalFluids = fluids;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (finalFluids.contains(data.getFluid())) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    MetaEffectTemplate IS_FULL = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (data.getLevel() >= PotionCauldronBlock.MAX_LEVEL) {
            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    };

    // params: "level": int - if set will only succeed when data.getLevel() >= int
    MetaEffectTemplate MIN_LEVEL = (params, file) -> {
        Number num = Json.getNumber(params.get("level"));
        if (num == null) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
        }

        final int finalTarget = num.intValue();
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (data.getLevel() >= finalTarget) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    // params: "level": int - if set will only succeed when data.heat <= int
    MetaEffectTemplate MAX_LEVEL = (params, file) -> {
        Number num = Json.getNumber(params.get("level"));
        if (num == null) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
        }

        final int finalTarget = num.intValue();
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (data.getLevel() <= finalTarget) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    // params: "heat": int - if set will only succeed when data.heat >= int
    MetaEffectTemplate MIN_HEAT = (params, file) -> {
        Number num = Json.getNumber(params.get("heat"));
        if (num == null) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
        }

        final int finalTarget = num.intValue();
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (data.heat >= finalTarget) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    // params: "heat": int - if set will only succeed when data.heat <= int
    MetaEffectTemplate MAX_HEAT = (params, file) -> {
        Number num = Json.getNumber(params.get("heat"));
        if (num == null) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
        }

        final int finalTarget = num.intValue();
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (data.heat <= finalTarget) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    MetaEffectTemplate ITEM_HAS_EFFECTS = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (PotionUtil.getPotionEffects(stack).isEmpty()) {
            return ActionResult.PASS;
        }

        return ActionResult.success(world.isClient);
    };
}
