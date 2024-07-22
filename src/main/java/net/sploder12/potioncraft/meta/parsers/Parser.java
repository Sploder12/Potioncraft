package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonElement;

import java.util.function.BiConsumer;

public class Parser {
    BiConsumer<JsonElement, String> parseFunc;

    public Parser(BiConsumer<JsonElement, String> parseFunc) {
        this.parseFunc = parseFunc;
    }

    public void parse(JsonElement elem, String file) {
        parseFunc.accept(elem, file);
    }
}
