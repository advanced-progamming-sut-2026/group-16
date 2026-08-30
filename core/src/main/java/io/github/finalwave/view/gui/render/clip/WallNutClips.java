package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


public final class WallNutClips {
    public static final String PATH = "768/INITIAL/PLANT/WALLNUT/WALLNUT.PAM";
    public static final String IDLE = "idle";
    public static final String IDLE2 = "idle2";
    public static final String DAMAGE = "damage";
    public static final String DAMAGE2 = "damage2";
    public static final String DAMAGE3 = "damage3";
    public static final String PLANT_FOOD_ON = "plantfood_on";
    public static final String PLANT_FOOD = "plantfood";
    public static final String PLANT_FOOD2 = "plantfood2";
    public static final String PLANT_FOOD3 = "plantfood3";
    public static final String ARMOR_STATES = "_wallnut_armor_states";
    public static final String ARMOR_1 = "wallnut_plantfood_armor_01";
    public static final String ARMOR_2 = "wallnut_plantfood_armor_02";
    public static final String ARMOR_3 = "wallnut_plantfood_armor_03";

    private static final String[] IDLE_CLIPS = {IDLE, IDLE2};
    private static final String[] ARMOR_PARTS = {ARMOR_1, ARMOR_2, ARMOR_3};

    private WallNutClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle() {
        return new EntityAnimationCatalog.ClipSpec(PATH, IDLE);
    }

    public static EntityAnimationCatalog.ClipSpec randomIdle() {
        String clip = IDLE_CLIPS[ThreadLocalRandom.current().nextInt(IDLE_CLIPS.length)];
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static EntityAnimationCatalog.ClipSpec otherIdle(String current) {
        if (IDLE.equals(current)) {
            return new EntityAnimationCatalog.ClipSpec(PATH, IDLE2);
        }
        if (IDLE2.equals(current)) {
            return new EntityAnimationCatalog.ClipSpec(PATH, IDLE);
        }
        return randomIdle();
    }

    public static boolean isIdleClip(String clip) {
        return IDLE.equals(clip) || IDLE2.equals(clip);
    }

    public static EntityAnimationCatalog.ClipSpec damage(int stage) {
        String clip = switch (Math.max(1, Math.min(3, stage))) {
            case 2 -> DAMAGE2;
            case 3 -> DAMAGE3;
            default -> DAMAGE;
        };
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static boolean isDamageClip(int stage, String clip) {
        return damage(stage).clip().equals(clip);
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodOn() {
        return new EntityAnimationCatalog.ClipSpec(PATH, PLANT_FOOD_ON);
    }

    public static EntityAnimationCatalog.ClipSpec plantFood() {
        return plantFoodLoop(1);
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodLoop(int stage) {
        String clip = switch (Math.max(1, Math.min(3, stage))) {
            case 2 -> PLANT_FOOD2;
            case 3 -> PLANT_FOOD3;
            default -> PLANT_FOOD;
        };
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static boolean isPlantFoodClip(String clip) {
        return PLANT_FOOD_ON.equals(clip)
                || PLANT_FOOD.equals(clip)
                || PLANT_FOOD2.equals(clip)
                || PLANT_FOOD3.equals(clip);
    }

    public static String armorPart(int stage) {
        return switch (Math.max(1, Math.min(3, stage))) {
            case 2 -> ARMOR_2;
            case 3 -> ARMOR_3;
            default -> ARMOR_1;
        };
    }

    public static Map<String, Boolean> armorVisibility(int stage) {
        String active = armorPart(stage);
        Map<String, Boolean> vis = new HashMap<>();
        vis.put(ARMOR_STATES, Boolean.TRUE);
        for (String part : ARMOR_PARTS) {
            vis.put(part, part.equals(active));
        }
        return vis;
    }
}
