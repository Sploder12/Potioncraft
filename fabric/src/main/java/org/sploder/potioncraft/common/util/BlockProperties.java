package org.sploder.potioncraft.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Optional;

public class BlockProperties {
    Block block;
    HashMap<Property<?>, Comparable<?>> properties;

    public BlockProperties(String blockstate) throws IllegalArgumentException {
        int stateStart = blockstate.indexOf('[');
        String state = null;
        if (stateStart != -1) {
            if (!blockstate.endsWith("]")) {
                throw new IllegalArgumentException("Blockstate string " + blockstate + " has malformed properties (missing ']')");
            }

            state = blockstate.substring(stateStart + 1, blockstate.length() - 1);
            blockstate = blockstate.substring(0, stateStart);
        }

        this.block = parseBlock(blockstate);
        this.properties = new HashMap<>();

        if (state == null) {
            return;
        }

        String[] properties = state.split(",");
        for (String property : properties) {
            String[] pair = property.split("=");
            if (pair.length != 2) {
                throw new IllegalArgumentException("blockstate string has invalid pair " + property);
            };

            addProperty(pair[0], pair[1]);
        }
    }

    private static Block parseBlock(String blockStr) throws IllegalArgumentException {
        Identifier blockId = Identifier.tryParse(blockStr);
        if (blockId == null) {
            throw new IllegalArgumentException("block " + blockStr + " is not an identifier");
        }

        var block = Registries.BLOCK.get(blockId);
        if (block == Blocks.AIR && !blockId.getPath().equalsIgnoreCase("air")) {
            throw new IllegalArgumentException("block " + blockStr + " is not a block identifier");
        }

        return block;
    }

    public <T extends Comparable<T>> void addProperty(Property<T> property, String value) throws IllegalArgumentException  {
        // check property makes sense for this block
        block.getDefaultState().get(property);

        // check value is of property type
        var val = parse(property, value);
        if (val.isEmpty())
            throw new IllegalArgumentException("Cannot set property " + property + " to " + value + ", it is not an allowed value");

        properties.put(property, val.get());
    }

    public void addProperty(String property, String value) throws IllegalArgumentException  {
        // check property makes sense for this block
        var prop = block.getDefaultState().getProperties().stream()
                .filter(p -> p.getName().equals(property))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(property + " is not a property of " + block));

        addProperty(prop, value);
    }

    public BlockState toBlockState() throws IllegalArgumentException {
        var state = block.getDefaultState();
        for (var entry : properties.entrySet()) {
            state = withProperty(state, entry.getKey(), entry.getValue());
        }
        return state;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isEquivalent(BlockState state) {
        try {
            for (var entry : properties.entrySet()) {
                if (!state.get(entry.getKey()).equals(entry.getValue()))
                    return false;
            }
        } catch(IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>, V extends T> BlockState withProperty(BlockState state, Property<?> property, Comparable<?> value) throws IllegalArgumentException {
        return state.with((Property<T>) property, (V) value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Optional<T> parse(Property<?> property, String valueStr) {
        Property<T> typed = (Property<T>) property;
        return typed.parse(valueStr);
    }
}
