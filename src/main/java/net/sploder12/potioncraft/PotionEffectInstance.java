package net.sploder12.potioncraft;


import jdk.jshell.Snippet;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PotionEffectInstance {
    public final StatusEffect type;
    public float duration;
    public float amplifier;
    public boolean ambient;
    public boolean showParticles;
    public boolean showIcon;

    private static final float epsilon = 0.00001f;

    PotionEffectInstance(StatusEffect type) {
        this(type, 0.0f, 1.0f);
    }

    PotionEffectInstance(StatusEffect type, float duration, float amplifier) {
        this(type, duration, amplifier, false, true);
    }

    PotionEffectInstance(StatusEffect type, float duration, float amplifier, boolean ambient, boolean visible) {
        this(type, duration, amplifier, ambient, visible, visible);
    }

    PotionEffectInstance(@NotNull StatusEffectInstance effect) {
        this(effect.getEffectType(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon());
    }

    PotionEffectInstance(StatusEffect type, float duration, float amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.showParticles = showParticles;
        this.showIcon = showIcon;
    }

    public StatusEffectInstance asStatusEffect() {
        int effectiveDuration = (int)(duration);
        int effectiveAmplifier = (int)(amplifier);
        if (amplifier < 1.0f) {
            effectiveDuration = 0;
            effectiveAmplifier = 0;
        }

        return new StatusEffectInstance(type, effectiveDuration, effectiveAmplifier, ambient, showParticles, showIcon);
    }

    public void dilute(float ratio) {
        duration *= ratio;
        amplifier *= ratio;

        if (duration - epsilon <= 0.0f) {
            duration = 0.0f;
        }

        if (amplifier - epsilon <= 0.0f) {
            amplifier = 0.0f;
        }
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return asStatusEffect().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof PotionEffectInstance other) {
            return
                    Math.abs(this.duration - other.duration) <= epsilon &&
                    Math.abs(this.amplifier - other.amplifier) <= epsilon &&
                    this.ambient == other.ambient &&
                    this.showParticles == other.showParticles &&
                    this.showIcon == other.showIcon;
        }

        return false;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("Id", StatusEffect.getRawId(this.type));
        this.writeTypelessNbt(nbt);
        return nbt;
    }

    private void writeTypelessNbt(NbtCompound nbt) {
        nbt.putFloat("Amplifier", this.amplifier);
        nbt.putFloat("Duration", this.duration);
        nbt.putBoolean("Ambient", this.ambient);
        nbt.putBoolean("ShowParticles", this.showParticles);
        nbt.putBoolean("ShowIcon", this.showIcon);
    }

    @Nullable
    public static PotionEffectInstance fromNbt(NbtCompound nbt) {
        int id = nbt.getInt("Id");
        StatusEffect type = StatusEffect.byRawId(id);
        return type == null ? null : fromNbt(type, nbt);
    }

    private static PotionEffectInstance fromNbt(StatusEffect type, NbtCompound nbt) {
        float amp = nbt.getFloat("Amplifier");
        float dur = nbt.getFloat("Duration");

        boolean ambient = nbt.getBoolean("Ambient");
        boolean particles = true;
        if (nbt.contains("ShowParticles", 1)) {
            particles = nbt.getBoolean("ShowParticles");
        }

        boolean icon = particles;
        if (nbt.contains("ShowIcon", 1)) {
            icon = nbt.getBoolean("ShowIcon");
        }

        return new PotionEffectInstance(type, amp, dur, ambient, particles, icon);
    }
}
