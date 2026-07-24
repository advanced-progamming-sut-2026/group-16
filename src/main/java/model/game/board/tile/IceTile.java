package model.game.board.tile;

public class IceTile extends Tile {

    public static final int MAX_HEALTH = 600;
    public static final int ADJACENT_FIRE_DAMAGE_PER_TICK = 6;

    private int health = MAX_HEALTH;

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
    public boolean isIce() {
        return true;
    }
}
