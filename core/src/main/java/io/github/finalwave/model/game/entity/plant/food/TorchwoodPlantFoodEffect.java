package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.PlantShotPatterns;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class TorchwoodPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 36;
    private static final int SETUP_TICKS = 6;
    private static final int BURST_TICKS = 24;
    private int tickTimer;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        plant.restoreHealth(plant.getMaxHealth());
        plant.setTorchwoodBoosted(true);
        plant.setAttacking(true);
        plant.beginPlantFood(DURATION_TICKS, SETUP_TICKS);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer <= SETUP_TICKS || tickTimer > SETUP_TICKS + BURST_TICKS) {
            return;
        }
        if ((tickTimer - SETUP_TICKS) % PlantShotPatterns.RAPID_FIRE_INTERVAL_TICKS != 0) {
            return;
        }
        int damage = Math.max(1, plant.getStats().damage() > 0 ? plant.getStats().damage() * 2 : 40);
        context.spawnDirectedProjectile(
                plant,
                damage,
                PlantShotPatterns.TILE_SPEED,
                0,
                1f,
                0,
                0,
                ProjectileEffect.FIRE);
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
    }
}
