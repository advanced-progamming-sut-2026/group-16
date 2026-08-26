package io.github.finalwave.model.game;

public final class LawnMower {
    public static final double START_X = -0.5;
    public static final double SPEED = 0.26;
    public static final double HIT_RADIUS = 0.26;
    public static final double EXIT_COLUMNS = 4;

    private final int row;
    private double x = START_X;
    private boolean active;
    private boolean used;

    public LawnMower(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("row must be non-negative");
        }
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public double getX() {
        return x;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isUsed() {
        return used;
    }

    public boolean isReady() {
        return !used && !active;
    }

    public void markSpent() {
        used = true;
        active = false;
    }

    public boolean trigger() {
        if (used || active) {
            return false;
        }
        active = true;
        return true;
    }

    public void tick(int cols) {
        if (!active) {
            return;
        }
        x += SPEED;
        if (x >= cols) {
            used = true;
        }
        if (x >= cols + EXIT_COLUMNS) {
            active = false;
        }
    }

    public boolean hits(double zombieX) {
        return active && zombieX <= x + HIT_RADIUS;
    }
}
