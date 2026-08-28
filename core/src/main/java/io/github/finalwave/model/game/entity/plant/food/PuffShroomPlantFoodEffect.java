package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class PuffShroomPlantFoodEffect implements PlantFoodEffect {

    private static final int INTRO_TICKS = 6;
    private static final int LOOP_TICKS = 30;
    private static final int OUTRO_TICKS = 6;
    private static final int SPORE_COUNT = 30;

    private int tickTimer;
    private int durationTicks;
    private int sporesFired;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        sporesFired = 0;
        durationTicks = INTRO_TICKS + LOOP_TICKS + OUTRO_TICKS;
        plant.beginPlantFood(durationTicks, INTRO_TICKS, OUTRO_TICKS);
        plant.setAttacking(true);
        ShroomFamilyPlantFoodSupport.resetFamilyLifespan(context, "Puff-shroom");
        ShroomFamilyPlantFoodSupport.triggerFamilyShots(context, "Puff-shroom");
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer <= INTRO_TICKS || tickTimer > INTRO_TICKS + LOOP_TICKS) {
            return;
        }
        if (sporesFired >= SPORE_COUNT) {
            return;
        }
        int damage = plant.getStats().damage() * 2;
        context.spawnProjectile(plant, damage, 1, ProjectileProfile.straight());
        sporesFired++;
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
        sporesFired = 0;
    }
}
