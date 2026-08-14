package io.github.finalwave.model.collection;

import java.util.List;

public record CollectionZombieDetail(
        String alias,
        String objClass,
        int hitpoints,
        double speed,
        String toughnessLabel,
        String speedLabel,
        int eatDps,
        boolean hasArmor,
        List<String> armorAliases
) {
    public String displayName() {
        return CollectionNames.zombie(alias);
    }

    public String toughnessDisplay() {
        return CollectionNames.statLabel(toughnessLabel, "toughness", String.valueOf(hitpoints));
    }

    public String speedDisplay() {
        return CollectionNames.statLabel(speedLabel, "speed", CollectionNames.formatSpeed(speed));
    }
}
