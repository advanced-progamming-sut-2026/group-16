package io.github.finalwave.model.game.entity.projectile;


public final class PepperMuzzles {
    public static final double DRAW_X = -0.10;
    public static final double DRAW_Y = 0.12;
    public static final double X = 0.5;
    public static final double Y = 0.73;
    public static final int ATTACK_WINDUP_TICKS = 4;
    public static final int[] PLANTFOOD_SHOT_TICKS = {13, 17, 22};
    public static final double[] PLANTFOOD_X = {-0.06, 0.54, 0.22};
    public static final double[] PLANTFOOD_Y = {0.6, 0.625, 0.64};

    private PepperMuzzles() {
    }

    public static double drawX() {
        return DRAW_X;
    }

    public static double drawY() {
        return DRAW_Y;
    }

    public static double x() {
        return X + DRAW_X;
    }

    public static double y() {
        return Y + DRAW_Y;
    }

    public static double plantFoodX(int index) {
        return PLANTFOOD_X[clamp(index)] + DRAW_X;
    }

    public static double plantFoodY(int index) {
        return PLANTFOOD_Y[clamp(index)] + DRAW_Y;
    }

    private static int clamp(int index) {
        return Math.max(0, Math.min(PLANTFOOD_X.length - 1, index));
    }
}
