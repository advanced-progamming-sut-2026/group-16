package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class SunProductionAbility implements PlantAbility {

    private final double baseAmount;

    public SunProductionAbility(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        double amount = baseAmount + plant.getStats().specialModifier("SUN_AMOUNT_BUFF");
        context.spawnSun(plant, amount);
    }
}
