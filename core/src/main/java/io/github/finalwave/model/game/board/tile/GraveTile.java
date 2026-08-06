package io.github.finalwave.model.game.board.tile;

public class GraveTile extends Tile {

    public static final int MAX_HEALTH = 700;

    public enum Loot {
        NONE,
        SUN_50,
        PLANT_FOOD
    }

    private final Loot loot;
    private int health;

    public GraveTile() {
        this(Loot.NONE);
    }

    public GraveTile(Loot loot) {
        this.loot = loot == null ? Loot.NONE : loot;
        this.health = MAX_HEALTH;
    }

    public Loot getLoot() {
        return loot;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    public void takeDamage(int amount) {
        if (amount <= 0 || health <= 0) {
            return;
        }
        health = Math.max(0, health - amount);
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    @Override
    public boolean blocksPlanting() {
        return true;
    }

    @Override
    public boolean isGrave() {
        return true;
    }

    @Override
    public boolean blocksProjectiles() {
        return true;
    }
}
