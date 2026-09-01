package io.github.finalwave.model.minigame.izombie;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class IZombieDuelCatalog {

    public static final int ROUND_SECONDS = 120;
    public static final int PICK_SECONDS = 45;
    public static final int ZOMBIE_START_SUN = 200;
    public static final int PLANT_START_SUN = 250;
    public static final int ZOMBIE_SUN_EVERY_TICKS = 20;
    public static final int ZOMBIE_SUN_AMOUNT = 25;
    public static final int PLACEMENT_COLUMN = 5;
    public static final int FIRST_ZOMBIE_COLUMN = 6;
    public static final int PLANT_SLOTS = 8;
    public static final int ZOMBIE_SLOTS = 5;
    public static final int ROWS = 5;
    public static final int COLS = 9;

    public static final String PHASE_PICKING = "picking";
    public static final String PHASE_PLAYING = "playing";

    public static final List<String> DEFAULT_PLANTS = List.of(
            "Sunflower",
            "Peashooter",
            "Wall-nut",
            "Snow Pea",
            "Repeater",
            "Cabbage-pult",
            "Potato Mine",
            "Chomper");

    public static final List<String> PLANT_POOL = List.of(
            "Sunflower",
            "Twin Sunflower",
            "Peashooter",
            "Repeater",
            "Snow Pea",
            "Threepeater",
            "Wall-nut",
            "Tall-nut",
            "Cabbage-pult",
            "Melon-pult",
            "Potato Mine",
            "Chomper",
            "Bonk Choy",
            "Cherry Bomb",
            "Torchwood",
            "Split Pea");

    public static final List<String> DEFAULT_ZOMBIES = List.of(
            "ZombieDefault",
            "ZombieImp",
            "ZombieArmor1",
            "ZombieProspector",
            "ZombieArmor2");

    public static final List<String> ZOMBIE_POOL = List.of(
            "ZombieDefault",
            "ZombieImp",
            "ZombieArmor1",
            "ZombieProspector",
            "ZombieArmor2",
            "ZombieNewspaper",
            "ZombieArmor4",
            "ZombieIceAgeDodo",
            "ZombieDarkArmor3");

    private IZombieDuelCatalog() {
    }

    public static final int DEFAULT_ZOMBIE_COST = 150;

    public static Map<String, Integer> zombieCosts() {
        Map<String, Integer> costs = new LinkedHashMap<>();
        costs.put("ZombieDefault", 100);
        costs.put("ZombieImp", 100);
        costs.put("ZombieArmor1", 200);
        costs.put("ZombieProspector", 200);
        costs.put("ZombieArmor2", 400);
        costs.put("ZombieNewspaper", 700);
        costs.put("ZombieArmor4", 700);
        costs.put("ZombieIceAgeDodo", 600);
        costs.put("ZombieDarkArmor3", 550);
        return costs;
    }

    public static int costOf(String alias) {
        if (alias == null || alias.isBlank()) {
            return DEFAULT_ZOMBIE_COST;
        }
        Integer cost = zombieCosts().get(alias);
        return cost == null ? DEFAULT_ZOMBIE_COST : cost;
    }

    public static Map<String, Integer> costsFor(List<String> roster) {
        Map<String, Integer> filtered = new LinkedHashMap<>();
        if (roster == null) {
            return filtered;
        }
        for (String alias : roster) {
            if (alias != null && !alias.isBlank()) {
                filtered.put(alias, costOf(alias));
            }
        }
        return filtered;
    }

    public static int rechargeSeconds(String alias) {
        return Math.max(3, costOf(alias) / 25);
    }
}
