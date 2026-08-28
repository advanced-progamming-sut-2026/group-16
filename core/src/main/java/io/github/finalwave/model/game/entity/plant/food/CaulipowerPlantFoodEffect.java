package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class CaulipowerPlantFoodEffect implements PlantFoodEffect {

    private static final int SETUP_TICKS = 4;
    private static final int LOOP_TICKS = 36;
    private static final int OUTRO_TICKS = 4;

    private final int targets;
    private int tickTimer;
    private int durationTicks;
    private boolean applied;

    public CaulipowerPlantFoodEffect(int targets) {
        this.targets = Math.max(1, targets);
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
        durationTicks = SETUP_TICKS + LOOP_TICKS + OUTRO_TICKS;
        plant.beginPlantFood(durationTicks, SETUP_TICKS, OUTRO_TICKS);
        plant.setAttacking(true);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!applied && tickTimer == SETUP_TICKS + 1) {
            context.hypnotizeRandomZombies(plant, targets);
            applied = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
        applied = false;
    }
}
