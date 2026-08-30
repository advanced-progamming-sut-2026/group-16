package io.github.finalwave.model.game.entity.plant.support;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.Random;

public final class PlantLaneSupport {

    private static final int GARLIC_STUN_TICKS = 75;
    private static final int GARLIC_DIVERT_TICKS_LIGHT = 11;
    private static final int GARLIC_DIVERT_TICKS_HEAVY = 16;

    private PlantLaneSupport() {
    }

    public static void scheduleGarlicDivert(Zombie zombie, Plant plant) {
        if (zombie == null || plant == null || !"Garlic".equals(plant.getName())) {
            return;
        }
        zombie.scheduleGarlicDivert(plant.getCol(), plant.getRow(), garlicDivertDelayTicks(plant));
    }

    public static void divertBiter(Zombie zombie, Plant plant, GameContext context) {
        if (zombie == null || plant == null) {
            return;
        }
        divertToAdjacentLane(zombie, plant.getRow(), context);
        zombie.applyStun(GARLIC_STUN_TICKS);
    }

    public static void divertBiter(Zombie zombie, int sourceRow, GameContext context) {
        if (zombie == null || context == null) {
            return;
        }
        divertToAdjacentLane(zombie, sourceRow, context);
        zombie.applyStun(GARLIC_STUN_TICKS);
    }

    public static void divertRowZombies(Plant plant, GameContext context, Random random) {
        int row = plant.getRow();
        for (Zombie zombie : context.getZombiesInRow(row)) {
            if (!zombie.isAlive() || zombie.getX() < plant.getCol()) {
                continue;
            }
            divertToAdjacentLane(zombie, row, context.getRowCount(), random);
            zombie.applyStun(GARLIC_STUN_TICKS);
        }
    }

    private static int garlicDivertDelayTicks(Plant plant) {
        if (plant == null || plant.getMaxHealth() <= 0) {
            return GARLIC_DIVERT_TICKS_LIGHT;
        }
        double ratio = plant.getHealth() / (double) plant.getMaxHealth();
        return ratio > 0.33 ? GARLIC_DIVERT_TICKS_LIGHT : GARLIC_DIVERT_TICKS_HEAVY;
    }

    private static void divertToAdjacentLane(Zombie zombie, int plantRow, GameContext context) {
        divertToAdjacentLane(zombie, plantRow, context.getRowCount(), null);
    }

    private static void divertToAdjacentLane(Zombie zombie, int plantRow, int rowCount, Random random) {
        int upper = plantRow - 1;
        int lower = plantRow + 1;
        if (upper < 0 && lower >= rowCount) {
            return;
        }
        if (upper < 0) {
            zombie.setRow(lower);
            return;
        }
        if (lower >= rowCount) {
            zombie.setRow(upper);
            return;
        }
        if (random != null) {
            zombie.setRow(random.nextBoolean() ? upper : lower);
            return;
        }
        zombie.setRow((zombie.getId().hashCode() & 1) == 0 ? upper : lower);
    }
}
