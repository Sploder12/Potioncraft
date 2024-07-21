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
import net.sploder12.potioncraft.util.Json;

import java.util.HashSet;

public interface Conditional {
    // hand swinging is controlled by the LAST event, thus why FORCE_SWING_HAND exists.
    // FORCE_SWING_HAND can also be used to generate a guaranteed SUCCESS
    // (or PASS if you use two FORCE_SWING_HANDs with a quickfail == SUCCESS)

    MetaEffectTemplate FORCE_SWING_HAND = (params) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.success(world.isClient);

    // Predicate templates are useful for checking conditions!
    // && is easy! || is harder but doable via De Morgan's law

    MetaEffectTemplate INVERT_COND = (params) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (prev == ActionResult.PASS) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    };

    MetaEffectTemplate IS_FROM_VANILLA = (params) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (Registries.BLOCK.getId(data.source).getNamespace().equalsIgnoreCase("minecraft")) {
            return ActionResult.success(world.isClient);
        }
        else {
            return ActionResult.PASS;
        }
    };

    // params: "level": int - if set will only succeed when data.getLevel() == int
    // else will succeed when data.getLevel() > 0
    MetaEffectTemplate HAS_LEVEL = (params) -> {
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
    MetaEffectTemplate HAS_HEAT = (params) -> {
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
    MetaEffectTemplate HAS_FLUID = (params) -> {
        JsonElement fluidsElem = params.get("fluids");
        HashSet<Fluid> fluids = null;

        if (fluidsElem != null) {
            if (fluidsElem.isJsonArray()) {
                JsonArray fluidsArr = fluidsElem.getAsJsonArray();

                for (JsonElement fluidId : fluidsArr) {
                    Fluid fluid = Json.getRegistryEntry(fluidId, Registries.FLUID);

                    if (fluid != null && fluid != Fluids.EMPTY) {
                        if (fluids == null) {
                            fluids = new HashSet<>();
                        }

                        fluids.add(fluid);
                    }
                }
            }
            else if (fluidsElem.isJsonPrimitive()) {
                Fluid fluid = Json.getRegistryEntry(fluidsElem, Registries.FLUID);

                if (fluid != null && fluid != Fluids.EMPTY) {
                    fluids = new HashSet<>();
                    fluids.add(fluid);
                }
                else {
                    Main.log("WARNING: " + fluidsElem.getAsString() + " does not name a fluid");
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

    MetaEffectTemplate IS_FULL = (params) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (data.getLevel() >= PotionCauldronBlock.MAX_LEVEL) {
            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    };

    // params: "level": int - if set will only succeed when data.getLevel() >= int
    MetaEffectTemplate MIN_LEVEL = (params) -> {
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
    MetaEffectTemplate MAX_LEVEL = (params) -> {
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
    MetaEffectTemplate MIN_HEAT = (params) -> {
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
    MetaEffectTemplate MAX_HEAT = (params) -> {
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

    MetaEffectTemplate ITEM_HAS_EFFECTS = (params) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (PotionUtil.getPotionEffects(stack).isEmpty()) {
            return ActionResult.PASS;
        }

        return ActionResult.success(world.isClient);
    };
}
