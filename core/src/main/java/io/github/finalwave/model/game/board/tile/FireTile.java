package io.github.finalwave.model.game.board.tile;

public class FireTile extends Tile {

    public static final int DURATION_TICKS = 40;

    private int remainingTicks;

    public FireTile() {
        this(DURATION_TICKS);
    }

    public FireTile(int durationTicks) {
        this.remainingTicks = Math.max(1, durationTicks);
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public boolean tickExpired() {
        remainingTicks--;
        return remainingTicks <= 0;
    }

    @Override
    public boolean blocksPlanting() {
        return true;
    }

    @Override
    public boolean isFire() {
        return true;
    }
}
