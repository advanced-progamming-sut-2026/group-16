package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MagnetShroomPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 40;
    private static final int SETUP_TICKS = 6;
    private static final int OUTRO_TICKS = 5;
    private static final int MAX_OBJECTS = 20;
    private static final int THROW_DAMAGE = 300;
    private int tickTimer;
    private boolean applied;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
        plant.beginPlantFood(DURATION_TICKS, SETUP_TICKS, OUTRO_TICKS);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!applied && tickTimer >= SETUP_TICKS) {
            throwMetal(plant, context);
            applied = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
    }

    private void throwMetal(Plant plant, GameContext context) {
        List<Zombie> targets = new ArrayList<>();
        for (Zombie zombie : context.getAllZombies()) {
            if (zombie.isAlive() && !zombie.isHypnotized()) {
                targets.add(zombie);
            }
        }
        Collections.shuffle(targets);
        int thrown = 0;
        for (Zombie zombie : targets) {
            if (thrown >= MAX_OBJECTS) {
                break;
            }
            if (zombie.stripArmorViaMagnet() != null) {
                zombie.takeDamage(THROW_DAMAGE);
                if (zombie.isDead()) {
                    context.onZombieKilled(zombie);
                }
                thrown++;
            }
        }
    }
}
