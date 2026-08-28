package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class MegaGatlingPlantFoodEffect implements PlantFoodEffect {

    private static final int BARRAGE_TICKS = 20;
    private static final int PEAS_PER_TICK = 8;

    private int tickTimer;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        plant.setMegaGatlingBoosted(true);
        plant.beginPlantFood(BARRAGE_TICKS, 0, 0);
        plant.setAttacking(true);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer > BARRAGE_TICKS) {
            return;
        }
        ProjectileProfile profile = ProjectileProfile.straight();
        context.spawnProjectile(plant, plant.getStats().damage(), PEAS_PER_TICK, profile);
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
    }
}
