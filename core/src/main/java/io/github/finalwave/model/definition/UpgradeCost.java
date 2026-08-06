package io.github.finalwave.model.definition;

import io.github.finalwave.model.definition.plant.PlantDefinition;

public final class UpgradeCost {

    private static final int BASE_COINS = 500;
    private static final int BASE_SEED_PACKETS = 5;

    private final int coins;
    private final int seedPackets;
    private final int targetLevel;

    public UpgradeCost(int targetLevel, int coins, int seedPackets) {
        this.targetLevel = targetLevel;
        this.coins = coins;
        this.seedPackets = seedPackets;
    }

    public static UpgradeCost forLevel(PlantDefinition definition, int targetLevel) {
        if (targetLevel <= 1 || targetLevel > definition.getMaxLevel()) {
            throw new IllegalArgumentException("Invalid upgrade target level: " + targetLevel);
        }
        int coins = BASE_COINS * targetLevel;
        int packets = BASE_SEED_PACKETS * (targetLevel - 1);
        return new UpgradeCost(targetLevel, coins, packets);
    }

    public int getCoins() {
        return coins;
    }

    public int getSeedPackets() {
        return seedPackets;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
