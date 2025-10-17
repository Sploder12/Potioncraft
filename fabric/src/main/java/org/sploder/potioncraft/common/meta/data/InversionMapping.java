package org.sploder.potioncraft.common.meta.data;

import net.minecraft.entity.effect.StatusEffect;

import java.util.HashMap;

public class InversionMapping {
    HashMap<StatusEffect, StatusEffect> inversions = new HashMap<>();

    public void addMutualInversion(StatusEffect first, StatusEffect second) {
        inversions.put(first, second);
        inversions.put(second, first);
    }

    public void addInversion(StatusEffect from, StatusEffect to) {
        inversions.put(from, to);
    }

    public boolean has(StatusEffect effect) {
        return inversions.containsKey(effect);
    }

    public StatusEffect get(StatusEffect effect) {
        return inversions.get(effect);
    }
}
