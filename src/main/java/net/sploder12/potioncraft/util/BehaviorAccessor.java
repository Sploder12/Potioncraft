package net.sploder12.potioncraft.util;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.sploder12.potioncraft.Main;

import java.lang.reflect.Field;
import java.util.Map;

// this is secretly a mixin
public class BehaviorAccessor {
    public static Map<Item, CauldronBehavior> getBehaviorMap(AbstractCauldronBlock cauldron) {
        try {
            Field behaviorMap = AbstractCauldronBlock.class.getDeclaredField("behaviorMap");
            behaviorMap.setAccessible(true);

            Object map = behaviorMap.get(cauldron);

            @SuppressWarnings("unchecked")
            Map<Item, CauldronBehavior> behavior = (Map<Item, CauldronBehavior>) map;
            return behavior;
        }
        catch (Exception e) {
            Main.error("Cannot access behavior map of cauldrons!!!", e);
        }

        return null;
    }
}
