package net.sploder12.potioncraft.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraft.component.type.PotionContentsComponent.mixColors;

public class PotionUtil {

    public static ItemStack setPotion(ItemStack target, RegistryEntry<Potion> potion) {
        target.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion));
        return target;
    }

    public static ItemStack setCustomPotionEffects(ItemStack target, List<StatusEffectInstance> effects) {
        PotionContentsComponent potionContentsComponent = target.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

        target.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(
                potionContentsComponent.potion(),
                potionContentsComponent.customColor(),
                effects,
                potionContentsComponent.customName()
        ));

        return target;
    }

    public static List<StatusEffectInstance> getPotionEffects(ItemStack itemStack) {
        PotionContentsComponent potionContentsComponent = itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

        ArrayList<StatusEffectInstance> effects = new ArrayList<>();
        potionContentsComponent.getEffects().forEach(effects::add);
        return effects;
    }

    public static List<StatusEffectInstance> getCustomPotionEffects(ItemStack itemStack) {
        PotionContentsComponent potionContentsComponent = itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

        return new ArrayList<>(potionContentsComponent.customEffects());
    }

    public static int getColor(List<StatusEffectInstance> effects) {
        return mixColors(effects).orElse(-13083194);
    }
}
