package io.github.finalwave.network.match;

public final class SnapshotArmorLayer {
    private String alias;
    private int health;

    public SnapshotArmorLayer() {
    }

    public SnapshotArmorLayer(String alias, int health) {
        this.alias = alias;
        this.health = health;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
