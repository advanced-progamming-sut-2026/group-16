package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;

public final class WallDamageSupport {

    private WallDamageSupport() {
    }

    public static String idleDamageClip(Plant plant) {
        return damageClip(plant, "idle_damage");
    }

    public static String damageClip(Plant plant) {
        return damageClip(plant, "damage");
    }

    private static String damageClip(Plant plant, String prefix) {
        int tier = damageTier(plant);
        if (tier <= 0) {
            return "damage".equals(prefix) ? "idle" : "idle";
        }
        return tier == 1 ? prefix : prefix + tier;
    }

    public static int damageTier(Plant plant) {
        if (plant == null || plant.getMaxHealth() <= 0) {
            return 0;
        }
        double ratio = plant.getHealth() / (double) plant.getMaxHealth();
        if (ratio > 0.66) {
            return 0;
        }
        if (ratio > 0.33) {
            return 1;
        }
        return 2;
    }
}
