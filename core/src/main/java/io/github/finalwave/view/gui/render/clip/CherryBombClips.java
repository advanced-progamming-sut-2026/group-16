package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class CherryBombClips {
    public static final float IDLE_SECONDS = 1.0f;
    public static final float ATTACK_SECONDS = 0.7f;
    public static final float DETONATION_FX_SECONDS = IDLE_SECONDS + ATTACK_SECONDS;
    public static final String EXPLOSION_CLIP = "explosion";
    public static final String PLANT_NAME = "Cherry Bomb";

    private static final String EXPLOSION_TOP_PATH =
            "768/FULL/EFFECTS/CHERRYBOMB_EXPLOSION_TOP/CHERRYBOMB_EXPLOSION_TOP.PAM";
    private static final String EXPLOSION_REAR_PATH =
            "768/FULL/EFFECTS/CHERRYBOMB_EXPLOSION_REAR/CHERRYBOMB_EXPLOSION_REAR.PAM";

    private CherryBombClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "idle", "attack");
    }

    public static EntityAnimationCatalog.ClipSpec attack(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "attack", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec explosionTop() {
        return new EntityAnimationCatalog.ClipSpec(EXPLOSION_TOP_PATH, EXPLOSION_CLIP);
    }

    public static EntityAnimationCatalog.ClipSpec explosionRear() {
        return new EntityAnimationCatalog.ClipSpec(EXPLOSION_REAR_PATH, EXPLOSION_CLIP);
    }
}
