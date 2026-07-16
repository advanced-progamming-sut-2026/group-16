package model.game.entity.plant;

import model.game.entity.Entity;
import model.game.entity.GameContext;

import java.util.concurrent.atomic.AtomicLong;

public final class PlantCovering extends Entity {

    public enum Type {
        HUNTER_ICE,
        OCTOPUS
    }

    private static final AtomicLong NEXT_ID = new AtomicLong();

    private final Type type;
    private final Plant coveredPlant;

    public PlantCovering(Type type, Plant coveredPlant, int health) {
        super("covering-" + NEXT_ID.incrementAndGet(), health,
                coveredPlant.getCol(), coveredPlant.getRow());
        this.type = type;
        this.coveredPlant = coveredPlant;
        coveredPlant.disable(getId());
    }

    public Type getType() {
        return type;
    }

    public Plant getCoveredPlant() {
        return coveredPlant;
    }

    public int getCol() {
        return (int) getX();
    }

    public int getRow() {
        return (int) getY();
    }

    public boolean blocksStraightProjectiles() {
        return true;
    }

    @Override
    public void onTickUpdate(GameContext context) {
        if (coveredPlant.isDead()) {
            takeDamage(getHealth());
        }
    }

    @Override
    protected void onDeath() {
        coveredPlant.enable(getId());
    }
}
