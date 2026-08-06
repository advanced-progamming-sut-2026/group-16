package io.github.finalwave.model.collection;

import io.github.finalwave.model.definition.PlantLevelStats;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.definition.zombie.ZombieDefinition;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        Set<String> seen = user.getUnlockedZombies();
        List<String> lines = new ArrayList<>();
        for (String name : seen) {
            lines.add(name);
        }
        if (lines.isEmpty()) {
            lines.add("No zombies seen yet.");
        }
        return lines;
    }

    public List<String> formatAllZombies(User user) {
        Set<String> seen = user.getUnlockedZombies();
        List<String> lines = new ArrayList<>();
        for (ZombieDefinition definition : zombieRegistry.getAllDefinitions()) {
            String alias = definition.getAlias();
            if (seen.contains(alias)) {
                lines.add(alias + " | SEEN");
            } else {
                lines.add("[ empty ]");
            }
        }
        return lines;
    }

    public String formatZombieDetails(User user, String zombieName) {
        if (!user.getUnlockedZombies().contains(zombieName)) {
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
