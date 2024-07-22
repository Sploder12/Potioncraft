package net.sploder12.potioncraft.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Json {

    @Nullable
    private static JsonPrimitive getPrim(JsonElement elem) {
        if (elem == null || !elem.isJsonPrimitive()) {
            return null;
        }

        return elem.getAsJsonPrimitive();
    }

    @Nullable
    public static JsonObject getObj(JsonElement elem) {
        if (elem == null || !elem.isJsonObject()) {
            return null;
        }

        return elem.getAsJsonObject();
    }

    @Nullable
    public static Identifier getId(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isString()) {
            return null;
        }

        return Identifier.tryParse(prim.getAsString());
    }

    @Nullable
    public static <T> T getRegistryEntry(JsonElement elem, Registry<T> registry) {
        Identifier id = getId(elem);

        if (id == null) {
            return null;
        }

        return registry.get(id);
    }

    @Nullable
    public static Number getNumber(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isNumber()) {
            return null;
        }

        return prim.getAsNumber();
    }

    @Nullable
    public static Integer getInt(JsonElement elem) {
        Number num = getNumber(elem);
        return num == null ? null : num.intValue();
    }

    public static int getIntOr(JsonElement elem, int or) {
        Integer val = getInt(elem);
        return val == null ? or : val;
    }

    @Nullable
    public static Float getFloat(JsonElement elem) {
        Number num = getNumber(elem);
        return num == null ? null : num.floatValue();
    }

    public static float getFloatOr(JsonElement elem, float or) {
        Float val = getFloat(elem);
        return val == null ? or : val;
    }

    @Nullable
    public static Boolean getBool(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isBoolean()) {
            return null;
        }

        return prim.getAsBoolean();
    }

    public static boolean getBoolOr(JsonElement elem, boolean or) {
        Boolean b = getBool(elem);
        return b == null ? or : b;
    }

    @Nullable
    public static String getString(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isString()) {
            return null;
        }

        return prim.getAsString();
    }

    public static String getStringOr(JsonElement elem, String or) {
        String str = getString(elem);
        return str == null ? or : str;
    }

    @Nullable
    public static String asString(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null) {
            return null;
        }

        try {
            return prim.getAsString();
        }
        catch (AssertionError e) {
            return null;
        }
    }

    public static Optional<ActionResult> getActionResult(JsonElement elem) {
        String str = getString(elem);

        if (str == null) return Optional.empty();

        if (str.equalsIgnoreCase("SUCCESS")) {
            return Optional.of(ActionResult.SUCCESS);
        }
        else if (str.equalsIgnoreCase("PASS")) {
            return Optional.of(ActionResult.PASS);
        }
        else if (str.equalsIgnoreCase("CONSUME")) {
            return Optional.of(ActionResult.CONSUME);
        }
        else if (str.equalsIgnoreCase("FAIL")) {
            return Optional.of(ActionResult.FAIL);
        }
        else if (str.equalsIgnoreCase("CONSUME_PARTIAL")) {
            return Optional.of(ActionResult.CONSUME_PARTIAL);
        }

        return Optional.empty();
    }

    public static ActionResult getActionResultOr(JsonElement elem, ActionResult or) {
        Optional<ActionResult> ar = getActionResult(elem);
        return ar.orElse(or);
    }
}
