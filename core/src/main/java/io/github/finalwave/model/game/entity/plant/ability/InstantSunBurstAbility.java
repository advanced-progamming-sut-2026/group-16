package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class InstantSunBurstAbility implements PlantAbility {

    private final double amount;

    public InstantSunBurstAbility(double amount) {
        this.amount = amount;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        double total = amount + plant.getStats().specialModifier("SUN_AMOUNT_BUFF");
        context.spawnSun(plant, total);
        plant.consumeInstantly();
    }
}
