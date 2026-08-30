package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class DoomShroomClips {
    public static final String PLANT_PATH =
            "768/FULL/PLANT/DOOMSHROOM/DOOMSHROOM.PAM";

    private DoomShroomClips() {
    }

    public static EntityAnimationCatalog.ClipSpec stage1Spawn() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage1_spawn");
    }

    public static EntityAnimationCatalog.ClipSpec stage1Idle() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage1_idle");
    }

    public static EntityAnimationCatalog.ClipSpec stage1Explode() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage1_explode");
    }

    public static EntityAnimationCatalog.ClipSpec stage1Transform() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage1_transform");
    }

    public static EntityAnimationCatalog.ClipSpec stage2Idle() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage2_idle");
    }

    public static EntityAnimationCatalog.ClipSpec stage2Idle2() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage2_idle2");
    }

    public static EntityAnimationCatalog.ClipSpec stage2Explode() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage2_explode");
    }

    public static EntityAnimationCatalog.ClipSpec stage2Transform() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage2_transform");
    }

    public static EntityAnimationCatalog.ClipSpec stage3Idle() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage3_idle");
    }

    public static EntityAnimationCatalog.ClipSpec stage3Idle2() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage3_idle2");
    }

    public static EntityAnimationCatalog.ClipSpec stage3Explode() {
        return new EntityAnimationCatalog.ClipSpec(PLANT_PATH, "stage3_explode");
    }

    public static EntityAnimationCatalog.ClipSpec idle(int growthStage, boolean alert) {
        return switch (Math.max(0, Math.min(2, growthStage))) {
            case 0 -> stage1Idle();
            case 1 -> alert ? stage2Idle2() : stage2Idle();
            default -> alert ? stage3Idle2() : stage3Idle();
        };
    }

    public static EntityAnimationCatalog.ClipSpec explode(int growthStage) {
        return switch (Math.max(0, Math.min(2, growthStage))) {
            case 0 -> stage1Explode();
            case 1 -> stage2Explode();
            default -> stage3Explode();
        };
    }

    public static EntityAnimationCatalog.ClipSpec transform(int fromStage) {
        return fromStage <= 0 ? stage1Transform() : stage2Transform();
    }
}
