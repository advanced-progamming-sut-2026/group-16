package io.github.finalwave.model.game.entity;

public abstract class Entity {

    private final String id;
    private int maxHealth;
    private int health;
    private double x;
    private double y;
    private double previousX;
    private double previousY;
    private boolean dead;

    protected Entity(String id, int maxHealth, double x, double y) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        this.id = id;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.dead = false;
    }

    public final void snapshotPose() {
        previousX = x;
        previousY = y;
    }

    public void takeDamage(int amount) {
        if (dead || amount <= 0) {
            return;
        }
        health -= amount;
        if (health <= 0) {
            health = 0;
            dead = true;
            onDeath();
        }
    }

    protected void onDeath() {
        // default: no-op
    }

    public final boolean isDead() {
        return dead;
    }

    public final boolean isAlive() {
        return !dead;
    }

    public final String getId() {
        return id;
    }

    public final int getHealth() {
        return health;
    }

    public final int getMaxHealth() {
        return maxHealth;
    }

    protected final void setMaxHealth(int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        this.maxHealth = maxHealth;
    }

    protected final void heal(int amount) {
        if (dead || amount <= 0) {
            return;
        }
        health = Math.min(maxHealth, health + amount);
    }

    public final double getX() {
        return x;
    }

    protected final void setX(double x) {
        this.x = x;
    }

    public final double getY() {
        return y;
    }

    public final double getPreviousX() {
        return previousX;
    }

    public final double getPreviousY() {
        return previousY;
    }

    protected final void setY(double y) {
        this.y = y;
    }

    public final double getHealthRatio() {
        return (double) health / maxHealth;
    }

    public abstract void onTickUpdate(GameContext context);
}