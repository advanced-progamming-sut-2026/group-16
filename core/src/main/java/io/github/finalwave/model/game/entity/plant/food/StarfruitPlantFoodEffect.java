package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.PlantShotPatterns;

public final class StarfruitPlantFoodEffect implements PlantFoodEffect {

    private static final int INTRO_TICKS = 8;
    private static final int LOOP_TICKS = 50;
    private static final int OUTRO_TICKS = 8;
    private static final int SHOT_INTERVAL = 5;

    private int tickTimer;
    private int durationTicks;
    private int rotation;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        rotation = 0;
        durationTicks = INTRO_TICKS + LOOP_TICKS + OUTRO_TICKS;
        plant.beginPlantFood(durationTicks, INTRO_TICKS, OUTRO_TICKS);
        plant.setAttacking(true);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer <= INTRO_TICKS || tickTimer > INTRO_TICKS + LOOP_TICKS) {
            return;
        }
        int loopTick = tickTimer - INTRO_TICKS;
        if (loopTick % SHOT_INTERVAL != 0) {
            return;
        }
        PlantShotPatterns.fireStarfruitGiant(plant, context, rotation++);
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
        rotation = 0;
    }
}
