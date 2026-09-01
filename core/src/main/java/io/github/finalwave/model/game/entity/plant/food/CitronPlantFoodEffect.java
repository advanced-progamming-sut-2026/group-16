package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.LawnBurst;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantBehaviorSupport;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;


public final class CitronPlantFoodEffect implements PlantFoodEffect {

    public static final int DURATION_TICKS = 16;
    public static final int LIGHTNING_TICK = 1;
    public static final int SHOCK_TICK = 4;
    public static final int FIRE_TICK = 10;

    private final int damage;
    private int tickTimer;
    private boolean fired;

    public CitronPlantFoodEffect(int damage) {
        this.damage = Math.max(1, damage);
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        fired = false;
        plant.setRecoveryTicksRemaining(0);
        plant.setChargeTicksRemaining(0);
        plant.beginPlantFood(DURATION_TICKS, 0, 0);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer == LIGHTNING_TICK) {
            context.queueLawnBurst(new LawnBurst(
                    LawnBurst.Kind.CITRON_PF_LIGHTNING, plant.getCol(), plant.getRow()));
        }
        if (tickTimer == SHOCK_TICK) {
            context.queueLawnBurst(new LawnBurst(
                    LawnBurst.Kind.CITRON_PF_SHOCK, plant.getCol(), plant.getRow()));
            context.queueLawnBurst(new LawnBurst(
                    LawnBurst.Kind.CITRON_PF_HIT, plant.getCol(), plant.getRow()));
        }
        if (!fired && tickTimer == FIRE_TICK) {
            context.spawnLaneClearProjectile(plant, damage, ProjectileEffect.PLASMA_PF);
            fired = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        fired = false;
        plant.setChargeTicksRemaining(PlantBehaviorSupport.chargeTicks(
                plant, context.getTicksPerSecond()));
    }
}
