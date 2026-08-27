package io.github.finalwave.model.game.entity.plant;

import io.github.finalwave.model.game.entity.Entity;
import io.github.finalwave.model.game.entity.GameContext;

import java.util.concurrent.atomic.AtomicLong;

public final class PlantCovering extends Entity {

    public enum Type {
        HUNTER_ICE,
        OCTOPUS
    }

    private static final AtomicLong NEXT_ID = new AtomicLong();

    private final Type type;
    private final Plant coveredPlant;
    private final double originX;
    private final double originY;
    private final int flightTicksTotal;
    private int flightTicksRemaining;
    private int holdTicksRemaining;

    public PlantCovering(Type type, Plant coveredPlant, int health) {
        this(type, coveredPlant, health, coveredPlant.getCol(), coveredPlant.getRow(), 0, 0);
    }

    public PlantCovering(Type type, Plant coveredPlant, int health,
                         double originX, double originY, int flightTicks) {
        this(type, coveredPlant, health, originX, originY, flightTicks, 0);
    }

    public PlantCovering(Type type, Plant coveredPlant, int health,
                         double originX, double originY, int flightTicks, int holdTicks) {
        super("covering-" + NEXT_ID.incrementAndGet(), health,
                coveredPlant.getCol(), coveredPlant.getRow());
        this.type = type;
        this.coveredPlant = coveredPlant;
        this.originX = originX;
        this.originY = originY;
        this.flightTicksTotal = Math.max(0, flightTicks);
        this.flightTicksRemaining = this.flightTicksTotal;
        this.holdTicksRemaining = Math.max(0, holdTicks);
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

    public boolean isHeld() {
        return holdTicksRemaining > 0;
    }

    public boolean isInFlight() {
        return holdTicksRemaining <= 0 && flightTicksRemaining > 0;
    }

    public double displayX() {
        double landX = coveredPlant.getCol() + 0.5;
        if (holdTicksRemaining > 0) {
            return originX;
        }
        if (flightTicksTotal <= 0 || flightTicksRemaining <= 0) {
            return landX;
        }
        double t = 1.0 - flightTicksRemaining / (double) flightTicksTotal;
        return originX + (landX - originX) * t;
    }

    public double displayY() {
        double landY = coveredPlant.getRow();
        if (holdTicksRemaining > 0) {
            return originY;
        }
        if (flightTicksTotal <= 0 || flightTicksRemaining <= 0) {
            return landY;
        }
        double t = 1.0 - flightTicksRemaining / (double) flightTicksTotal;
        return originY + (landY - originY) * t;
    }

    public boolean blocksStraightProjectiles() {
        return !isHeld() && !isInFlight();
    }

    @Override
    public void onTickUpdate(GameContext context) {
        if (holdTicksRemaining > 0) {
            holdTicksRemaining--;
        } else if (flightTicksRemaining > 0) {
            flightTicksRemaining--;
        }
        if (coveredPlant.isDead()) {
            takeDamage(getHealth());
        }
    }

    @Override
    protected void onDeath() {
        coveredPlant.enable(getId());
    }
}
