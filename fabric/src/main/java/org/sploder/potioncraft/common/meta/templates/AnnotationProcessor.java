package org.sploder.potioncraft.common.meta.templates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.sploder.potioncraft.common.util.BlockProperties;
import org.sploder.potioncraft.common.util.Json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;

public class AnnotationProcessor {
    public static final Map<Class<?>, Function<JsonElement, Object>> parsers;

    static {
        parsers = new HashMap<>();

        // Primitive type beat
        parsers.put(String.class, Json::getString);
        parsers.put(Integer.class, Json::getInt);
        parsers.put(int.class, Json::getInt);
        parsers.put(Boolean.class, Json::getBool);
        parsers.put(boolean.class, Json::getBool);
        parsers.put(Float.class, Json::getFloat);
        parsers.put(float.class, Json::getFloat);

        // Raw
        parsers.put(JsonElement.class, (elem) -> elem);
        parsers.put(JsonPrimitive.class, (elem) -> {
           if (elem.isJsonPrimitive()) {
               return elem.getAsJsonPrimitive();
           }
           return null;
        });
        parsers.put(JsonArray.class, (elem) -> {
            if (elem.isJsonArray()) {
                return elem.getAsJsonArray();
            }
            return null;
        });
        parsers.put(JsonObject.class, (elem) -> {
            if (elem.isJsonObject()) {
                return elem.getAsJsonObject();
            }
            return null;
        });

        // Minceraft
        parsers.put(Identifier.class, Json::getId);
        parsers.put(ActionResult.class, Json::getActionResult);
        parsers.put(BlockProperties.class, (elem) -> {
            var strval = Json.getString(elem);
            if (strval != null) {
                try {
                    return new BlockProperties(strval);
                }
                catch (IllegalArgumentException ignored) {
                }
            }
            return null;
        });

        // Registry based
        parsers.put(Block.class, (elem) -> Json.getRegistryEntry(elem, Registries.BLOCK));
        parsers.put(EntityType.class, (elem) -> Json.getRegistryEntry(elem, Registries.ENTITY_TYPE));
        parsers.put(Fluid.class, (elem) -> Json.getRegistryEntry(elem, Registries.FLUID));
        parsers.put(Item.class, (elem) -> Json.getRegistryEntry(elem, Registries.ITEM));
        parsers.put(Potion.class, (elem) -> Json.getRegistryEntry(elem, Registries.POTION));
        parsers.put(SoundEvent.class, (elem) -> Json.getRegistryEntry(elem, Registries.SOUND_EVENT));
        parsers.put(StatusEffect.class, (elem) -> Json.getRegistryEntry(elem, Registries.STATUS_EFFECT));
    }

    private static Object parseValue(Class<?> type, String key, JsonElement value) throws RuntimeException, IllegalAccessException {
        var parser = parsers.get(type);
        if (parser == null) {
            throw new RuntimeException("Argument " + key + " has no associated parser for " + type);
        }

        var val = parser.apply(value);
        if (val == null) {
            throw new IllegalArgumentException("Failed to parse " + key + ", " + value + " is not a valid " + type);
        }

        if (!type.isAssignableFrom(val.getClass())) {
            // oh Java :)
            if (type == boolean.class && val.getClass() == Boolean.class) {
                return (boolean) val;
            }
            else if (type == byte.class && val.getClass() == Byte.class) {
                return (byte) val;
            }
            else if (type == char.class && val.getClass() == Character.class) {
                return (char) val;
            }
            else if (type == short.class && val.getClass() == Short.class) {
                return (short) val;
            }
            else if (type == int.class && val.getClass() == Integer.class) {
                return (int) val;
            }
            else if (type == long.class && val.getClass() == Long.class) {
                return (long) val;
            }
            else if (type == float.class && val.getClass() == Float.class) {
                return (float) val;
            }
            else if (type == double.class && val.getClass() == Double.class) {
                return (double) val;
            }

            throw new RuntimeException("Argument " + key + " of type " + type + " cannot be set by " + val + " of type " + val.getClass());
        }

        return val;
    }

    private static List<Object> parseList(Field field, String key, @NotNull JsonArray value) throws RuntimeException, IllegalAccessException {
        var genType = field.getGenericType();
        if (genType instanceof ParameterizedType paramType) {
            var typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> elemType) {
                ArrayList<Object> out = new ArrayList<>();
                for (var v : value) {
                    out.add(parseValue(elemType, key, v));
                }
                return out;
            }
        }

