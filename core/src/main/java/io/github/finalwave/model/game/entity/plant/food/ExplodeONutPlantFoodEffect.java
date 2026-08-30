package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class ExplodeONutPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 30;
    private static final int SETUP_TICKS = 8;
    private static final int OUTRO_TICKS = 6;
    private final int armorValue;
    private int tickTimer;
    private boolean applied;

    public ExplodeONutPlantFoodEffect(double armorValue) {
        this.armorValue = Math.max(0, (int) Math.round(armorValue));
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
        plant.beginPlantFood(DURATION_TICKS, SETUP_TICKS, OUTRO_TICKS);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!applied && tickTimer >= SETUP_TICKS) {
            plant.setExplodeONutPfArmor(true);
            context.grantArmor(plant, armorValue);
            applied = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setExplodeONutPfArmor(false);
        tickTimer = 0;
        applied = false;
    }
}
