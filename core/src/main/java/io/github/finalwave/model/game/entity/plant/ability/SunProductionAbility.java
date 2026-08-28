package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.item.SunType;

public final class SunProductionAbility implements PlantAbility {

    private final double baseAmount;

    public SunProductionAbility(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        double amount = produceAmount(plant) + plant.getStats().specialModifier("SUN_AMOUNT_BUFF");
        context.spawnSun(plant, amount, sunType(plant));
    }

    private double produceAmount(Plant plant) {
        if ("Sun-shroom".equals(plant.getName())) {
            return 25.0 * (plant.getGrowthStage() + 1);
        }
        return baseAmount;
    }

    private static SunType sunType(Plant plant) {
        if ("Twin Sunflower".equals(plant.getName())) {
            return SunType.SPECIAL;
        }
        return SunType.NORMAL;
    }
}
