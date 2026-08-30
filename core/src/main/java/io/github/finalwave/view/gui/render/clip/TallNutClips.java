package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class TallNutClips {
    public static final String PATH = "768/FULL/PLANT/TALLNUT/TALLNUT.PAM";
    public static final String IDLE = "idle";
    public static final String DAMAGE = "damage";
    public static final String DAMAGE2 = "damage2";

    private TallNutClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle() {
        return new EntityAnimationCatalog.ClipSpec(PATH, IDLE);
    }

    public static boolean isIdleClip(String clip) {
        return IDLE.equals(clip);
    }

    public static EntityAnimationCatalog.ClipSpec damage(int stage) {
        String clip = stage >= 2 ? DAMAGE2 : DAMAGE;
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static boolean isDamageClip(int stage, String clip) {
        return damage(stage).clip().equals(clip);
    }
}
