package model.collection;

import model.definition.PlantLevelStats;
import model.definition.PlantRegistry;
import model.definition.UpgradeCost;
import model.definition.plant.PlantDefinition;
import model.quest.reward.QuestReward;

import java.util.ArrayList;
import java.util.List;

public final class PlantCollection {

    public static final int PURCHASE_COST_COINS = 2000;

    private final PlantRegistry registry;
    private final PlayerPlantProgress progress;
    private int coins;

    public PlantCollection(PlantRegistry registry, PlayerPlantProgress progress, int startingCoins) {
        this.registry = registry;
        this.progress = progress;
        this.coins = startingCoins;
    }

    public List<OwnedPlant> getOwnedPlants() {
        List<OwnedPlant> owned = new ArrayList<>();
        for (OwnedPlant plant : progress.getOwnedPlants().values()) {
            if (plant.isUnlocked()) {
                owned.add(plant);
            }
        }
        return owned;
    }

    public List<PlantDefinition> getAllDefinitions() {
        return registry.getAllDefinitions();
    }

    public PlantLevelStats showPlantDetails(String plantName) {
        PlantDefinition definition = registry.getDefinition(plantName);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown plant: " + plantName);
        }
        int level = progress.getOwnedPlant(plantName).map(OwnedPlant::getLevel).orElse(1);
        return PlantLevelStats.atLevel(definition, level);
    }

    public boolean canUpgrade(String plantName) {
        PlantDefinition definition = registry.getDefinition(plantName);
        OwnedPlant owned = progress.getOwnedPlant(plantName).orElse(null);
        if (definition == null || owned == null || !owned.isUnlocked()) {
            return false;
        }
        int nextLevel = owned.getLevel() + 1;
        if (nextLevel > definition.getMaxLevel() || nextLevel > progress.getMaxLevel()) {
            return false;
        }
        UpgradeCost cost = UpgradeCost.forLevel(definition, nextLevel);
        return coins >= cost.getCoins() && owned.getSeedPackets() >= cost.getSeedPackets();
    }

    public boolean upgrade(String plantName) {
        if (!canUpgrade(plantName)) {
            return false;
        }
        PlantDefinition definition = registry.getDefinition(plantName);
        OwnedPlant owned = progress.getMutablePlant(plantName);
        int nextLevel = owned.getLevel() + 1;
        UpgradeCost cost = UpgradeCost.forLevel(definition, nextLevel);
        coins -= cost.getCoins();
        owned.consumeSeedPackets(cost.getSeedPackets());
        owned.setLevel(nextLevel);
        return true;
    }

    public boolean canPurchase(String plantName) {
        PlantDefinition definition = registry.getDefinition(plantName);
        if (definition == null) {
            return false;
        }
        if (progress.isOwned(plantName)) {
            return false;
        }
        return coins >= PURCHASE_COST_COINS;
    }

    public boolean purchase(String plantName) {
        if (!canPurchase(plantName)) {
            return false;
        }
        coins -= PURCHASE_COST_COINS;
        progress.unlock(plantName);
        return true;
    }

    public PurchaseFailure getPurchaseFailure(String plantName) {
        PlantDefinition definition = registry.getDefinition(plantName);
        if (definition == null) {
            return PurchaseFailure.UNKNOWN_PLANT;
        }
        if (progress.isOwned(plantName)) {
            return PurchaseFailure.ALREADY_OWNED;
        }
        if (coins < PURCHASE_COST_COINS) {
            return PurchaseFailure.INSUFFICIENT_COINS;
        }
        return null;
    }

    public PurchaseResult purchaseWithResult(String plantName) {
        PurchaseFailure failure = getPurchaseFailure(plantName);
        if (failure != null) {
            return new PurchaseResult(false, false, failure);
        }
        coins -= PURCHASE_COST_COINS;
        boolean newlyUnlocked = progress.unlock(plantName);
        return new PurchaseResult(true, newlyUnlocked, null);
    }

    public UpgradeFailure getUpgradeFailure(String plantName) {
        PlantDefinition definition = registry.getDefinition(plantName);
        if (definition == null) {
            return UpgradeFailure.UNKNOWN_PLANT;
        }
        OwnedPlant owned = progress.getOwnedPlant(plantName).orElse(null);
        if (owned == null || !owned.isUnlocked()) {
            return UpgradeFailure.NOT_OWNED;
        }
        int nextLevel = owned.getLevel() + 1;
        if (nextLevel > definition.getMaxLevel() || nextLevel > progress.getMaxLevel()) {
            return UpgradeFailure.MAX_LEVEL;
        }
        UpgradeCost cost = UpgradeCost.forLevel(definition, nextLevel);
        if (coins < cost.getCoins()) {
            return UpgradeFailure.INSUFFICIENT_COINS;
        }
        if (owned.getSeedPackets() < cost.getSeedPackets()) {
            return UpgradeFailure.INSUFFICIENT_SEED_PACKETS;
        }
        return null;
    }

    public UpgradeResult upgradeWithResult(String plantName) {
        UpgradeFailure failure = getUpgradeFailure(plantName);
        if (failure != null) {
            return new UpgradeResult(false, ownedLevel(plantName), failure);
        }
        PlantDefinition definition = registry.getDefinition(plantName);
        OwnedPlant owned = progress.getMutablePlant(plantName);
        int nextLevel = owned.getLevel() + 1;
        UpgradeCost cost = UpgradeCost.forLevel(definition, nextLevel);
        coins -= cost.getCoins();
        owned.consumeSeedPackets(cost.getSeedPackets());
        owned.setLevel(nextLevel);
        return new UpgradeResult(true, nextLevel, null);
    }

    private int ownedLevel(String plantName) {
        return progress.getOwnedPlant(plantName).map(OwnedPlant::getLevel).orElse(0);
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        coins += amount;
    }

    public void addSeedPackets(String plantName, int count) {
        PlantDefinition definition = resolveDefinition(plantName);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown plant: " + plantName);
        }
        progress.getOrCreate(definition.getName()).addSeedPackets(count);
    }

    public void applyQuestReward(QuestReward reward) {
        applyQuestRewardWithResult(reward);
    }

    public QuestRewardResult applyQuestRewardWithResult(QuestReward reward) {
        if (reward == null) {
            return new QuestRewardResult(null);
        }
        addCoins(reward.getCoins());
        String seedPlant = reward.getSeedPacketPlantId();
        if (seedPlant != null && reward.getSeedPacketCount() > 0
                && !"ANY".equalsIgnoreCase(seedPlant)) {
            addSeedPackets(seedPlant, reward.getSeedPacketCount());
        }
        String newlyUnlockedPlant = null;
        if (reward.getUnlockTargetId() != null
                && !"RANDOM_PLANT".equals(reward.getUnlockTargetId())) {
            PlantDefinition definition = resolveDefinition(reward.getUnlockTargetId());
            if (definition != null && progress.unlock(definition.getName())) {
                newlyUnlockedPlant = definition.getName();
            }
        }
        return new QuestRewardResult(newlyUnlockedPlant);
    }

    public PlayerPlantProgress getProgress() {
        return progress;
    }

    private PlantDefinition resolveDefinition(String nameOrId) {
        PlantDefinition byName = registry.getDefinition(nameOrId);
        if (byName != null) {
            return byName;
        }
        try {
            return registry.getDefinitionById(Integer.parseInt(nameOrId));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public enum PurchaseFailure {
        UNKNOWN_PLANT,
        ALREADY_OWNED,
        INSUFFICIENT_COINS
    }

    public enum UpgradeFailure {
        UNKNOWN_PLANT,
        NOT_OWNED,
        MAX_LEVEL,
        INSUFFICIENT_COINS,
        INSUFFICIENT_SEED_PACKETS
    }

    public record PurchaseResult(boolean success, boolean newlyUnlocked, PurchaseFailure failure) {
    }

    public record UpgradeResult(boolean success, int newLevel, UpgradeFailure failure) {
    }

    public record QuestRewardResult(String newlyUnlockedPlant) {
    }
}
