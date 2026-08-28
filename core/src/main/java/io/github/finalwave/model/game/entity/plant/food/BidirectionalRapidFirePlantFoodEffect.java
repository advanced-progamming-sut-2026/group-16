package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.PlantShotPatterns;
import io.github.finalwave.model.game.entity.plant.ability.ProjectileAttackAbility;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class BidirectionalRapidFirePlantFoodEffect implements PlantFoodEffect {

    private int tickTimer;
    private int barrageTicks;
    private int durationTicks;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        barrageTicks = PlantShotPatterns.RAPID_FIRE_DURATION_TICKS;
        durationTicks = barrageTicks;
        plant.beginPlantFood(durationTicks, 0, 0);
        plant.setAttacking(true);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer > durationTicks) {
            return;
        }
        if (tickTimer % PlantShotPatterns.RAPID_FIRE_INTERVAL_TICKS != 0) {
            return;
        }
        ProjectileProfile profile = plant.getAbility() instanceof ProjectileAttackAbility attack
                ? attack.getProfile()
                : ProjectileProfile.straight();
        PlantShotPatterns.fireScatteredPeas(plant, context);
        context.spawnReverseProjectile(plant, plant.getStats().damage(), 1, profile);
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
    }
}
