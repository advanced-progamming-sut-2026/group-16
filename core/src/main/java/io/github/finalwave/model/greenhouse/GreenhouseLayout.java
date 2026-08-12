package io.github.finalwave.model.greenhouse;

public final class GreenhouseLayout {
    public static final int COLUMNS = 4;
    public static final int ROWS = 3;
    public static final int SLOT_COUNT = COLUMNS * ROWS;
    public static final int POT_UNLOCK_COST_COINS = 2000;

    private GreenhouseLayout() {
    }

    public static boolean isValid(int x, int y) {
        return x >= 1 && x <= COLUMNS && y >= 1 && y <= ROWS;
    }

    public static boolean startsLocked(int y) {
        return y > 1;
    }
}
