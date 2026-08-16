package io.github.finalwave.view.gui.widget;

public final class GreenhouseGrid {
    public static final float SLOT_WIDTH = 220f;
    public static final float SLOT_HEIGHT = 280f;

    public static final float BACKGROUND_WIDTH = 1750f;
    public static final float BACKGROUND_HEIGHT = 774f;

    public static final float COL_1_BG_X = 617f;
    public static final float COL_2_BG_X = 789f;
    public static final float COL_3_BG_X = 956f;
    public static final float COL_4_BG_X = 1123f;

    public static final float ROW_1_BG_Y_FROM_TOP = 301f;
    public static final float ROW_2_BG_Y_FROM_TOP = 462f;
    public static final float ROW_3_BG_Y_FROM_TOP = 626f;

    public static final float COL_1_SHIFT_X = 0f;
    public static final float COL_2_SHIFT_X = 0f;
    public static final float COL_3_SHIFT_X = 0f;
    public static final float COL_4_SHIFT_X = 0f;

    public static final float ROW_1_SHIFT_Y = 0f;
    public static final float ROW_2_SHIFT_Y = 0f;
    public static final float ROW_3_SHIFT_Y = 0f;

    public static final float PLANT_ANCHOR_X = 110f;
    public static final float PLANT_ANCHOR_Y = 110f;
    public static final float HIT_WIDTH = 150f;
    public static final float HIT_HEIGHT = 150f;
    public static final float OVERLAY_Y = 58f;

    public static final float PAM_Y = 40f;
    public static final float PAM_HEIGHT = 220f;
    public static final float PAM_ANCHOR_Y = 0.32f;
    public static final float EMPTY_SCALE = 0.55f;
    public static final float EMPTY_OFFSET_X = 24f;
    public static final float EMPTY_OFFSET_Y = 0f;
    public static final float POT_WIDTH = 100f;
    public static final float POT_HEIGHT = 95f;
    public static final float PLANTED_OFFSET_X = -22f;
    public static final float PLANTED_OFFSET_Y = 26f;
    public static final float PLANT_FEET_ANCHOR_Y = 0.18f;
    public static final float SOIL_FROM_POT = 0.62f;
    public static final float GROWING_SCALE = 0.32f;
    public static final float READY_SCALE = 0.55f;

    private GreenhouseGrid() {
    }

    public static float slotX(int column, float worldWidth, float worldHeight) {
        float scale = fillScale(worldWidth, worldHeight);
        float cropLeft = (BACKGROUND_WIDTH * scale - worldWidth) * 0.5f;
        float matX = columnBgX(column) * scale - cropLeft;
        return matX - PLANT_ANCHOR_X + columnShiftX(column);
    }

    public static float slotY(int row, float worldWidth, float worldHeight) {
        float scale = fillScale(worldWidth, worldHeight);
        float displayedHeight = BACKGROUND_HEIGHT * scale;
        float cropBottom = (displayedHeight - worldHeight) * 0.5f;
        float matY = (BACKGROUND_HEIGHT - rowBgYFromTop(row)) * scale - cropBottom;
        return matY - PLANT_ANCHOR_Y + rowShiftY(row);
    }

    public static float plantOffsetX(int column) {
        return 0f;
    }

    public static float plantOffsetY(int row) {
        return 0f;
    }

    public static float pamOriginX() {
        return PLANT_ANCHOR_X + EMPTY_OFFSET_X;
    }

    public static float pamOriginY() {
        return PAM_Y + PAM_HEIGHT * PAM_ANCHOR_Y + EMPTY_OFFSET_Y;
    }

    public static float potImageX() {
        return pamOriginX() - POT_WIDTH * 0.5f + PLANTED_OFFSET_X;
    }

    public static float potImageY() {
        return pamOriginY() - POT_HEIGHT * 0.5f + PLANTED_OFFSET_Y;
    }

    public static float soilX() {
        return pamOriginX() + PLANTED_OFFSET_X;
    }

    public static float soilY() {
        return potImageY() + POT_HEIGHT * SOIL_FROM_POT;
    }

    public static float columnBgX(int column) {
        return switch (column) {
            case 1 -> COL_1_BG_X;
            case 2 -> COL_2_BG_X;
            case 3 -> COL_3_BG_X;
            default -> COL_4_BG_X;
        };
    }

    public static float rowBgYFromTop(int row) {
        return switch (row) {
            case 1 -> ROW_1_BG_Y_FROM_TOP;
            case 2 -> ROW_2_BG_Y_FROM_TOP;
            default -> ROW_3_BG_Y_FROM_TOP;
        };
    }

    public static float columnShiftX(int column) {
        return switch (column) {
            case 1 -> COL_1_SHIFT_X;
            case 2 -> COL_2_SHIFT_X;
            case 3 -> COL_3_SHIFT_X;
            default -> COL_4_SHIFT_X;
        };
    }

    public static float rowShiftY(int row) {
        return switch (row) {
            case 1 -> ROW_1_SHIFT_Y;
            case 2 -> ROW_2_SHIFT_Y;
            default -> ROW_3_SHIFT_Y;
        };
    }

    private static float fillScale(float worldWidth, float worldHeight) {
        return Math.max(worldWidth / BACKGROUND_WIDTH, worldHeight / BACKGROUND_HEIGHT);
    }
}
