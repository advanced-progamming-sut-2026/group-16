package io.github.finalwave.model.game.entity.projectile;


public final class KernelMuzzles {
    public static final double X = 0.05;
    public static final double Y = 1.00;
    public static final double PLANTFOOD_X = 0.00;
    public static final double PLANTFOOD_Y = 0.44;
    public static final int ATTACK_WINDUP_TICKS = 6;
    public static final int PLANTFOOD_WINDUP_TICKS = 6;

    private KernelMuzzles() {
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
