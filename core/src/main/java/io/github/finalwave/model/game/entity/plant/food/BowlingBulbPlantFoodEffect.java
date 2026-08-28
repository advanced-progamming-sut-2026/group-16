package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.PlantShotPatterns;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class BowlingBulbPlantFoodEffect implements PlantFoodEffect {

    private static final int SETUP_TICKS = 12;
    private static final int GIANT_INTERVAL_TICKS = 20;
    private static final float GIANT_SCALE = 1.8f;

    private int tickTimer;
    private int barrageTicks;
    private int durationTicks;
    private int giantShotsFired;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        giantShotsFired = 0;
        barrageTicks = PlantShotPatterns.RAPID_FIRE_DURATION_TICKS;
        durationTicks = SETUP_TICKS + barrageTicks;
        plant.beginPlantFood(durationTicks, SETUP_TICKS, 0);
        plant.setAttacking(true);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer > durationTicks) {
            return;
        }
        if (tickTimer > SETUP_TICKS && tickTimer <= SETUP_TICKS + barrageTicks) {
            int phase = tickTimer - SETUP_TICKS;
            if (phase % PlantShotPatterns.RAPID_FIRE_INTERVAL_TICKS == 0) {
                context.spawnBowlingProjectile(
                        plant, plant.getStats().damage(), ProjectileEffect.BOWLING_CYAN,
                        ProjectileProfile.straight());
            }
            if (phase % GIANT_INTERVAL_TICKS == 0 && giantShotsFired < 3) {
                context.spawnBowlingProjectile(
                        plant,
                        plant.getStats().damage() * PlantShotPatterns.GIANT_PEA_DAMAGE_MULTIPLIER,
                        ProjectileEffect.BOWLING_PF,
                        ProjectileProfile.straight());
                giantShotsFired++;
            }
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
        giantShotsFired = 0;
    }
}
