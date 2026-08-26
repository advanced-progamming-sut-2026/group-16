package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Map;


public final class ZombieClips {
    private static final Map<String, String[]> ABILITY_CLIPS = Map.of(
            "ZombieRa", new String[]{"power", "power_up"},
            "ZombieTombRaiser", new String[]{"power"},
            "ZombieGargantuar", new String[]{"smash_left", "fire"},
            "ZombieBeachFisherman", new String[]{"cast", "cast_loop", "idle"},
            "ZombiePiano", new String[]{"play", "play2", "idle"}
    );

    private final EntityAnimationCatalog catalog;

    public ZombieClips(EntityAnimationCatalog catalog) {
        this.catalog = catalog;
    }

    public EntityAnimationCatalog.ClipSpec idle(String alias) {
        return catalog.zombieClip(alias, "idle", "walk");
    }

    public EntityAnimationCatalog.ClipSpec appear(String alias) {
        String path = catalog.zombiePath(alias);
        for (String name : new String[]{"pop", "spawn", "appear", "rise"}) {
            if (catalog.hasClip(path, name)) {
                return catalog.zombieClip(alias, name);
            }
        }
        return null;
    }

    public EntityAnimationCatalog.ClipSpec boss(String alias, String logical) {
        return ZombossClips.spec(catalog, alias, logical);
    }

    public void applyBoss(PamActor actor, String alias, String logical, float scale) {
        ZombossClips.Sequence sequence = ZombossClips.sequence(logical);
        EntityAnimationCatalog.ClipSpec first = catalog.zombieClip(alias, sequence.first());
        if (sequence.follow() == null) {
            actor.setClip(first.path(), first.clip(), scale, sequence.loop());
            return;
        }
        EntityAnimationCatalog.ClipSpec follow = catalog.zombieClip(alias, sequence.follow());
        actor.playThen(first.path(), first.clip(), scale, follow.clip(), sequence.loop(), null);
    }

    public EntityAnimationCatalog.ClipSpec walk(String alias) {
        return catalog.zombieClip(alias, "walk", "idle");
    }

    public EntityAnimationCatalog.ClipSpec walkNewspaper(String alias) {
        return catalog.zombieClip(alias, "walk_newspaper", "walk", "idle");
    }

    public EntityAnimationCatalog.ClipSpec eat(String alias) {
        return catalog.zombieClip(alias, "eat", "walk", "idle");
    }

    public EntityAnimationCatalog.ClipSpec eatNewspaper(String alias) {
        return catalog.zombieClip(alias, "eat_newspaper", "eat", "walk", "idle");
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

    public String armorLayer(Armor armor) {
        if (armor == null) {
            return null;
        }
        String[] layers = catalog.armorLayers(armor.getType());
        if (layers == null || layers.length == 0) {
            layers = catalog.armorLayers(armor.getAlias());
        }
        if (layers == null || layers.length == 0) {
            return null;
        }
        int index = damageLayerIndex(armor);
        if (index >= layers.length) {
            index = layers.length - 1;
        }
        return layers[index];
    }

    private static int damageLayerIndex(Armor armor) {
        int max = armor.getMaxHealth();
        if (max <= 0) {
            return 2;
        }
        float ratio = armor.getHealth() / (float) max;
        if (ratio > 0.666f) {
            return 0;
        }
        if (ratio > 0.333f) {
            return 1;
        }
        return 2;
    }
}
