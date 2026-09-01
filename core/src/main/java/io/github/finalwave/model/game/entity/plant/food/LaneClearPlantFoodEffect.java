package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;


public final class LaneClearPlantFoodEffect implements PlantFoodEffect {

    private final int damage;
    private final ProjectileEffect effect;
    private final int fireDelayTicks;
    private final int durationTicks;
    private int tickTimer;
    private boolean fired;

    public LaneClearPlantFoodEffect(int damage, ProjectileEffect effect, int fireDelayTicks, int durationTicks) {
        this.damage = damage;
        this.effect = effect;
        this.fireDelayTicks = Math.max(1, fireDelayTicks);
        this.durationTicks = Math.max(fireDelayTicks + 1, durationTicks);
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        fired = false;
        plant.beginPlantFood(durationTicks, 0, 0);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!fired && tickTimer == fireDelayTicks) {
            context.spawnLaneClearProjectile(plant, damage, effect);
            fired = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        fired = false;
    }
}
