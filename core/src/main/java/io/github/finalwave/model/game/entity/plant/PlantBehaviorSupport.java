package io.github.finalwave.model.game.entity.plant;

import io.github.finalwave.model.game.entity.GameContext;

import java.util.concurrent.ThreadLocalRandom;

public final class PlantBehaviorSupport {

    private PlantBehaviorSupport() {
    }

    public static void onPlanted(Plant plant, GameContext context, int ticksPerSecond) {
        if (plant.hasTag(PlantTag.WARM_UP)) {
            plant.setGrowthStage(0);
            plant.setGrowthTicksRemaining((int) Math.ceil(
                    Math.max(0.1, plant.getStats().actionInterval()) * ticksPerSecond));
        }
        if (plant.hasTag(PlantTag.CHARGE) && !"Bowling Bulb".equals(plant.getName())) {
            plant.setChargeTicksRemaining((int) Math.ceil(
                    Math.max(0.1, plant.getStats().actionInterval()) * ticksPerSecond));
        }
        if ("Caulipower".equals(plant.getName()) || "Electric Blueberry".equals(plant.getName())) {
            plant.setVisualIdleVariant(1 + ThreadLocalRandom.current().nextInt(4));
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
        if (plant.getRecoveryTicksRemaining() > 0) {
            plant.decrementRecoveryTicks();
        }
        if (plant.getReloadTicksRemaining() > 0 && plant.isBowlingReloading()) {
            plant.decrementReloadTicks();
            if (plant.getReloadTicksRemaining() <= 0) {
                plant.setBowlingReloading(false);
                plant.setBowlingAmmo(1);
            }
        }
    }

    public static boolean canAct(Plant plant) {
        if (plant.hasTag(PlantTag.WARM_UP)
                && plant.getGrowthStage() < plant.maxGrowthStage()
                && plant.getCategory() != PlantCategory.SUN_PRODUCER) {
            return false;
        }
        if (plant.getRecoveryTicksRemaining() > 0) {
            return false;
        }
        if (plant.isBowlingReloading() || plant.getReloadTicksRemaining() > 0) {
            return false;
        }
        if (plant.hasTag(PlantTag.CHARGE) && plant.getChargeTicksRemaining() > 0
                && !"Bowling Bulb".equals(plant.getName())) {
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
