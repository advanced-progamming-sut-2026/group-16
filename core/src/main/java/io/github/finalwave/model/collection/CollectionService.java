package io.github.finalwave.model.collection;

import io.github.finalwave.model.definition.PlantLevelStats;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.UpgradeCost;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.definition.zombie.ZombieDefinition;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class CollectionService {

    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;

    public CollectionService(PlantRegistry plantRegistry, ZombieRegistry zombieRegistry) {
        this.plantRegistry = plantRegistry;
        this.zombieRegistry = zombieRegistry;
    }

    public static CollectionService createDefault(PlantRegistry plantRegistry) {
        return new CollectionService(plantRegistry, loadZombieRegistry());
    }

    public PlantCollection createCollection(User user) {
        return new PlantCollection(plantRegistry, user.getPlantProgress(), user.getCoins());
    }

    public void syncCoinsToUser(User user, PlantCollection collection) {
        user.setCoins(collection.getCoins());
    }

    public List<CollectionPlantEntry> listPlants(User user, CollectionPlantQuery query) {
        CollectionPlantQuery resolved = query == null ? CollectionPlantQuery.all() : query;
        PlantCollection collection = createCollection(user);
        List<CollectionPlantEntry> entries = new ArrayList<>();
        for (PlantDefinition definition : plantRegistry.getAllDefinitions()) {
            CollectionPlantEntry entry = plantEntry(user, collection, definition);
            if (matches(entry, resolved)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public CollectionPlantDetail plantDetail(User user, String plantName) {
        PlantDefinition definition = plantRegistry.getDefinition(plantName);
        if (definition == null) {
            return null;
        }
        PlantCollection collection = createCollection(user);
        CollectionPlantEntry entry = plantEntry(user, collection, definition);
        PlantLevelStats stats = collection.showPlantDetails(plantName);
        PlantLevelStats next = null;
        if (entry.owned() && !entry.maxLevel()) {
            next = PlantLevelStats.atLevel(definition, entry.level() + 1);
        }
        return new CollectionPlantDetail(
                entry.name(),
                entry.category(),
                entry.tags(),
                entry.level(),
                entry.owned(),
                entry.maxLevel(),
                entry.seedPackets(),
                entry.seedPacketsNeeded(),
                entry.upgradeCoins(),
                entry.canUpgrade(),
                entry.canPurchase(),
                stats.getCost(),
                stats.getMaxHealth(),
                stats.getDamage(),
                stats.getRecharge(),
                stats.getActionInterval(),
                definition.getPlantFoodType(),
                definition.getAbilityType(),
                definition.getAbilityValue(),
                definition.getPlantFoodValue(),
                stats.getNextUpgradeSummary(),
                next == null ? null : next.getCost(),
                next == null ? null : next.getMaxHealth(),
                next == null ? null : next.getDamage(),
                next == null ? null : next.getRecharge());
    }

    public List<String> plantFamilies() {
        LinkedHashSet<String> families = new LinkedHashSet<>();
        for (PlantDefinition definition : plantRegistry.getAllDefinitions()) {
            if (definition.getCategory() != null && !definition.getCategory().isBlank()) {
                families.add(definition.getCategory());
            }
        }
        List<String> sorted = new ArrayList<>(families);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    public CollectionCounts plantCounts(User user) {
        int owned = 0;
        int total = 0;
        for (PlantDefinition definition : plantRegistry.getAllDefinitions()) {
            total++;
            if (user.getPlantProgress().isOwned(definition.getName())) {
                owned++;
            }
        }
        return new CollectionCounts(owned, total);
    }

    public List<CollectionZombieEntry> listZombies(User user) {
        List<CollectionZombieEntry> entries = new ArrayList<>();
        for (ZombieDefinition definition : zombieRegistry.getAllDefinitions()) {
            String alias = definition.getAlias();
            entries.add(new CollectionZombieEntry(
                    alias,
                    hasSeenZombie(user, alias),
                    definition.getHitpoints(),
                    definition.getSpeed(),
                    definition.getToughnessLabel(),
                    definition.getSpeedLabel()));
        }
        return entries;
    }

    public CollectionZombieDetail zombieDetail(User user, String zombieName) {
        if (!hasSeenZombie(user, zombieName)) {
            return null;
        }
        ZombieDefinition definition = zombieRegistry.getDefinition(zombieName);
        if (definition == null) {
            return null;
        }
        return new CollectionZombieDetail(
                definition.getAlias(),
                definition.getObjClass(),
                definition.getHitpoints(),
                definition.getSpeed(),
                definition.getToughnessLabel(),
                definition.getSpeedLabel(),
                definition.getEatDps(),
                definition.hasArmor(),
                definition.getArmorAliases());
    }

    public List<String> formatOwnedPlants(User user) {
        PlantCollection collection = createCollection(user);
        List<String> lines = new ArrayList<>();
        for (OwnedPlant owned : collection.getOwnedPlants()) {
            lines.add(formatOwnedPlantLine(owned));
        }
        if (lines.isEmpty()) {
            lines.add("No plants owned.");
        }
        return lines;
    }

    public List<String> formatAllPlants(User user) {
        List<String> lines = new ArrayList<>();
        for (PlantDefinition definition : plantRegistry.getAllDefinitions()) {
            String name = definition.getName();
            if (user.getPlantProgress().isOwned(name)) {
                OwnedPlant owned = user.getPlantProgress().getOwnedPlant(name).orElseThrow();
                lines.add(name + " | OWNED | level " + owned.getLevel()
                        + " | seeds " + owned.getSeedPackets());
            } else {
                lines.add(name + " | LOCKED");
            }
        }
        return lines;
    }

    public String formatPlantDetails(User user, String plantName) {
        PlantCollection collection = createCollection(user);
        PlantDefinition definition = plantRegistry.getDefinition(plantName);
        if (definition == null) {
            return null;
        }
        PlantLevelStats stats = collection.showPlantDetails(plantName);
        int seedPackets = user.getPlantProgress().getOwnedPlant(plantName)
                .map(OwnedPlant::getSeedPackets)
                .orElse(0);
        boolean owned = user.getPlantProgress().isOwned(plantName);
        StringBuilder builder = new StringBuilder();
        builder.append("Plant: ").append(stats.getPlantName()).append('\n');
        builder.append("Category: ").append(stats.getCategory()).append('\n');
        builder.append("Tags: ").append(String.join(", ", stats.getTags())).append('\n');
        builder.append("Owned: ").append(owned).append('\n');
        builder.append("Level: ").append(stats.getLevel()).append('\n');
        builder.append("Seed packets: ").append(seedPackets).append('\n');
        builder.append("Cost: ").append(stats.getCost()).append('\n');
        builder.append("Max health: ").append(stats.getMaxHealth()).append('\n');
        builder.append("Damage: ").append(stats.getDamage()).append('\n');
        builder.append("Action interval: ").append(stats.getActionInterval()).append('\n');
        builder.append("Recharge: ").append(stats.getRecharge()).append('\n');
        if (!stats.getNextUpgradeSummary().isBlank()) {
            builder.append("Next upgrade: ").append(stats.getNextUpgradeSummary());
        }
        return builder.toString().trim();
    }

    public List<String> formatSeenZombies(User user) {
        List<String> lines = new ArrayList<>();
        if (user.isDebugMode()) {
            for (ZombieDefinition definition : zombieRegistry.getAllDefinitions()) {
                lines.add(definition.getAlias());
            }
        } else {
            lines.addAll(user.getUnlockedZombies());
        }
        if (lines.isEmpty()) {
            lines.add("No zombies seen yet.");
        }
        return lines;
    }

    public List<String> formatAllZombies(User user) {
        List<String> lines = new ArrayList<>();
        for (ZombieDefinition definition : zombieRegistry.getAllDefinitions()) {
            String alias = definition.getAlias();
            if (hasSeenZombie(user, alias)) {
                lines.add(alias + " | SEEN");
            } else {
                lines.add("[ empty ]");
            }
        }
        return lines;
    }

    public String formatZombieDetails(User user, String zombieName) {
        if (!hasSeenZombie(user, zombieName)) {
            return null;
        }
        ZombieDefinition definition = zombieRegistry.getDefinition(zombieName);
        if (definition == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Zombie: ").append(definition.getAlias()).append('\n');
        builder.append("Class: ").append(definition.getObjClass()).append('\n');
        builder.append("Hitpoints: ").append(definition.getHitpoints()).append('\n');
        builder.append("Eat DPS: ").append(definition.getEatDps()).append('\n');
        builder.append("Speed: ").append(definition.getSpeed()).append('\n');
        builder.append("Wave point cost: ").append(definition.getWavePointCost()).append('\n');
        if (definition.getToughnessLabel() != null) {
            builder.append("Toughness: ").append(definition.getToughnessLabel()).append('\n');
        }
        if (definition.getSpeedLabel() != null) {
            builder.append("Speed label: ").append(definition.getSpeedLabel()).append('\n');
        }
        if (definition.hasArmor()) {
            builder.append("Armor: ").append(String.join(", ", definition.getArmorAliases()));
        }
        return builder.toString().trim();
    }

    public PlantCollection.UpgradeResult upgradePlant(User user, String plantName) {
        PlantCollection collection = createCollection(user);
        PlantCollection.UpgradeResult result = collection.upgradeWithResult(plantName);
        if (result.success()) {
            syncCoinsToUser(user, collection);
        }
        return result;
    }

    public PlantCollection.PurchaseResult purchasePlant(User user, UnlockService unlockService, String plantName) {
        PlantCollection collection = createCollection(user);
        PlantCollection.PurchaseFailure failure = collection.getPurchaseFailure(plantName);
        if (failure != null) {
            return new PlantCollection.PurchaseResult(false, false, failure);
        }
        user.spendCoins(PlantCollection.PURCHASE_COST_COINS);
        boolean newlyUnlocked = unlockService.unlockPlant(user, plantName);
        return new PlantCollection.PurchaseResult(true, newlyUnlocked, null);
    }

    public boolean isKnownPlant(String plantName) {
        return plantRegistry.getDefinition(plantName) != null;
    }

    public boolean isKnownZombie(String zombieName) {
        return zombieRegistry.getDefinition(zombieName) != null;
    }

    public boolean hasSeenZombie(User user, String zombieName) {
        if (zombieName == null || !isKnownZombie(zombieName)) {
            return false;
        }
        return user.isDebugMode() || user.getUnlockedZombies().contains(zombieName);
    }

    public List<String> selectablePlantNames(User user) {
        return selectablePlantNames(user, plantRegistry);
    }

    public boolean canSelectPlant(User user, String plantName) {
        return canSelectPlant(user, plantName, plantRegistry);
    }

    public static List<String> selectablePlantNames(User user, PlantRegistry registry) {
        if (user != null && user.isDebugMode() && registry != null) {
            List<String> names = new ArrayList<>();
            for (PlantDefinition definition : registry.getAllDefinitions()) {
                names.add(definition.getName());
            }
            return List.copyOf(names);
        }
        if (user == null) {
            return List.of();
        }
        return user.getPlantProgress().getUnlockedPlantNames();
    }

    public static boolean canSelectPlant(User user, String plantName, PlantRegistry registry) {
        if (plantName == null || plantName.isBlank()) {
            return false;
        }
        if (user != null && user.isDebugMode()) {
            return registry != null && registry.getDefinition(plantName) != null;
        }
        return user != null && user.getPlantProgress().isOwned(plantName);
    }

    private CollectionPlantEntry plantEntry(User user, PlantCollection collection, PlantDefinition definition) {
        String name = definition.getName();
        boolean owned = user.getPlantProgress().isOwned(name);
        OwnedPlant ownedPlant = user.getPlantProgress().getOwnedPlant(name).orElse(null);
        int level = owned && ownedPlant != null ? ownedPlant.getLevel() : 1;
        int seeds = ownedPlant == null ? 0 : ownedPlant.getSeedPackets();
        int maxLevel = Math.min(definition.getMaxLevel(), user.getPlantProgress().getMaxLevel());
        boolean atMax = owned && level >= maxLevel;
        return new CollectionPlantEntry(
                name,
                definition.getCategory(),
                definition.getTags(),
                level,
                owned,
                atMax,
                seeds,
                packetsNeeded(definition, level, maxLevel, seeds),
                upgradeCoinsNeeded(definition, level, maxLevel),
                collection.canUpgrade(name),
                collection.canPurchase(name));
    }

    private static boolean matches(CollectionPlantEntry entry, CollectionPlantQuery query) {
        CollectionPlantFilter filter = query.filter() == null ? CollectionPlantFilter.ALL : query.filter();
        String family = query.family();
        if (family != null && !family.isBlank()
                && (entry.category() == null || !entry.category().equalsIgnoreCase(family))) {
            return false;
        }
        return switch (filter) {
            case ALL -> true;
            case OWNED -> entry.owned();
            case LOCKED -> !entry.owned();
            case UPGRADEABLE -> entry.canUpgrade();
        };
    }

    private static int packetsNeeded(PlantDefinition definition, int level, int maxLevel, int seeds) {
        if (level >= maxLevel || level + 1 > definition.getMaxLevel()) {
            return Math.max(seeds, 1);
        }
        return UpgradeCost.forLevel(definition, level + 1).getSeedPackets();
    }

    private static int upgradeCoinsNeeded(PlantDefinition definition, int level, int maxLevel) {
        if (level >= maxLevel || level + 1 > definition.getMaxLevel()) {
            return 0;
        }
        return UpgradeCost.forLevel(definition, level + 1).getCoins();
    }

    private static String formatOwnedPlantLine(OwnedPlant owned) {
        return owned.getPlantName() + " | level " + owned.getLevel()
                + " | seeds " + owned.getSeedPackets();
    }

    private static ZombieRegistry loadZombieRegistry() {
        ZombieRegistry registry = new ZombieRegistry();
        try (InputStream zombies = CollectionService.class.getClassLoader()
                .getResourceAsStream("zombies.json");
             InputStream armor = CollectionService.class.getClassLoader()
                     .getResourceAsStream("ArmorTypeData.json")) {
            if (zombies == null || armor == null) {
                throw new IllegalStateException("zombie resources missing");
            }
            registry.loadFromJson(zombies);
            registry.loadArmorFromJson(armor);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load zombie registry", e);
        }
        return registry;
    }
}
