package io.github.finalwave.model.collection;

public record CollectionZombieEntry(
        String alias,
        boolean seen,
        int hitpoints,
        double speed,
        String toughnessLabel,
        String speedLabel
) {
    public String displayName() {
        return CollectionNames.zombie(alias);
    }
}
