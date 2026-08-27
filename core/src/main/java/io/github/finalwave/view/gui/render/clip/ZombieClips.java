package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Map;
import java.util.Set;


public final class ZombieClips {
    private static final Map<String, String[]> ABILITY_CLIPS = Map.ofEntries(
            Map.entry("ZombieRa", new String[]{"power", "power_up", "power_down"}),
            Map.entry("ZombieTombRaiser", new String[]{"power"}),
            Map.entry("ZombieGargantuar", new String[]{"smash_left", "fire"}),
            Map.entry("ZombieBeachFisherman", new String[]{"cast", "reel", "toss", "cast_loop", "idle"}),
            Map.entry("ZombiePiano", new String[]{"play", "play2", "idle"}),
            Map.entry("ZombieModernAllStar", new String[]{"kick", "tackle", "run"}),
            Map.entry("ZombieCrystalSkull", new String[]{"attack", "power", "power_up", "power_down"}),
            Map.entry("ZombieIceAgeHunter", new String[]{"throw"}),
            Map.entry("ZombieBeachOctopus", new String[]{"toss"}),
            Map.entry("ZombieWizard", new String[]{"sheep"}),
            Map.entry("ZombieDarkKing", new String[]{"special"}),
            Map.entry("ZombieDarkJuggler", new String[]{"spinup", "spin", "spin_walk", "spindown"}),
            Map.entry("ZombieIceAgeTroglobite", new String[]{"push"}),
            Map.entry("ZombieArcade", new String[]{"push"}),
            Map.entry("ZombieNewspaper", new String[]{"newspaper_defeat"}),
            Map.entry("ZombieProspector", new String[]{"blastoff", "fly", "land"}),
            Map.entry("ZombieImp", new String[]{"fly", "land"})
    );

    private static final Set<String> SPECIAL_CLIPS = Set.of(
            "smash_left", "fire", "kick", "tackle", "run",
            "power", "power_up", "power_down", "attack",
            "throw", "toss", "sheep", "special",
            "spinup", "spin", "spin_walk", "spindown",
            "cast", "reel", "push", "play", "play2",
            "newspaper_defeat", "blastoff", "fly", "land", "cannon_fire"
    );

    private static final Set<String> ONE_SHOT_CLIPS = Set.of(
            "smash_left", "fire", "kick", "tackle",
            "throw", "toss", "sheep", "special",
            "attack", "cast", "reel",
            "newspaper_defeat", "blastoff", "fly", "land", "cannon_fire"
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

    public EntityAnimationCatalog.ClipSpec named(String alias, String... names) {
        if (names == null || names.length == 0) {
            return idle(alias);
        }
        return catalog.zombieClip(alias, names);
    }

    public EntityAnimationCatalog.ClipSpec ability(String alias) {
        return ability(alias, null);
    }

    public EntityAnimationCatalog.ClipSpec ability(String alias, String presentationClip) {
        if (presentationClip != null && !presentationClip.isBlank() && isSpecial(presentationClip)) {
            String[] fallbacks = ABILITY_CLIPS.get(alias);
            if (fallbacks == null) {
                return catalog.zombieClip(alias, presentationClip);
            }
            String[] names = new String[fallbacks.length + 1];
            names[0] = presentationClip;
            System.arraycopy(fallbacks, 0, names, 1, fallbacks.length);
            return catalog.zombieClip(alias, names);
        }
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

    public static boolean isSpecial(String clip) {
        return clip != null && SPECIAL_CLIPS.contains(clip);
    }

    public static boolean isOneShot(String clip) {
        return clip != null && ONE_SHOT_CLIPS.contains(clip);
    }

    public static boolean isOneShot(String alias, String clip) {
        if ("ZombieTombRaiser".equals(alias) && "power".equals(clip)) {
            return true;
        }
        return isOneShot(clip);
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
