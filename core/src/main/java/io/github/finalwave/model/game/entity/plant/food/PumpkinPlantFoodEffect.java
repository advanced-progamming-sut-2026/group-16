package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class PumpkinPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 28;
    private static final int SETUP_TICKS = 4;
    private final int armorValue;
    private int tickTimer;
    private boolean applied;

    public PumpkinPlantFoodEffect(double armorValue) {
        this.armorValue = Math.max(0, (int) Math.round(armorValue));
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
        plant.beginPlantFood(DURATION_TICKS, SETUP_TICKS);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!applied && tickTimer >= SETUP_TICKS) {
            context.grantArmor(plant, armorValue);
            plant.setPumpkinShellTier(4);
            applied = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
    }
}
