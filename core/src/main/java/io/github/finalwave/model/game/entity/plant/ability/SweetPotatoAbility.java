package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public final class SweetPotatoAbility implements PlantAbility {

    private int tickCounter;

    @Override
    public void onTick(Plant plant, GameContext context) {
        tickCounter++;
        if (tickCounter >= context.getTicksPerSecond()) {
            tickCounter = 0;
            attractAdjacent(plant, context, 1);
        }
    }

    public static void attractAdjacent(Plant plant, GameContext context, int columnReach) {
        int row = plant.getRow();
        int col = plant.getCol();
        for (Zombie zombie : context.getAllZombies()) {
            if (!zombie.isAlive() || zombie.isHypnotized()) {
                continue;
            }
            int zombieCol = (int) Math.floor(zombie.getX());
            if (Math.abs(zombieCol - col) > columnReach) {
                continue;
            }
            for (int occupiedRow : zombie.occupiedRows()) {
                if (occupiedRow != row && Math.abs(occupiedRow - row) == 1) {
                    zombie.setRow(row);
                    break;
                }
            }
        }
    }

    public static void attractArea(Plant plant, GameContext context) {
        int centerCol = plant.getCol();
        int centerRow = plant.getRow();
        for (Zombie zombie : context.getAllZombies()) {
            if (!zombie.isAlive() || zombie.isHypnotized()) {
                continue;
            }
            int zombieCol = (int) Math.floor(zombie.getX());
            if (Math.abs(zombieCol - centerCol) > 2) {
                continue;
            }
            for (int occupiedRow : zombie.occupiedRows()) {
                if (Math.abs(occupiedRow - centerRow) <= 1) {
                    zombie.setRow(centerRow);
                    break;
                }
            }
        }
    }
}
