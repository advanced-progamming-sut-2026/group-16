package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.LawnBurst;

public final class ExplosionLooks {
    public static final String CHERRY_PATH =
            "768/FULL/EFFECTS/CHERRYBOMB_EXPLOSION_TOP/CHERRYBOMB_EXPLOSION_TOP.PAM";
    public static final String CHERRY_REAR_PATH =
            "768/FULL/EFFECTS/CHERRYBOMB_EXPLOSION_REAR/CHERRYBOMB_EXPLOSION_REAR.PAM";
    public static final String CHERRY_CLIP = "explosion";
    public static final String MINE_PATH =
            "768/INITIAL/EFFECTS/POTATOMINE_EXPLOSION/POTATOMINE_EXPLOSION.PAM";
    public static final String MINE_CLIP = "animation";
    public static final String SCORCH_IMAGE =
            "IMAGE_EFFECTS_ESCAPEROOT_EXPLOSION_CHERRYBOMB_REAR_MC_CHERRYBOMB_EXPLOSION_SCORCH";
    private static final float CHERRY_CANVAS_HEIGHT = 500f;
    private static final float MINE_CANVAS_HEIGHT = 390f;
    private static final float CHERRY_GROUND = 0.1f;
    private static final float MINE_GROUND = 0.18f;

    private ExplosionLooks() {
    }

    public static String path(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE) {
            return MINE_PATH;
        }
        return CHERRY_PATH;
    }

    public static String rearPath(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE) {
            return null;
        }
        return CHERRY_REAR_PATH;
    }

    public static String clip(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE) {
            return MINE_CLIP;
        }
        return CHERRY_CLIP;
    }

    public static float scale(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE) {
            return 1.0f;
        }
        return 1.05f;
    }

    public static float liftY(LawnBurst.Kind kind, float scale) {
        float canvas = kind == LawnBurst.Kind.MINE ? MINE_CANVAS_HEIGHT : CHERRY_CANVAS_HEIGHT;
        float ground = kind == LawnBurst.Kind.MINE ? MINE_GROUND : CHERRY_GROUND;
        return (0.5f - ground) * canvas * scale;
    }

    public static boolean hasScorch(LawnBurst.Kind kind) {
        return kind != LawnBurst.Kind.MINE;
    }

    public static float shakeSeconds(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE) {
            return 0.22f;
        }
        return 0.4f;
    }

    public static float shakePixels(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE) {
            return 4f;
        }
        return 7f;
    }
}
