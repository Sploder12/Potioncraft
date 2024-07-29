package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.util.FluidHelper;
import net.sploder12.potioncraft.util.Json;

public interface CauldronsParser {

    private static void parseCauldrons(JsonObject cauldrons, String file) {
        cauldrons.asMap().forEach((String cauldronId, JsonElement elem) -> {
            if (!elem.isJsonPrimitive()) {
                Main.warn("fluid for " + cauldronId + " must be a string. " + file);
                return;
            }

            Block block = Registries.BLOCK.get(Identifier.tryParse(cauldronId));
            if (block instanceof AbstractCauldronBlock cauldronBlock) {
                Fluid fluid = Json.getRegistryEntry(elem, Registries.FLUID, file);
                if (fluid == null) {
                    return;
                }

                FluidHelper.setStaticBlockMapping(cauldronBlock, fluid);
            }
            else {
                Main.warn("cauldron identifer " + cauldronId + " does not name a cauldron block " + file);
            }
        });
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null || !elem.isJsonObject()) {
            Main.debug("cauldrons resource not present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Main.warn("cauldrons resource not object " + file);
            return;
        }

        parseCauldrons(elem.getAsJsonObject(), file);
    }
}
