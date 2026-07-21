package model.game;

import model.adventure.ChapterId;
import model.adventure.LevelConfig;
import model.definition.PlantRegistry;
import model.definition.plant.PlantDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class LockedPlantsRulesFactory {

    private static final Map<String, List<String>> SPECIFIC_LOCKS = Map.of(
            chapterLevelKey(ChapterId.ANCIENT_EGYPT, 3), List.of("Wall-nut", "Cherry Bomb"));

    private LockedPlantsRulesFactory() {
    }

    public static LockedPlantsRules create(LevelConfig level,
                                           ChapterId chapterId,
                                           PlantRegistry plantRegistry,
                                           Iterable<String> ownedPlantNames,
                                           Random random) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }
        String handlerKey = level.getSpecialHandlerKey();
        if (handlerKey == null) {
            return emptyRules(LockedPlantsMode.FAMILY);
        }
        return switch (handlerKey) {
            case "locked" -> createFamilyMode(plantRegistry, ownedPlantNames, random);
            case "locked-specific" -> createSpecificMode(chapterId, level.getIndex(), ownedPlantNames);
            default -> throw new IllegalArgumentException("Unknown locked plants handler key: " + handlerKey);
        };
    }

    private static LockedPlantsRules createFamilyMode(PlantRegistry plantRegistry,
                                                      Iterable<String> ownedPlantNames,
                                                      Random random) {
        Random picker = random == null ? new Random() : random;
        Map<String, List<String>> byCategory = new HashMap<>();
        for (String plantName : ownedPlantNames) {
            PlantDefinition definition = plantRegistry.getDefinition(plantName);
            if (definition == null) {
                continue;
            }
            byCategory.computeIfAbsent(definition.getCategory(), ignored -> new ArrayList<>())
                    .add(plantName);
        }

        Set<String> locked = new HashSet<>();
        Set<String> allowed = new HashSet<>();
        for (List<String> categoryPlants : byCategory.values()) {
            if (categoryPlants.size() < 2) {
                allowed.addAll(categoryPlants);
                continue;
            }
            List<String> shuffled = new ArrayList<>(categoryPlants);
            shuffle(shuffled, picker);
            allowed.add(shuffled.get(0));
            for (int i = 1; i < shuffled.size(); i++) {
                locked.add(shuffled.get(i));
            }
        }
        return new LockedPlantsRules(LockedPlantsMode.FAMILY, locked, allowed);
    }

    private static LockedPlantsRules createSpecificMode(ChapterId chapterId,
                                                        int levelIndex,
                                                        Iterable<String> ownedPlantNames) {
        List<String> configuredLocks = SPECIFIC_LOCKS.getOrDefault(
                chapterLevelKey(chapterId, levelIndex), List.of());
        Set<String> owned = new HashSet<>();
        for (String plantName : ownedPlantNames) {
            owned.add(plantName);
        }
        Set<String> locked = new HashSet<>();
        for (String plantName : configuredLocks) {
            if (owned.contains(plantName)) {
                locked.add(plantName);
            }
        }
        return new LockedPlantsRules(LockedPlantsMode.SPECIFIC, locked, Set.of());
    }

    private static LockedPlantsRules emptyRules(LockedPlantsMode mode) {
        return new LockedPlantsRules(mode, Set.of(), Set.of());
    }

    private static String chapterLevelKey(ChapterId chapterId, int levelIndex) {
        return chapterId.name() + ":" + levelIndex;
    }

    private static void shuffle(List<String> items, Random random) {
        for (int i = items.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String temp = items.get(i);
            items.set(i, items.get(j));
            items.set(j, temp);
        }
    }
}
