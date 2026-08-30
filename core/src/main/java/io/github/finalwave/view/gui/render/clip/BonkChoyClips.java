package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class BonkChoyClips {
    public static final String PATH = "768/INITIAL/PLANT/BONKCHOY/BONKCHOY.PAM";

    private BonkChoyClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "idle");
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

    public static EntityAnimationCatalog.ClipSpec clipFor(
            io.github.finalwave.model.game.entity.plant.ability.BonkChoyAbility.PunchStyle style) {
        return switch (style) {
            case LEFT -> attackLeft();
            case BOTH -> attackBoth();
            case UP_RIGHT -> attackUpRight();
            case UP_LEFT -> attackUpLeft();
            default -> attackRight();
        };
    }
}
