package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
public final class GooPeashooterPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 28;
    private static final int FIRE_DELAY = 9;

    private int tickTimer;
    private boolean fired;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        fired = false;
        plant.beginPlantFood(DURATION_TICKS, 0, 0);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!fired && tickTimer == FIRE_DELAY) {
            int damage = Math.max(1, (int) Math.round(plant.getStats().damage() * 4.0));
            context.spawnPoisonLaneBall(plant, damage);
            context.addGooLaneTrail(plant, 90);
            fired = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        fired = false;
    }
}
