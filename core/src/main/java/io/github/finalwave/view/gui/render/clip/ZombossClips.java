package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;

public final class ZombossClips {

    public static final String EGYPT_MISSILE =
            "768/INITIAL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_EGYPT/ZOMBOSS_MISSILE_EXPLOSION_EGYPT.PAM";
    public static final String ICE_MISSILE =
            "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_ICEAGE/ZOMBOSS_MISSILE_EXPLOSION_ICEAGE.PAM";
    public static final String DARK_FIREBALL =
            "768/FULL/EFFECTS/ZOMBOSS_DARK_FIREBALL/ZOMBOSS_DARK_FIREBALL.PAM";
    public static final String SHARK =
            "768/FULL/EFFECTS/ZOMBOSS_SHARK_PROJECTILE/ZOMBOSS_SHARK_PROJECTILE.PAM";
    public static final String TURBINE =
            "768/FULL/EFFECTS/ZOMBOSS_TURBINE_WIND/ZOMBOSS_TURBINE_WIND.PAM";
    public static final String FIRE_TILE =
            "768/FULL/EFFECTS/SCORCHED_EARTH_TILE/SCORCHED_EARTH_TILE.PAM";
    public static final String FIRE_TILE_INTRO_CLIP = "animation";
    public static final String FIRE_TILE_CLIP = "animation2";
    public static final String GLACIER =
            "768/FULL/EFFECTS/ZOMBOSS_GLACIER_BLOCK/ZOMBOSS_GLACIER_BLOCK.PAM";
    public static final String CHILL_WIND =
            "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";
    public static final String ICE_BLOCK_ZOMBIE =
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM";
    public static final String ICE_BLOCK_ZOMBIE_SPAWN =
            "768/INITIAL/EFFECTS/ICEBLOOM_ICE_BLOCK_ZOMBIE_EFFECT/ICEBLOOM_ICE_BLOCK_ZOMBIE_EFFECT.PAM";
    public static final String ICE_BLOCK_ZOMBIE_SPAWN_CLIP = "animation";

    public record Sequence(String[] first, String[] follow, boolean loop) {
    }

    private ZombossClips() {
    }

    public static EntityAnimationCatalog.ClipSpec spec(EntityAnimationCatalog catalog, String alias, String logical) {
        Sequence sequence = sequence(logical);
        return catalog.zombieClip(alias, sequence.first());
    }

    public static Sequence sequence(String logical) {
        if (logical == null) {
            return new Sequence(new String[]{"idle"}, null, true);
        }
        return switch (logical) {
            case "intro" -> new Sequence(
                    new String[]{"intro", "Pre_Intro", "idle"},
                    new String[]{"idle"},
                    true);
            case "missile" -> new Sequence(
                    new String[]{"missile_start", "slingshot", "fire_bomb", "idle"},
                    new String[]{"rocket_launch", "idle"},
                    false);
            case "missile_launch" -> new Sequence(
                    new String[]{"rocket_launch", "slingshot", "idle"},
                    null,
                    false);
            case "slingshot" -> new Sequence(
                    new String[]{"slingshot", "idle"},
                    null,
                    false);
            case "portal" -> new Sequence(
                    new String[]{"zombie_portal_start", "summoning", "spawn", "idle"},
                    new String[]{"zombie_portal_loop", "idle"},
                    true);
            case "portal_end" -> new Sequence(
                    new String[]{"zombie_portal_end", "idle"},
                    null,
                    false);
            case "stun" -> new Sequence(
                    new String[]{"stun_start", "stun", "vulnerable", "idle"},
                    new String[]{"stun_loop", "vulnerable_loop", "idle"},
                    true);
            case "walk_forward" -> new Sequence(new String[]{"walk_forward", "idle"}, null, true);
            case "walk_backwards" -> new Sequence(new String[]{"walk_backwards", "idle"}, null, true);
            case "walk_up" -> new Sequence(new String[]{"walk_up", "idle"}, null, true);
            case "walk_down" -> new Sequence(new String[]{"walk_down", "idle"}, null, true);
            case "fire_bomb" -> new Sequence(
                    new String[]{"fire_bomb", "idle"},
                    new String[]{"fire_bomb_loop", "idle"},
                    true);
            case "fire_bomb_loop" -> new Sequence(new String[]{"fire_bomb_loop", "idle"}, null, true);
            case "fire_bomb_end" -> new Sequence(new String[]{"fire_bomb_end", "idle"}, null, false);
            case "fire" -> new Sequence(
                    new String[]{"fire_attack", "idle"},
                    new String[]{"fire_attack_idle", "idle"},
                    true);
            case "fire_end" -> new Sequence(new String[]{"fire_attack_end", "idle"}, null, false);
            case "wind" -> new Sequence(new String[]{"wind_1", "wind_2", "wind_3", "wind_4", "idle"}, null, false);
            case "glacier" -> new Sequence(new String[]{"glacier_column_1", "idle"}, null, false);
            case "glacier_1" -> new Sequence(new String[]{"glacier_column_1", "idle"}, null, false);
            case "glacier_2" -> new Sequence(new String[]{"glacier_column_2", "idle"}, null, false);
            case "glacier_3" -> new Sequence(new String[]{"glacier_column_3", "idle"}, null, false);
            case "glacier_4" -> new Sequence(new String[]{"glacier_column_4", "idle"}, null, false);
            case "glacier_5" -> new Sequence(new String[]{"glacier_column_5", "idle"}, null, false);
            case "glacier_6" -> new Sequence(new String[]{"glacier_column_6", "idle"}, null, false);
            case "suction" -> new Sequence(new String[]{"suction_loop", "suction_on", "idle"}, null, true);
            case "die" -> new Sequence(new String[]{"die", "die_idle", "idle"}, null, false);
            default -> new Sequence(new String[]{"idle"}, null, true);
        };
    }
}