        throw new RuntimeException("List like type " + field.getType() + " could not be parsed");
    }

    private static Map<String, Object> parseMap(Field field, String key, @NotNull JsonObject value) throws RuntimeException, IllegalAccessException {
        var genType = field.getGenericType();
        if (genType instanceof ParameterizedType paramType) {
            var typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length == 2 && typeArgs[0] == String.class && typeArgs[1] instanceof Class<?> elemType) {
                HashMap<String, Object> out = new HashMap<>();
                for (var entry : value.entrySet()) {
                    out.put(entry.getKey(), parseValue(elemType, key, entry.getValue()));
                }
                return out;
            }
        }
        throw new RuntimeException("Map like type " + field.getType() + " could not be parsed");
    }

    private static Set<Object> parseSet(Field field, String key, @NotNull JsonElement value) throws RuntimeException, IllegalAccessException {
        var genType = field.getGenericType();
        if (genType instanceof ParameterizedType paramType) {
            var typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> elemType) {
                Set<Object> out = new HashSet<>();
                if (value.isJsonArray()) {
                    var val = value.getAsJsonArray();
                    for (var v : val) {
                        out.add(parseValue(elemType, key, v));
                    }
                }
                else if (value.isJsonObject()) {
                    out.add(parseValue(elemType, key, value));
                }
                else if (value.isJsonPrimitive()) {
                    out.add(parseValue(elemType, key, value));
                }
                else {
                    throw new IllegalArgumentException("Failed to parse " + key + ", " + value + "is not a valid " + field.getType());
                }
                return out;
            }
        }

        throw new RuntimeException("Set like type " + field.getType() + " could not be parsed");
    }

    private static Object parseField(Field field, String key, @NotNull JsonElement value) throws RuntimeException, IllegalAccessException {
        var fieldType = field.getType();
        if (!parsers.containsKey(fieldType)) {
            if (List.class == fieldType) {
                if (!value.isJsonArray()) {
                    throw new IllegalArgumentException("Failed to parse " + key + ", " + value + "is not a valid " + fieldType);
                }
                var arr = value.getAsJsonArray();

                return parseList(field, key, arr);
            } else if (Map.class == fieldType) {
                if (!value.isJsonObject()) {
                    throw new IllegalArgumentException("Failed to parse " + key + ", " + value + "is not a valid " + fieldType);
                }
                var dict = value.getAsJsonObject();

                return parseMap(field, key, dict);
            }
            else if (Set.class == fieldType) {
               return parseSet(field, key, value);
            }
            else {
                throw new RuntimeException("Argument " + key + " has no associated parser for " + fieldType);
            }
        }
        else {
            return parseValue(fieldType, key, value);
        }
    }

    public static <T> void populateTemplate(@NotNull T target, @NotNull JsonObject params) throws RuntimeException, IllegalArgumentException {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) target.getClass();

            var ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            T defaultInstance = ctor.newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Argument.class)) {
                    Argument annotation = field.getAnnotation(Argument.class);
                    var key = annotation.key();
                    var optional = annotation.optional();
                    var fieldType = field.getType();

                    field.setAccessible(true);

                    var param = params.get(key);
                    if (param == null || param.isJsonNull()) {
                        if (!optional) {
                            throw new IllegalArgumentException("Argument " + key + " is not optional and has no value");
                        }

                        field.set(target, field.get(defaultInstance));
                        continue;
                    }

                    var value = parseField(field, key, param);
                    if (value != null && !fieldType.isAssignableFrom(value.getClass())) {
                        // Oh Java :)
                        if (
                                (fieldType == boolean.class && value.getClass() == Boolean.class)
                                || (fieldType == byte.class && value.getClass() == Byte.class)
                                || (fieldType == char.class && value.getClass() == Character.class)
                                || (fieldType == short.class && value.getClass() == Short.class)
                                || (fieldType == int.class && value.getClass() == Integer.class)
                                || (fieldType == long.class && value.getClass() == Long.class)
                                || (fieldType == float.class && value.getClass() == Float.class)
                                || (fieldType == double.class && value.getClass() == Double.class)) {
                            field.set(target, value);
                            continue;
                        }

                        throw new RuntimeException("Argument " + key + " of type " + fieldType + " cannot be set by " + value + " of type " + value.getClass());
                    }

                    field.set(target, value);
                }
                else if (field.isAnnotationPresent(RawArguments.class)) {
                    var fieldType = field.getType();

                    field.setAccessible(true);

                    if (!fieldType.isAssignableFrom(params.getClass())) {
                        throw new RuntimeException("Raw arguments field " + field + " is not of type JsonObject");
                    }
                    field.set(target, params);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
