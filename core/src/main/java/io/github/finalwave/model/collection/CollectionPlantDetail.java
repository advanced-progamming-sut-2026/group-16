package io.github.finalwave.model.collection;

import java.util.List;

public record CollectionPlantDetail(
        String name,
        String category,
        List<String> tags,
        int level,
        boolean owned,
        boolean maxLevel,
        int seedPackets,
        int seedPacketsNeeded,
        int upgradeCoins,
        boolean canUpgrade,
        boolean canPurchase,
        int cost,
        int maxHealth,
        int damage,
        double recharge,
        double actionInterval,
        String plantFoodType,
        String abilityType,
        double abilityValue,
        double plantFoodValue,
        String nextUpgradeSummary,
        Integer nextCost,
        Integer nextMaxHealth,
        Integer nextDamage,
        Double nextRecharge
) {
    public boolean hasNextLevel() {
        return nextCost != null;
    }
}
