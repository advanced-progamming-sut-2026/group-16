package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;

import java.util.concurrent.ThreadLocalRandom;


public final class ChomperClips {
    public static final String PATH = "768/INITIAL/PLANT/CHOMPER/CHOMPER.PAM";
    private static final String[] IDLE_CLIPS = {"idle", "idle2", "idle3", "idle4"};

    private ChomperClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "idle");
    }

    public static EntityAnimationCatalog.ClipSpec randomIdle() {
        String clip = IDLE_CLIPS[ThreadLocalRandom.current().nextInt(IDLE_CLIPS.length)];
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static EntityAnimationCatalog.ClipSpec bite() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "bite");
    }

    public static EntityAnimationCatalog.ClipSpec biteEnd() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "bite_end");
    }

    public static EntityAnimationCatalog.ClipSpec swallow() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "special");
    }

    public static EntityAnimationCatalog.ClipSpec chew() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "special_idle");
    }

    public static EntityAnimationCatalog.ClipSpec chewEnd() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "special_end");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodOn() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood_on");
    }

    public static EntityAnimationCatalog.ClipSpec plantFood() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodOff() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood_off");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodBurp() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood_burp");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodBurpEnd() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood_burp_end");
    }
}
