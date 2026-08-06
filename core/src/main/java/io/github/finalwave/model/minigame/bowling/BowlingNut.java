package io.github.finalwave.model.minigame.bowling;

public final class BowlingNut {

    private final BowlingNutType type;
    private double x;
    private double row;
    private double angleRadians;
    private int zombieHitCount;

    public BowlingNut(BowlingNutType type, double x, double row) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        this.type = type;
        this.x = x;
        this.row = row;
        this.angleRadians = 0.0;
    }

    public BowlingNutType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getRow() {
        return row;
    }

    public void setRow(double row) {
        this.row = row;
    }

    public double getAngleRadians() {
        return angleRadians;
    }

    public void setAngleRadians(double angleRadians) {
        this.angleRadians = angleRadians;
    }

    public int getZombieHitCount() {
        return zombieHitCount;
    }

    public void incrementZombieHitCount() {
        zombieHitCount++;
    }
}
