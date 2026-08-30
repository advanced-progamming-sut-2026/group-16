package io.github.finalwave.model.game.entity.projectile;


public final class MelonMuzzles {
    public static final double X = 0.4;
    public static final double Y = 0.85;
    public static final double PLANTFOOD_X = 0;
    public static final double PLANTFOOD_Y = 0.76;
    public static final int ATTACK_WINDUP_TICKS = 7;
    public static final int[] PLANTFOOD_SHOT_TICKS = {10, 15, 19, 22};

    private MelonMuzzles() {
    }

    public static double x() {
        return X;
    }

    public static double y() {
        return Y;
    }

    public static double plantFoodX() {
        return PLANTFOOD_X;
    }

    public static double plantFoodY() {
        return PLANTFOOD_Y;
    }
}
