package model.game.entity;

public abstract class Obstacle extends Entity {

    private final boolean blockProjectile;

    protected Obstacle(String id, int maxHealth, double x, double y,
                       boolean blockProjectile) {
        super(id, maxHealth, x, y);
        this.blockProjectile = blockProjectile;
    }

    public boolean blocksProjectiles() {
        return blockProjectile;
    }
}