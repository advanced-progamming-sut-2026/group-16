package io.github.finalwave.model.game.entity.plant;

import io.github.finalwave.model.definition.PlantRegistry;

import java.util.HashMap;
import java.util.Map;

public final class PlantArmor {

    private int health;
    private final int maxHealth;
    private final boolean smashAbsorb;

    public PlantArmor(int maxHealth) {
        this(maxHealth, false);
    }

    public PlantArmor(int maxHealth, boolean smashAbsorb) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.smashAbsorb = smashAbsorb;
    }

    public int absorb(int damage) {
        if (damage <= 0 || health <= 0) {
            return damage;
        }
        int absorbed = Math.min(health, damage);
        health -= absorbed;
        return damage - absorbed;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean absorbsSmash() {
        return smashAbsorb;
    }

    public boolean absorbSmash() {
        if (!smashAbsorb || health <= 0) {
            return false;
        }
        health = 0;
        return true;
    }

    public static final class PlantCooldownTracker {

        private final Map<String, Integer> ticksRemainingByPlantName = new HashMap<>();

        public boolean isReady(String plantName) {
            return ticksRemainingByPlantName.getOrDefault(plantName, 0) <= 0;
        }

        public int ticksRemaining(String plantName) {
            return Math.max(0, ticksRemainingByPlantName.getOrDefault(plantName, 0));
        }

        public void startCooldown(String plantName, double rechargeSeconds, int ticksPerSecond) {
            int ticks = (int) Math.ceil(rechargeSeconds * ticksPerSecond);
            if (ticks <= 0) {
                ticksRemainingByPlantName.remove(plantName);
                return;
            }
            ticksRemainingByPlantName.put(plantName, ticks);
        }

        public void resetCategory(PlantRegistry registry, String category) {
            if (registry == null || category == null) {
                return;
            }
            for (var def : registry.getByCategory(category)) {
                ticksRemainingByPlantName.remove(def.getName());
            }
        }

        public void resetAll() {
            ticksRemainingByPlantName.clear();
        }

        public void tick() {
            ticksRemainingByPlantName.replaceAll((name, ticks) -> Math.max(0, ticks - 1));
            ticksRemainingByPlantName.entrySet().removeIf(entry -> entry.getValue() <= 0);
        }
    }
}
