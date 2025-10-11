package net.sploder12.potioncraft.mixin;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AbstractCauldronBlock.class)
public interface BehaviorAccessor {
    @Accessor(value = "behaviorMap")
    Map<Item, CauldronBehavior> getBehaviorMap();
}
