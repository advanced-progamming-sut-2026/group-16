package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.plant.ability.WasabiWhipAbility;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;

import java.util.concurrent.ThreadLocalRandom;


public final class WasabiWhipClips {
    public static final String PATH = "768/INITIAL/PLANT/WASABIWHIP/WASABIWHIP.PAM";
    public static final String IDLE = "idle";
    private static final String[] IDLE_CLIPS = {"idle", "idle2", "idle3"};

    private WasabiWhipClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle() {
        return new EntityAnimationCatalog.ClipSpec(PATH, IDLE);
    }

    public static EntityAnimationCatalog.ClipSpec randomIdle() {
        String clip = IDLE_CLIPS[ThreadLocalRandom.current().nextInt(IDLE_CLIPS.length)];
        return new EntityAnimationCatalog.ClipSpec(PATH, clip);
    }

    public static EntityAnimationCatalog.ClipSpec otherIdle(String current) {
        int index = ThreadLocalRandom.current().nextInt(IDLE_CLIPS.length - 1);
        for (String clip : IDLE_CLIPS) {
            if (!clip.equals(current)) {
                if (index == 0) {
                    return new EntityAnimationCatalog.ClipSpec(PATH, clip);
                }
                index--;
            }
        }
        return idle();
    }

    public static boolean isIdleClip(String clip) {
        if (clip == null) {
            return false;
        }
        for (String idle : IDLE_CLIPS) {
            if (idle.equals(clip)) {
                return true;
            }
        }
        return false;
    }

    public static EntityAnimationCatalog.ClipSpec attackRight() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack");
    }

    public static EntityAnimationCatalog.ClipSpec attackLeft() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack2");
    }

    public static EntityAnimationCatalog.ClipSpec attackBoth() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack3");
    }

    public static EntityAnimationCatalog.ClipSpec attackUpRight() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack4");
    }

    public static EntityAnimationCatalog.ClipSpec attackUpLeft() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack5");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodOn() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood_on");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodLoop() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood");
    }

    public static EntityAnimationCatalog.ClipSpec plantFoodOff() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "plantfood_off");
    }

    public static EntityAnimationCatalog.ClipSpec clipFor(WasabiWhipAbility.WhipStyle style) {
        return switch (style) {
            case LEFT -> attackLeft();
            case UP_RIGHT, DOWN_RIGHT -> attackUpRight();
            case UP_LEFT, DOWN_LEFT -> attackUpLeft();
            default -> attackRight();
        };
    }
}
