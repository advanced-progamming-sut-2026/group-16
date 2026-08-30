package io.github.finalwave.network.match;

public final class SnapshotZombie {
    private String id;
    private String type;
    private double x;
    private double y;
    private int row;
    private int health;
    private int maxHealth;
    private boolean stationary;

    public SnapshotZombie() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public boolean isStationary() {
        return stationary;
    }

    public void setStationary(boolean stationary) {
        this.stationary = stationary;
    }
}
