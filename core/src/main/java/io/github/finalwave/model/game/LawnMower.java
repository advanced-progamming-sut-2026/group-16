package io.github.finalwave.model.game;

public final class LawnMower {

    private final int row;
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

    public boolean isUsed() {
        return used;
    }

    public boolean trigger() {
        if (used) {
            return false;
        }
        used = true;
        return true;
    }
}
