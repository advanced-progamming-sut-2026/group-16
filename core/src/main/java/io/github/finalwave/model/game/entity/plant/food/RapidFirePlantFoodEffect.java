package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.PlantShotPatterns;

public final class RapidFirePlantFoodEffect implements PlantFoodEffect {

    private final boolean rapidFire;
    private final int giantShots;
    private final boolean fourWay;
    private int tickTimer;
    private int barrageTicks;
    private int durationTicks;
    private int giantShotsFired;

    public RapidFirePlantFoodEffect(boolean rapidFire, int giantShots, boolean fourWay) {
        this.rapidFire = rapidFire;
        this.giantShots = Math.max(0, giantShots);
        this.fourWay = fourWay;
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        giantShotsFired = 0;
        barrageTicks = fourWay
                ? PlantShotPatterns.ROTOBAGA_PLANT_FOOD_TICKS
                : PlantShotPatterns.RAPID_FIRE_DURATION_TICKS;
        int finale = giantShots > 0 ? PlantShotPatterns.GIANT_PEA_PHASE_TICKS : 0;
        durationTicks = barrageTicks + finale;
        plant.beginPlantFood(durationTicks, 0, finale);
        plant.setAttacking(true);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (tickTimer > durationTicks) {
            return;
        }
        int interval = fourWay ? 2 : PlantShotPatterns.RAPID_FIRE_INTERVAL_TICKS;
        if (rapidFire && tickTimer <= barrageTicks
                && tickTimer % interval == 0) {
            if (fourWay) {
                PlantShotPatterns.fireRotobaga(plant, context);
            } else {
                PlantShotPatterns.fireScatteredPeas(plant, context);
            }
        }
        if (giantShotsFired < giantShots
                && tickTimer == barrageTicks + PlantShotPatterns.GIANT_PEA_FIRE_DELAY_TICKS) {
            PlantShotPatterns.fireGiantPea(plant, context);
            giantShotsFired++;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
        giantShotsFired = 0;
    }
}
