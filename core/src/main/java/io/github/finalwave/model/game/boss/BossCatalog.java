package io.github.finalwave.model.game.boss;

import io.github.finalwave.model.adventure.ChapterId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class BossCatalog {

    public static final int MAX_HEALTH = 3600;
    public static final int ROW_SPAN = 2;
    public static final int INTRO_TICKS = 26;
    public static final int DARK_INTRO_TICKS = 104;
    public static final int IDLE_MIN_TICKS = 40;
    public static final int IDLE_MAX_TICKS = 80;
    public static final int MISSILE_START_TICKS = 33;
    public static final int MISSILE_ICE_START_TICKS = 35;
    public static final int MISSILE_LAUNCH_TICKS = 18;
    public static final int MISSILE_DELAY_TICKS = 5;
    public static final int MISSILE_FLIGHT_TICKS = 15;
    public static final int FIRE_BOMB_TICKS = 18;
    public static final int FIRE_BOMB_LOOP_TICKS = 11;
    public static final int FIRE_BOMB_END_TICKS = 9;
    public static final int FIREBALL_FLIGHT_TICKS = 20;
    public static final int FIRE_ATTACK_TICKS = 18;
    public static final int FIRE_ATTACK_LOOP_TICKS = 8;
    public static final int FIRE_ATTACK_END_TICKS = 8;
    public static final int ICE_INTRO_TICKS = 47;
    public static final int WIND_START_TICKS = 10;
    public static final int WIND_TICKS = 29;
    public static final int ICE_WIND_FROST_STACKS = 3;
    public static final int GLACIER_STRIKE_TICKS = 20;
    public static final int GLACIER_TICKS = 63;
    public static final int FROZEN_ZOMBIE_TICKS = 600;
    public static final int PORTAL_START_TICKS = 23;
    public static final int PORTAL_LOOP_TICKS = 12;
    public static final int PORTAL_END_TICKS = 16;
    public static final int LANE_SWITCH_TICKS = 13;

    private BossCatalog() {
    }

    public static String alias(ChapterId chapter) {
        if (chapter == null) {
            return "ZombieEgyptZomboss";
        }
        return switch (chapter) {
            case ANCIENT_EGYPT -> "ZombieEgyptZomboss";
            case DARK_AGES -> "ZombieDarkZomboss";
            case FROSTBITE_CAVES -> "ZombieIceageZomboss";
            case BIG_WAVE_BEACH -> "ZombieBeachZomboss";
        };
    }

    public static boolean allowsSummon(ChapterId chapter) {
        return chapter != ChapterId.FROSTBITE_CAVES;
    }

    public static boolean allowsLaneSwitch(ChapterId chapter) {
        return chapter != ChapterId.FROSTBITE_CAVES;
    }

    public static int introTicks(ChapterId chapter) {
        if (chapter == ChapterId.DARK_AGES) {
            return DARK_INTRO_TICKS;
        }
        if (chapter == ChapterId.FROSTBITE_CAVES) {
            return ICE_INTRO_TICKS;
        }
        return INTRO_TICKS;
    }

    public static List<String> conveyorPlants(ChapterId chapter, Collection<String> available) {
        List<String> preferred = switch (chapter == null ? ChapterId.ANCIENT_EGYPT : chapter) {
            case ANCIENT_EGYPT -> List.of(
                    "Peashooter", "Repeater", "Cabbage-pult", "Kernel-pult",
                    "Iceberg Lettuce", "Wall-nut", "Potato Mine", "Bonk Choy");
            case DARK_AGES -> List.of(
                    "Puff-shroom", "Fume-shroom", "Sun-shroom", "Grave Buster",
                    "Peashooter", "Wall-nut", "Iceberg Lettuce", "Hypno-shroom");
            case FROSTBITE_CAVES -> List.of(
                    "Hot Potato", "Peashooter", "Repeater", "Cabbage-pult",
                    "Pepper-pult", "Wall-nut", "Kernel-pult", "Threepeater");
            case BIG_WAVE_BEACH -> List.of(
                    "Lily Pad", "Tangle Kelp", "Peashooter", "Repeater",
                    "Wall-nut", "Kernel-pult", "Cabbage-pult", "Threepeater");
        };
        if (available == null || available.isEmpty()) {
            return preferred;
        }
        List<String> filtered = new ArrayList<>();
        for (String plant : preferred) {
            if (containsIgnoreCase(available, plant)) {
                filtered.add(plant);
            }
        }
        return filtered.isEmpty() ? preferred : List.copyOf(filtered);
    }

    public static List<String> summonPool(ChapterId chapter) {
        return switch (chapter == null ? ChapterId.ANCIENT_EGYPT : chapter) {
            case ANCIENT_EGYPT -> List.of(
                    "ZombieDefault", "ZombieArmor1", "ZombieArmor2",
                    "ZombieExplorer", "ZombieTombRaiser");
            case DARK_AGES -> List.of(
                    "ZombieDefault", "ZombieArmor1", "ZombieArmor2",
                    "ZombieDarkImpDragon", "ZombieDarkJuggler");
            case BIG_WAVE_BEACH -> List.of(
                    "ZombieDefault", "ZombieArmor1", "ZombieBeachSnorkel", "ZombieImp");
            case FROSTBITE_CAVES -> List.of();
        };
    }

    public static String impAlias(ChapterId chapter) {
        if (chapter == ChapterId.DARK_AGES) {
            return "ZombieDarkImpDragon";
        }
        return "ZombieImp";
    }

    private static boolean containsIgnoreCase(Collection<String> names, String plant) {
        for (String name : names) {
            if (name != null && name.equalsIgnoreCase(plant)) {
                return true;
            }
        }
        return plant != null && names.contains(plant.toLowerCase(Locale.ROOT));
    }
}
