package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.util.FluidHelper;
import net.sploder12.potioncraft.util.Json;

public interface Cauldrons {

    private static void parseCauldrons(JsonObject cauldrons, String file) {
        cauldrons.asMap().forEach((String cauldronId, JsonElement elem) -> {
            if (!elem.isJsonArray()) {
                Main.log("WARNING: cauldrons for " + cauldronId + " must be an array " + file);
                return;
            }

            Block block = Registries.BLOCK.get(Identifier.tryParse(cauldronId));
            if (block instanceof AbstractCauldronBlock cauldronBlock) {
                elem.getAsJsonArray().forEach((JsonElement entry) -> {
                    if (!entry.isJsonPrimitive()) {
                        Main.log("WARNING: liquid entries must be identifiers " + file);
                        return;
                    }

                    Fluid fluid = Json.getRegistryEntry(entry, Registries.FLUID);
                    if (fluid == null || fluid == Fluids.EMPTY) {
                        Main.log("WARNING: liquid entries must identify fluids" + file);
                        return;
                    }

                    FluidHelper.setStaticBlockMapping(cauldronBlock, fluid);
                });
            }
            else {
                Main.log("WARNING: cauldron identifer must name a cauldron block " + file);
            }
        });
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null || !elem.isJsonObject()) {
            return;
        }

        if (!elem.isJsonObject()) {
            Main.log("WARNING: cauldrons resource not object" + file);
            return;
        }

        parseCauldrons(elem.getAsJsonObject(), file);
    }
}
