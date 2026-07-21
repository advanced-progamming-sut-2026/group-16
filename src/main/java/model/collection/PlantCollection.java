package model.collection;

import model.definition.PlantRegistry;
import model.definition.UpgradeCost;
import model.definition.PlantLevelStats;
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

    public PurchaseResult purchaseWithResult(String plantName) {
        if (!canPurchase(plantName)) {
            return new PurchaseResult(false, false);
        }
        coins -= PURCHASE_COST_COINS;
        boolean newlyUnlocked = progress.unlock(plantName);
        return new PurchaseResult(true, newlyUnlocked);
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

    public record PurchaseResult(boolean success, boolean newlyUnlocked) {
    }

    public record QuestRewardResult(String newlyUnlockedPlant) {
    }
}
