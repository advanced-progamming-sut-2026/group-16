package io.github.finalwave.model.game.entity.projectile;


public final class FumeMuzzles {
    public static final double X = 0.88;
    public static final double Y = 0.06;
    public static final double PLANTFOOD_X = 0.95;
    public static final double PLANTFOOD_Y = -0.080;
    public static final double RANGE_TILES = 4.0;
    public static final double PLANTFOOD_RANGE_TILES = 8.0;
    public static final int ATTACK_CLOUD_TICKS = 12;
    public static final int PLANTFOOD_CLOUD_TICKS = 42;
    public static final int HIT_DELAY_TICKS = 7;
    public static final int HIT_INTERVAL_TICKS = 2;

    private FumeMuzzles() {
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

    public static boolean isHitTick(int ageTicks) {
        if (ageTicks < HIT_DELAY_TICKS) {
            return false;
        }
        return (ageTicks - HIT_DELAY_TICKS) % HIT_INTERVAL_TICKS == 0;
    }

    public static boolean inRangeFromCenter(double plantCenterX, double zombieX, double range) {
        double dx = zombieX - plantCenterX;
        return dx >= -0.05 && dx <= range;
    }
}
