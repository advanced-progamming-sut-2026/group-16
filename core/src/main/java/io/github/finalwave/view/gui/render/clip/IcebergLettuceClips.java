package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class IcebergLettuceClips {
    public static final String PATH = "768/INITIAL/PLANT/ICEBURG/ICEBURG.PAM";

    private IcebergLettuceClips() {
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
}
