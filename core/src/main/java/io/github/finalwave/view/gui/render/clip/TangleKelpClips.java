package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class TangleKelpClips {
    public static final String PATH = "768/FULL/PLANT/TANGLEKELP/TANGLEKELP.PAM";

    private TangleKelpClips() {
    }

    public static EntityAnimationCatalog.ClipSpec idle() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "idle");
    }

    public static EntityAnimationCatalog.ClipSpec attackSubmerge() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack_submerge");
    }

    public static EntityAnimationCatalog.ClipSpec attack() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack");
    }

    public static EntityAnimationCatalog.ClipSpec attackEmerge() {
        return new EntityAnimationCatalog.ClipSpec(PATH, "attack_emerge");
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

    public static EntityAnimationCatalog.ClipSpec remoteAttack() {
        return attack();
    }
}
