package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;

public final class GraveBusterAbility implements PlantAbility {

    public static final float ATTACK_CLIP_SECONDS = 1.0f;
    public static final float ATTACK1_CLIP_SECONDS = 0.6667f;
    public static final double BASE_EAT_SECONDS = 4.0;

    private int plantedTick = -1;
    private boolean finished;

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        plantedTick = context.getCurrentTick();
        plant.setAttacking(true);
        plant.setGraveBusting(false);
    }

    @Override
    public void onTick(Plant plant, GameContext context) {
        if (finished || plantedTick < 0 || !plant.isAlive()) {
            return;
        }
        int elapsed = context.getCurrentTick() - plantedTick;
        int introTicks = context.getTicksPerSecond();
        int eatTicks = eatDurationTicks(plant, context);
        if (elapsed >= introTicks + eatTicks) {
            finish(plant, context);
            return;
        }
        if (elapsed >= introTicks && !plant.isGraveBusting()) {
            plant.setAttacking(false);
            plant.setGraveBusting(true);
        }
    }

    private static int eatDurationTicks(Plant plant, GameContext context) {
        return Math.max(1, (int) Math.round(eatSeconds(plant) * context.getTicksPerSecond()));
    }

    public static float eatSeconds(Plant plant) {
        return (float) Math.max(0.0,
                BASE_EAT_SECONDS + plant.getStats().specialModifier(PlantSpecialModifiers.EAT_TIME_REDUCTION));
    }

    private void finish(Plant plant, GameContext context) {
        if (finished) {
            return;
        }
        finished = true;
        context.clearGraveAt(plant.getCol(), plant.getRow());
        plant.consumeInstantly();
    }
}
