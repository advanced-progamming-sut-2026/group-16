package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;

import java.util.Map;


public final class ZombieClips {
    private static final Map<String, String[]> ABILITY_CLIPS = Map.of(
            "ZombieRa", new String[]{"power", "power_up"},
            "ZombieTombRaiser", new String[]{"power"},
            "ZombieGargantuar", new String[]{"smash_left", "fire"}
    );

    private final EntityAnimationCatalog catalog;

    public ZombieClips(EntityAnimationCatalog catalog) {
        this.catalog = catalog;
    }

    public EntityAnimationCatalog.ClipSpec idle(String alias) {
        return catalog.zombieClip(alias, "idle", "walk");
    }

    public EntityAnimationCatalog.ClipSpec walk(String alias) {
        return catalog.zombieClip(alias, "walk", "idle");
    }

    public EntityAnimationCatalog.ClipSpec eat(String alias) {
        return catalog.zombieClip(alias, "eat", "walk", "idle");
    }

    public EntityAnimationCatalog.ClipSpec ability(String alias) {
        String[] names = ABILITY_CLIPS.get(alias);
        if (names == null) {
            return idle(alias);
        }
        return catalog.zombieClip(alias, names);
    }

    public EntityAnimationCatalog.ClipSpec die(String alias) {
        return catalog.zombieClip(alias, "die", "idle");
    }

    public EntityAnimationCatalog.ClipSpec particles(String alias) {
        String path = catalog.zombiePath(alias);
        if (!catalog.hasClip(path, "particles")) {
            return null;
        }
        return catalog.zombieClip(alias, "particles");
    }

    public boolean hasDie(String alias) {
        return catalog.hasClip(catalog.zombiePath(alias), "die");
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
