package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.entity.plant.Plant;


public final class PeaPodMuzzles {
    public static final double GIANT_X = 0.36;
    public static final double GIANT_Y = -0.32;

    private static final double[][][] OFFSETS = {
            {{0.18, -0.32}},
            {{-0.08, -0.16}, {0.18, -0.32}},
            {{-0.08, -0.16}, {0.18, -0.32}, {0.12, -0.30}},
            {{-0.08, -0.16}, {0.18, -0.32}, {0.12, -0.30}, {-0.08, 0.00}},
            {{-0.08, 0.16}, {-0.08, -0.16}, {0.18, -0.32}, {0.12, -0.30}, {-0.08, 0.00}}
    };

    private PeaPodMuzzles() {
    }

    public static double x(int heads, int index) {
        return offset(heads, index, 0);
    }

    public static double y(int heads, int index) {
        return offset(heads, index, 1);
    }

    public static double giantX() {
        return GIANT_X;
    }

    public static double giantY() {
        return GIANT_Y;
    }

    private static double offset(int heads, int index, int axis) {
        double[][] table = OFFSETS[clampHeads(heads) - 1];
        int muzzle = Math.max(0, Math.min(table.length - 1, index));
        return table[muzzle][axis];
    }

    private static int clampHeads(int heads) {
        return Math.max(1, Math.min(Plant.MAX_PEA_POD_STACK, heads));
    }
}
