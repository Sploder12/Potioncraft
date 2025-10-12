package net.sploder12.potioncraft.common;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;

import java.io.File;
import java.util.Map;

public interface Loader {
    Map<Item, CauldronBehavior> getBehaviorMap(AbstractCauldronBlock cauldron);

    File getConfigFile();

    BlockEntityType<PotionCauldronBlockEntity> getPotionCauldronBlockEntity();
}

