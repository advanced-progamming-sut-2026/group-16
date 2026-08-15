package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class ZombieClips {
    private final EntityAnimationCatalog catalog;

    public ZombieClips(EntityAnimationCatalog catalog) {
        this.catalog = catalog;
    }

    public EntityAnimationCatalog.ClipSpec walk(String alias) {
        return catalog.zombieClip(alias, "walk");
    }

    public EntityAnimationCatalog.ClipSpec eat(String alias) {
        return catalog.zombieClip(alias, "eat");
    }

    public String path(String alias) {
        return catalog.zombiePath(alias);
    }

    public String armorPart(String armorType, String armorAlias) {
        String fromType = catalog.armorPart(armorType);
        if (fromType != null) {
            return fromType;
        }
        return catalog.armorPart(armorAlias);
    }
}
