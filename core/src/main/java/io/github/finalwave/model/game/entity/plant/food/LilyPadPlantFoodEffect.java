package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class LilyPadPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 18;
    private static final int SETUP_TICKS = 3;
    private final int cloneCount;
    private int tickTimer;
    private boolean applied;

    public LilyPadPlantFoodEffect(double cloneCount) {
        this.cloneCount = Math.max(1, (int) Math.round(cloneCount));
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
        plant.beginPlantFood(DURATION_TICKS, SETUP_TICKS);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!applied && tickTimer >= SETUP_TICKS) {
            context.spawnClones(plant, cloneCount);
            healAdjacentPads(plant, context);
            applied = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
    }

    private void healAdjacentPads(Plant plant, GameContext context) {
        int[][] deltas = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] delta : deltas) {
            Plant neighbor = context.getPlantAt(plant.getCol() + delta[0], plant.getRow() + delta[1]);
            if (neighbor != null && neighbor.isAlive() && "Lily Pad".equals(neighbor.getName())) {
                neighbor.restoreHealth(neighbor.getMaxHealth());
            }
        }
    }
}
