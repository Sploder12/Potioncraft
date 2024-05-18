package net.sploder12.potioncraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.Identifier;

public class Json {

    private static JsonPrimitive getPrim(JsonElement elem) {
        if (elem == null || !elem.isJsonPrimitive()) {
            return null;
        }

        return elem.getAsJsonPrimitive();
    }

    public static Identifier getId(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isString()) {
            return null;
        }

        return Identifier.tryParse(prim.getAsString());
    }

    public static Number getNumber(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isNumber()) {
            return null;
        }

        return prim.getAsNumber();
    }

    public static Integer getInt(JsonElement elem) {
        Number num = getNumber(elem);
        return num == null ? null : num.intValue();
    }

    public static int getIntOr(JsonElement elem, int or) {
        Integer val = getInt(elem);
        return val == null ? or : val;
    }

    public static Float getFloat(JsonElement elem) {
        Number num = getNumber(elem);
        return num == null ? null : num.floatValue();
    }

    public static float getFloatOr(JsonElement elem, float or) {
        Float val = getFloat(elem);
        return val == null ? or : val;
    }

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
}
