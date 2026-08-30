package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HypnoShroomPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 24;
    private static final int SETUP_TICKS = 5;
    private static final double GARGANTUAR_HEALTH = 3.0;
    private static final double GARGANTUAR_DAMAGE = 3.0;
    private int tickTimer;
    private boolean applied;

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
            hypnotizeGargantuarTier(plant, context);
            applied = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
    }

    private void hypnotizeGargantuarTier(Plant plant, GameContext context) {
        List<Zombie> candidates = new ArrayList<>();
        for (Zombie zombie : context.getAllZombies()) {
            if (zombie.isAlive() && !zombie.isHypnotized()) {
                candidates.add(zombie);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Collections.shuffle(candidates);
        Zombie target = candidates.getFirst();
        double healthMultiplier = Math.max(GARGANTUAR_HEALTH, plant.getStats()
                .specialModifier(PlantSpecialModifiers.ZOMBIE_HEALTH_MULTIPLIER));
        double damageMultiplier = Math.max(GARGANTUAR_DAMAGE, plant.getStats()
                .specialModifier(PlantSpecialModifiers.ZOMBIE_DAMAGE_MULTIPLIER));
        target.hypnotize(healthMultiplier, damageMultiplier);
    }
}
