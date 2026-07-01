package model.game.entity.plant.ability;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;

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
