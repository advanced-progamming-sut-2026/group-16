package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public final class FirePeashooterPlantFoodEffect implements PlantFoodEffect {

    private static final int INTRO_TICKS = 10;
    private static final int LOOP_TICKS = 30;
    private static final int OUTRO_TICKS = 8;
    private static final int DAMAGE_INTERVAL = 4;
    private static final int DAMAGE_PER_TICK = 225;

    private int tickTimer;
    private int durationTicks;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        durationTicks = INTRO_TICKS + LOOP_TICKS + OUTRO_TICKS;
        plant.beginPlantFood(durationTicks, INTRO_TICKS, OUTRO_TICKS);
        thawRow(plant, context);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer <= INTRO_TICKS || tickTimer > INTRO_TICKS + LOOP_TICKS) {
            return;
        }
        int loopTick = tickTimer - INTRO_TICKS;
        if (loopTick % DAMAGE_INTERVAL != 0) {
            return;
        }
        burnRow(plant, context);
        thawRow(plant, context);
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
    }

    private static void burnRow(Plant plant, GameContext context) {
        int row = plant.getRow();
        for (Zombie zombie : context.getZombiesInRow(row)) {
            if (zombie == null || !zombie.isAlive() || zombie.getX() < plant.getCol()) {
                continue;
            }
            zombie.clearColdStatuses();
            zombie.takeDirectDamage(DAMAGE_PER_TICK);
            if (zombie.isDead()) {
                context.onZombieKilled(zombie);
            }
        }
    }

    private static void thawRow(Plant plant, GameContext context) {
        int row = plant.getRow();
        for (int col = plant.getCol(); col < context.getColCount(); col++) {
            Plant target = context.getPlantAt(col, row);
            if (target != null && target.isAlive()) {
                target.clearHostileIce();
            }
            context.damageIceAt(col, row, Integer.MAX_VALUE / 4);
        }
    }
}
