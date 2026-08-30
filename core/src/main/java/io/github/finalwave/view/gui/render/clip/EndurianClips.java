package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


public final class EndurianClips {
    public static final String PATH = "768/FULL/PLANT/ENDURIAN/ENDURIAN.PAM";
    public static final String IDLE = "idle";
    public static final String IDLE2 = "idle2";
    public static final String DAMAGE = "damage";
    public static final String DAMAGE2 = "damage2";
    public static final String DAMAGE3 = "damage3";
    public static final String ATTACK_START = "attack_start";
    public static final String ATTACK_LOOP = "attack_loop";
    public static final String ATTACK_END = "attack_end";
    public static final String PLANT_FOOD_ON = "plantfood_on";
    public static final String ARMOR_PARENT = "endurian_plantfood_armor";
    public static final String ARMOR_1 = "armor_damage_1";
    public static final String ARMOR_2 = "armor_damage_2";
    public static final String ARMOR_3 = "armor_damage_3";
    public static final String ARMOR_1_ATTACK = "armor_damage_1_attack";
    public static final String ARMOR_2_ATTACK = "armor_damage_2_attack";
    public static final String ARMOR_3_ATTACK = "armor_damage_3_attack";

    private static final String[] IDLE_CLIPS = {IDLE, IDLE2};
    private static final String[] ARMOR_IDLE = {ARMOR_1, ARMOR_2, ARMOR_3};
    private static final String[] ARMOR_ATTACK = {ARMOR_1_ATTACK, ARMOR_2_ATTACK, ARMOR_3_ATTACK};

    private EndurianClips() {
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
        String clip = switch (clampDamage(stage)) {
            case 2 -> DAMAGE2;
            case 3 -> DAMAGE3;
            default -> DAMAGE;
        };
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static boolean isDamageClip(int stage, String clip) {
        return damage(stage).clip().equals(clip);
    }

    public static EntityAnimationCatalog.ClipSpec attackStart(int stage) {
        return attackClip(stage, ATTACK_START, "attack_start_damage", "attack_start_damage2", "attack_start_damage3");
    }

    public static EntityAnimationCatalog.ClipSpec attackLoop(int stage) {
        return attackClip(stage, ATTACK_LOOP, "attack_loop_damage", "attack_loop_damage2", "attack_loop_damage3");
    }

    public static EntityAnimationCatalog.ClipSpec attackEnd(int stage) {
        return attackClip(stage, ATTACK_END, "attack_end_damage", "attack_end_damage2", "attack_end_damage3");
    }

    public static boolean isAttackEndClip(int stage, String clip) {
        return attackEnd(stage).clip().equals(clip);
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodOn() {
        return new EntityAnimationCatalog.ClipSpec(PATH, PLANT_FOOD_ON);
    }

    public static EntityAnimationCatalog.ClipSpec bodyIdle(int damageStage) {
        if (damageStage <= 0) {
            return idle();
        }
        return damage(damageStage);
    }

    public static String armorPart(int stage, boolean attacking) {
        int clamped = Math.max(1, Math.min(3, stage));
        if (attacking) {
            return switch (clamped) {
                case 2 -> ARMOR_2_ATTACK;
                case 3 -> ARMOR_3_ATTACK;
                default -> ARMOR_1_ATTACK;
            };
        }
        return switch (clamped) {
            case 2 -> ARMOR_2;
            case 3 -> ARMOR_3;
            default -> ARMOR_1;
        };
    }

    public static Map<String, Boolean> armorVisibility(int stage, boolean attacking) {
        String active = armorPart(stage, attacking);
        Map<String, Boolean> vis = new HashMap<>();
        vis.put(ARMOR_PARENT, Boolean.TRUE);
        for (String part : ARMOR_IDLE) {
            vis.put(part, !attacking && part.equals(active));
        }
        for (String part : ARMOR_ATTACK) {
            vis.put(part, attacking && part.equals(active));
        }
        return vis;
    }

    private static EntityAnimationCatalog.ClipSpec attackClip(
            int stage, String healthy, String damage, String damage2, String damage3) {
        String clip = switch (clampDamage(stage)) {
            case 1 -> damage;
            case 2 -> damage2;
            case 3 -> damage3;
            default -> healthy;
        };
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    private static int clampDamage(int stage) {
        return Math.max(0, Math.min(3, stage));
    }
}
