package io.github.finalwave.model.game.entity.zombie;

import io.github.finalwave.model.definition.armor.ArmorDefinition;

public final class Armor {

    private final String alias;
    private final String type;
    private final int maxHealth;
    private final boolean magneticRemovable;
    private final boolean helm;
    private int health;

    public Armor(String alias, String type, int maxHealth, boolean magneticRemovable, boolean helm) {
        this.alias = alias;
        this.type = type;
        this.maxHealth = Math.max(0, maxHealth);
        this.health = this.maxHealth;
        this.magneticRemovable = magneticRemovable;
        this.helm = helm;
    }

    public static Armor fromDefinition(ArmorDefinition def) {
        return new Armor(def.getAlias(), def.getArmorType(), def.getBaseHealth(), def.isMagnetic(), def.isHelm());
    }

    public int absorbDamage(int amount) {
        if (amount <= 0 || health <= 0) {
            return amount;
        }
        int absorbed = Math.min(health, amount);
        health -= absorbed;
        return amount - absorbed;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public void destroy() {
        health = 0;
    }

    public void restoreHealth(int amount) {
        health = Math.max(0, Math.min(maxHealth, amount));
    }

    public String getAlias() {
        return alias;
    }

    public String getType() {
        return type;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isMagneticRemovable() {
        return magneticRemovable;
    }

    public boolean isHelm() {
        return helm;
    }

    @Override
    public String toString() {
        return "Armor[%s hp=%d/%d]".formatted(alias, health, maxHealth);
    }
}