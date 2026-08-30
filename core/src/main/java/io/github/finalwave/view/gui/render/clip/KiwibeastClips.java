package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;

import java.util.concurrent.ThreadLocalRandom;


public final class KiwibeastClips {
    public static final String PATH = "768/INITIAL/PLANT/KIWIBEAST/KIWIBEAST.PAM";
    public static final String ATTACK_PULSE_PATH =
            "768/INITIAL/EFFECTS/KIWIBEAST_ATTACK_PULSE/KIWIBEAST_ATTACK_PULSE.PAM";
    public static final String PLANT_FOOD_PULSE_PATH =
            "768/INITIAL/EFFECTS/KIWIBEAST_PF_PULSE/KIWIBEAST_PF_PULSE.PAM";
    public static final String TILE_HIT_PATH =
            "768/INITIAL/EFFECTS/KIWIBEAST_TILE_HIT/KIWIBEAST_TILE_HIT.PAM";
    public static final String EFFECT_CLIP = "animation";

    private static final String[] STAGE1_IDLE = {"idle_stage1_", "idle_stage1_2", "idle_stage1_3"};
    private static final String[] STAGE2_IDLE = {"idle_stage2_", "idle_stage2_2", "idle_stage2_3"};
    private static final String[] STAGE3_IDLE = {"idle_stage3_", "idle_stage3_2", "idle_stage3_3"};

    private KiwibeastClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle(int stage) {
        String[] clips = idleClips(stage);
        return new EntityAnimationCatalog.ClipSpec(PATH, clips[0]);
    }

    public static EntityAnimationCatalog.ClipSpec randomIdle(int stage) {
        String[] clips = idleClips(stage);
        String clip = clips[ThreadLocalRandom.current().nextInt(clips.length)];
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static EntityAnimationCatalog.ClipSpec otherIdle(int stage, String current) {
        String[] clips = idleClips(stage);
        int index = ThreadLocalRandom.current().nextInt(clips.length - 1);
        for (String clip : clips) {
            if (!clip.equals(current)) {
                if (index == 0) {
                    return new EntityAnimationCatalog.ClipSpec(PATH, clip);
                }
                index--;
            }
        }
        return idle(stage);
    }

    public static boolean isIdleClip(int stage, String clip) {
        if (clip == null) {
            return false;
        }
        for (String idle : idleClips(stage)) {
            if (idle.equals(clip)) {
                return true;
            }
        }
        return false;
    }

    public static EntityAnimationCatalog.ClipSpec attack(int stage) {
        String clip = switch (clamp(stage)) {
            case 2 -> "attack_stage2";
            case 3 -> "attack_stage3";
            default -> "attack_stage1";
        };
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static EntityAnimationCatalog.ClipSpec growth(int fromStage) {
        String clip = fromStage <= 1 ? "growth_stage1" : "growth_stage2";
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static EntityAnimationCatalog.ClipSpec plantFood() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood_stage3");
    }

    public static EntityAnimationCatalog.ClipSpec attackPulse() {
        return new EntityAnimationCatalog.ClipSpec(ATTACK_PULSE_PATH, EFFECT_CLIP);
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodPulse() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_FOOD_PULSE_PATH, EFFECT_CLIP);
    }

    public static EntityAnimationCatalog.ClipSpec tileHit() {
        return new EntityAnimationCatalog.ClipSpec(TILE_HIT_PATH, EFFECT_CLIP);
    }

    private static String[] idleClips(int stage) {
        return switch (clamp(stage)) {
            case 2 -> STAGE2_IDLE;
            case 3 -> STAGE3_IDLE;
            default -> STAGE1_IDLE;
        };
    }

    private static int clamp(int stage) {
        return Math.max(1, Math.min(3, stage));
    }
}
