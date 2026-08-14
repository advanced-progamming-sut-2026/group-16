package io.github.finalwave.model.collection;

import java.util.List;

public record CollectionPlantEntry(
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
        boolean canPurchase
) {
}
