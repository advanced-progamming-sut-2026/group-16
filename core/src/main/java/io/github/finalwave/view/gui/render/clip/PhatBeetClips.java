package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class PhatBeetClips {
    public static final String PATH = "768/FULL/PLANT/PHATBEETS/PHATBEETS.PAM";
    public static final String ATTACK_PULSE_PATH =
            "768/FULL/EFFECTS/PHATBEETS_ATTACK_PULSE/PHATBEETS_ATTACK_PULSE.PAM";
    public static final String PLANT_FOOD_PULSE_PATH =
            "768/FULL/EFFECTS/PHATBEETS_PF_PULSE/PHATBEETS_PF_PULSE.PAM";
    public static final String TILE_HIT_PATH =
            "768/FULL/EFFECTS/PHATBEETS_TILE_HIT/PHATBEETS_TILE_HIT.PAM";
    public static final String EFFECT_CLIP = "animation";

    private PhatBeetClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "idle");
    }

    public static EntityAnimationCatalog.ClipSpec attack() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack");
    }

    public static EntityAnimationCatalog.ClipSpec plantFood() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood");
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
}
