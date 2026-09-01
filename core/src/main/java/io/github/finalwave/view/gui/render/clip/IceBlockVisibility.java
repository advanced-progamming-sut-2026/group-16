package io.github.finalwave.view.gui.render.clip;

import java.util.HashMap;
import java.util.Map;


public final class IceBlockVisibility {
    private static final int PLANT_ICE_MAX_HEALTH = 600;

    private IceBlockVisibility() {
    }

    public static Map<String, Boolean> fromPlantIceHealth(int health) {
        int clamped = Math.max(0, health);
        int stage;
        if (clamped > 400) {
            stage = 0;
        } else if (clamped > 200) {
            stage = 2;
        } else {
            stage = 4;
        }
        return singleDamageStage(PlantClips.ICE_BLOCK_DAMAGE_PARTS, stage);
    }

    public static Map<String, Boolean> fromFreezeTicksRemaining(int remainingTicks) {
        int remaining = Math.max(0, remainingTicks);
        int stage;
        if (remaining >= 300) {
            stage = 0;
        } else if (remaining >= 200) {
            stage = 1;
        } else if (remaining >= 120) {
            stage = 2;
        } else if (remaining >= 80) {
            stage = 3;
        } else if (remaining >= 50) {
            stage = 4;
        } else {
            stage = 5;
        }
        return singleDamageStage(ZombossClips.ICE_BLOCK_DAMAGE_PARTS, stage);
    }

    public static int plantIceMaxHealth() {
        return PLANT_ICE_MAX_HEALTH;
    }

    private static Map<String, Boolean> singleDamageStage(String[] parts, int activeIndex) {
        Map<String, Boolean> vis = new HashMap<>();
        int index = Math.max(0, Math.min(parts.length - 1, activeIndex));
        for (int i = 0; i < parts.length; i++) {
            vis.put(parts[i], i == index);
        }
        return vis;
    }
}
