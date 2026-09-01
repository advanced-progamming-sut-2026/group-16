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
    public static final String BURN_PATH =
            "768/INITIAL/EFFECTS/PLANT_BURNT/PLANT_BURNT.PAM";
    public static final String BURN_CLIP = "animation";
    public static final String BONE_HIT_PATH =
            "768/INITIAL/EFFECTS/ZOMBIE_EGYPT_TOMBRAISER_BONE_HIT/ZOMBIE_EGYPT_TOMBRAISER_BONE_HIT.PAM";
    public static final String BONE_HIT_CLIP = "animation";
    public static final String BONE_PROJECTILE_IMAGE = "IMAGE_ZOMBIE_BONE_PROJECTILE";
    public static final String LASER_PATH =
            "768/FULL/EFFECTS/CRYSTALSKULL_BEAM/CRYSTALSKULL_BEAM.PAM";
    public static final String LASER_CLIP = "laser_beam";
    public static final String ICE_PATH =
            "768/FULL/EFFECTS/ICESHROOM_FX/ICESHROOM_FX.PAM";
    public static final String ICE_CLIP = "animation";
    public static final String CITRON_PF_LIGHTNING_PATH =
            "768/FULL/EFFECTS/CITRON_PLANTFOOD_LIGHTNING_CHARGE/CITRON_PLANTFOOD_LIGHTNING_CHARGE.PAM";
    public static final String CITRON_PF_LIGHTNING_CLIP = "Citron_Plantfood_Lightning_Charge";
    public static final String CITRON_PF_SHOCK_PATH =
            "768/FULL/EFFECTS/CITRON_PLANTFOOD_SHOCK/CITRON_PLANTFOOD_SHOCK.PAM";
    public static final String CITRON_PF_SHOCK_CLIP = "animation";
    public static final String CITRON_PF_HIT_PATH =
            "768/FULL/EFFECTS/CITRON_PLANTFOOD_HIT/CITRON_PLANTFOOD_HIT.PAM";
    public static final String CITRON_PF_HIT_CLIP = "animation";
    public static final String SCORCH_IMAGE =
            "IMAGE_EFFECTS_ESCAPEROOT_EXPLOSION_CHERRYBOMB_REAR_MC_CHERRYBOMB_EXPLOSION_SCORCH";
    private static final float CHERRY_CANVAS_HEIGHT = 500f;
    private static final float MINE_CANVAS_HEIGHT = 390f;
    private static final float CHERRY_GROUND = 0.1f;
    private static final float MINE_GROUND = 0.18f;

    private ExplosionLooks() {
    }

    public static String path(LawnBurst.Kind kind) {
        return switch (kind == null ? LawnBurst.Kind.GENERIC : kind) {
            case MINE -> MINE_PATH;
            case BURN -> BURN_PATH;
            case BONE_HIT -> BONE_HIT_PATH;
            case LASER -> LASER_PATH;
            case ICE -> ICE_PATH;
            case CITRON_PF_LIGHTNING -> CITRON_PF_LIGHTNING_PATH;
            case CITRON_PF_SHOCK -> CITRON_PF_SHOCK_PATH;
            case CITRON_PF_HIT -> CITRON_PF_HIT_PATH;
            case CHERRY, GENERIC -> CHERRY_PATH;
        };
    }

    public static String rearPath(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.CHERRY || kind == LawnBurst.Kind.GENERIC) {
            return CHERRY_REAR_PATH;
        }
        return null;
    }

    public static String clip(LawnBurst.Kind kind) {
        return switch (kind == null ? LawnBurst.Kind.GENERIC : kind) {
            case MINE -> MINE_CLIP;
            case BURN -> BURN_CLIP;
            case BONE_HIT -> BONE_HIT_CLIP;
            case LASER -> LASER_CLIP;
            case ICE -> ICE_CLIP;
            case CITRON_PF_LIGHTNING -> CITRON_PF_LIGHTNING_CLIP;
            case CITRON_PF_SHOCK -> CITRON_PF_SHOCK_CLIP;
            case CITRON_PF_HIT -> CITRON_PF_HIT_CLIP;
            case CHERRY, GENERIC -> CHERRY_CLIP;
        };
    }

    public static float scale(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE || kind == LawnBurst.Kind.BURN
                || kind == LawnBurst.Kind.BONE_HIT) {
            return 1.0f;
        }
        if (kind == LawnBurst.Kind.LASER) {
            return 1.15f;
        }
        if (kind == LawnBurst.Kind.CITRON_PF_LIGHTNING
                || kind == LawnBurst.Kind.CITRON_PF_SHOCK
                || kind == LawnBurst.Kind.CITRON_PF_HIT) {
            return 1.1f;
        }
        return 1.05f;
    }

    public static float liftY(LawnBurst.Kind kind, float scale) {
        if (kind == LawnBurst.Kind.BURN || kind == LawnBurst.Kind.BONE_HIT
                || kind == LawnBurst.Kind.LASER || kind == LawnBurst.Kind.ICE
                || kind == LawnBurst.Kind.CITRON_PF_LIGHTNING
                || kind == LawnBurst.Kind.CITRON_PF_SHOCK
                || kind == LawnBurst.Kind.CITRON_PF_HIT) {
            return 0f;
        }
        float canvas = kind == LawnBurst.Kind.MINE ? MINE_CANVAS_HEIGHT : CHERRY_CANVAS_HEIGHT;
        float ground = kind == LawnBurst.Kind.MINE ? MINE_GROUND : CHERRY_GROUND;
        return (0.5f - ground) * canvas * scale;
    }

    public static boolean hasScorch(LawnBurst.Kind kind) {
        return kind == LawnBurst.Kind.CHERRY || kind == LawnBurst.Kind.GENERIC;
    }

    public static float shakeSeconds(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE) {
            return 0.22f;
        }
        if (kind == LawnBurst.Kind.CHERRY || kind == LawnBurst.Kind.GENERIC) {
            return 0.4f;
        }
        return 0f;
    }

    public static float shakePixels(LawnBurst.Kind kind) {
        if (kind == LawnBurst.Kind.MINE) {
            return 4f;
        }
        if (kind == LawnBurst.Kind.CHERRY || kind == LawnBurst.Kind.GENERIC) {
            return 7f;
        }
        return 0f;
    }
}
