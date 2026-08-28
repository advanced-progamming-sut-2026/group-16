package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ElectricBlueberryPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 20;
    private static final int STRIKE_DELAY_TICKS = 10;
    private static final int STRIKE_DAMAGE = 10000;

    private final int targets;
    private int tickTimer;
    private boolean struck;

    public ElectricBlueberryPlantFoodEffect(int targets) {
        this.targets = Math.max(1, targets);
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        struck = false;
        plant.beginPlantFood(DURATION_TICKS, 0, 0);
        plant.setAttacking(true);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!struck && tickTimer == STRIKE_DELAY_TICKS) {
            strikeRandomTargets(context);
            struck = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        plant.setAttacking(false);
        tickTimer = 0;
        struck = false;
    }

    private void strikeRandomTargets(GameContext context) {
        List<Zombie> candidates = new ArrayList<>();
        for (Zombie zombie : context.getAllZombies()) {
            if (zombie != null && zombie.isAlive() && !zombie.isHypnotized()) {
                candidates.add(zombie);
            }
        }
        Collections.shuffle(candidates);
        int count = Math.min(targets, candidates.size());
        for (int i = 0; i < count; i++) {
            Zombie zombie = candidates.get(i);
            zombie.takeDirectDamage(STRIKE_DAMAGE);
            if (zombie.isDead()) {
                context.onZombieKilled(zombie);
            }
        }
    }
}
