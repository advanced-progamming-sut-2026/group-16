package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.PhatBeetMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.concurrent.ThreadLocalRandom;


public final class PhatBeetAbility implements PlantAbility {

    private int windupRemaining;
    private int attacksUntilCrit = nextCritWindow();

    public void cancelWindup() {
        windupRemaining = 0;
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (plant.isPlantFooding()) {
            windupRemaining = 0;
            plant.setAttacking(false);
            return false;
        }
        if (windupRemaining > 0) {
            windupRemaining--;
            if (windupRemaining == 0) {
                executeShockwave(plant, context);
                plant.setAttacking(false);
                return true;
            }
            return false;
        }
        if (!hasTarget(plant, context)) {
            return false;
        }
        windupRemaining = PhatBeetMuzzles.attackPulseTicks();
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return PhatBeetMuzzles.attackPulseTicks();
    }

    private void executeShockwave(Plant plant, GameContext context) {
        attacksUntilCrit--;
        boolean crit = attacksUntilCrit <= 0;
        if (crit) {
            attacksUntilCrit = nextCritWindow();
        }
        int damage = plant.getStats().damage();
        if (crit) {
            damage *= PhatBeetMuzzles.CRIT_MULTIPLIER;
        }
        context.dealPhatBeetShockwave(plant, damage);
    }

    static boolean hasTarget(Plant plant, GameContext context) {
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        int radius = PhatBeetMuzzles.INNER_RADIUS;
        for (Zombie zombie : context.getAllZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            if (Math.max(Math.abs(zCol - plantCol), Math.abs(zombie.getRow() - plantRow)) <= radius) {
                return true;
            }
        }
        return false;
    }

    private static int nextCritWindow() {
        return ThreadLocalRandom.current().nextInt(
                PhatBeetMuzzles.MIN_ATTACKS_TO_CRIT,
                PhatBeetMuzzles.MAX_ATTACKS_TO_CRIT + 1);
    }
}
