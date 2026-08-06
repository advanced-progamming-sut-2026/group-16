package io.github.finalwave.model.game.entity.plant;

import io.github.finalwave.model.game.entity.GameContext;

public final class PlantBehaviorSupport {

    private PlantBehaviorSupport() {
    }

    public static void onPlanted(Plant plant, GameContext context, int ticksPerSecond) {
        if (plant.hasTag(PlantTag.WARM_UP)) {
            plant.setGrowthStage(0);
            plant.setGrowthTicksRemaining((int) Math.ceil(
                    Math.max(0.1, plant.getStats().actionInterval()) * ticksPerSecond));
        }
        if (plant.hasTag(PlantTag.CHARGE)) {
            plant.setChargeTicksRemaining((int) Math.ceil(
                    Math.max(0.1, plant.getStats().actionInterval()) * ticksPerSecond));
        }
    }

    public static void tick(Plant plant, int ticksPerSecond) {
        if (plant.hasTag(PlantTag.WARM_UP) && plant.getGrowthStage() < plant.maxGrowthStage()) {
            plant.decrementGrowthTicks();
            if (plant.getGrowthTicksRemaining() <= 0) {
                plant.advanceGrowthStage(ticksPerSecond);
            }
        }
        if (plant.hasTag(PlantTag.CHARGE) && plant.getChargeTicksRemaining() > 0) {
            plant.decrementChargeTicks();
        }
    }

    public static boolean canAct(Plant plant) {
        if (plant.hasTag(PlantTag.WARM_UP) && plant.getGrowthStage() < plant.maxGrowthStage()) {
            return false;
        }
        if (plant.hasTag(PlantTag.CHARGE) && plant.getChargeTicksRemaining() > 0) {
            return false;
        }
        return true;
    }

    public static double actionIntervalTicks(Plant plant, int ticksPerSecond) {
        double interval = plant.getStats().actionInterval();
        if (plant.hasTag(PlantTag.WARM_UP)) {
            interval = interval / Math.max(1, plant.getGrowthStage());
        }
        return Math.max(1, interval * ticksPerSecond);
    }
}
