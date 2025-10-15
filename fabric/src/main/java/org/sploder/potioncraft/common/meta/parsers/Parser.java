package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonElement;

import java.util.function.BiConsumer;

public class Parser {
    final BiConsumer<JsonElement, String> parseFunc;

    public Parser(BiConsumer<JsonElement, String> parseFunc) {
        this.parseFunc = parseFunc;
    }

    public void parse(JsonElement elem, String file) {
        parseFunc.accept(elem, file);
    }
}
