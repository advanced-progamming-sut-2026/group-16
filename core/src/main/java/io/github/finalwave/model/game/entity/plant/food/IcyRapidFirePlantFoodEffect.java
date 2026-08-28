package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.PlantShotPatterns;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public final class IcyRapidFirePlantFoodEffect implements PlantFoodEffect {

    private static final int SETUP_TICKS = 11;
    private static final int OUTRO_TICKS = 3;
    private static final int ROW_FREEZE_TICKS = 150;

    private int tickTimer;
    private int barrageTicks;
    private int durationTicks;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        barrageTicks = PlantShotPatterns.RAPID_FIRE_DURATION_TICKS;
        durationTicks = SETUP_TICKS + barrageTicks + OUTRO_TICKS;
        plant.beginPlantFood(durationTicks, SETUP_TICKS, OUTRO_TICKS);
        plant.setAttacking(true);
        freezeRow(plant, context);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer > durationTicks) {
            return;
        }
        if (tickTimer > SETUP_TICKS && tickTimer <= SETUP_TICKS + barrageTicks
                && tickTimer % PlantShotPatterns.RAPID_FIRE_INTERVAL_TICKS == 0) {
            PlantShotPatterns.fireScatteredPeas(plant, context);
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
    }

    private static void freezeRow(Plant plant, GameContext context) {
        for (Zombie zombie : context.getZombiesInRow(plant.getRow())) {
            if (zombie != null && zombie.isAlive()) {
                zombie.applyFreeze(ROW_FREEZE_TICKS);
            }
        }
    }
}
