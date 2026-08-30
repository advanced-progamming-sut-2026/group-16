package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class JalapenoClips {
    public static final float IDLE_SECONDS = 0.6667f;
    public static final float ATTACK_SECONDS = 0.6667f;
    public static final String PLANT_NAME = "Jalapeno";
    public static final String PLANT_PATH =
            "768/INITIAL/PLANT/JALAPENO/JALAPENO.PAM";

    public static final String FIRE_PATH =
            "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM";
    public static final String FIRE_IDLE_CLIP = "idle";
    public static final String FIRE_IDLE2_CLIP = "idle2";
    public static final String FIRE_IDLE3_CLIP = "idle3";

    private JalapenoClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "idle", "attack");
    }

    public static EntityAnimationCatalog.ClipSpec attack(EntityAnimationCatalog catalog) {
        return catalog.plantClip(PLANT_NAME, "attack", "idle");
    }

    public static EntityAnimationCatalog.ClipSpec fireIdle() {
        return new EntityAnimationCatalog.ClipSpec(FIRE_PATH, FIRE_IDLE_CLIP);
    }

    public static EntityAnimationCatalog.ClipSpec fireIdle2() {
        return new EntityAnimationCatalog.ClipSpec(FIRE_PATH, FIRE_IDLE2_CLIP);
    }

    public static EntityAnimationCatalog.ClipSpec fireIdle3() {
        return new EntityAnimationCatalog.ClipSpec(FIRE_PATH, FIRE_IDLE3_CLIP);
    }
}
