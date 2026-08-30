package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.KiwibeastMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;


public final class KiwibeastAbility implements PlantAbility {

    private int ticksElapsed;
    private int pulseAt;
    private int attackUntil;
    private boolean pulsed;
    private int forcedMinStage = 1;
    private int lastStage = 1;
    private int growthToken;
    private boolean attacking;

    public int stage(Plant plant) {
        return Math.max(forcedMinStage, KiwibeastMuzzles.hpStage(plant));
    }

    public int growthToken() {
        return growthToken;
    }

    public void forceMinStage(int stage) {
        int next = KiwibeastMuzzles.clampStage(stage);
        if (next > forcedMinStage) {
            forcedMinStage = next;
        }
    }

    public void notifyGrowth() {
        growthToken++;
    }

    public void cancelWindup() {
        ticksElapsed = 0;
        pulseAt = 0;
        attackUntil = 0;
        pulsed = false;
        attacking = false;
    }

    public void onDamaged(Plant plant, GameContext context) {
        if (plant == null || !plant.isAlive()) {
            return;
        }
        int next = stage(plant);
        if (next <= lastStage) {
            return;
        }
        lastStage = next;
        cancelWindup();
        plant.setAttacking(false);
        growthToken++;
        if (context != null) {
            context.knockbackEatingZombies(plant);
        }
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (plant.isPlantFooding()) {
            cancelWindup();
            plant.setAttacking(false);
            return false;
        }
        if (attacking) {
            ticksElapsed++;
            if (!pulsed && ticksElapsed >= pulseAt) {
                executeShockwave(plant, context);
                pulsed = true;
            }
            if (ticksElapsed >= attackUntil) {
                cancelWindup();
                plant.setAttacking(false);
                return true;
            }
            return false;
        }
        if (!hasTarget(plant, context)) {
            return false;
        }
        int current = stage(plant);
        lastStage = current;
        pulseAt = KiwibeastMuzzles.pulseTicks(current);
        attackUntil = KiwibeastMuzzles.attackDurationTicks(current);
        ticksElapsed = 0;
        pulsed = false;
        attacking = true;
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return attackUntil > 0
                ? attackUntil
                : KiwibeastMuzzles.attackDurationTicks(1);
    }

    private void executeShockwave(Plant plant, GameContext context) {
        int current = stage(plant);
        int damage = KiwibeastMuzzles.stageDamage(plant, current);
        int radius = KiwibeastMuzzles.radius(current);
        context.dealKiwibeastShockwave(plant, damage, radius, false);
    }

    static boolean hasTarget(Plant plant, GameContext context) {
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        int radius = KiwibeastMuzzles.radius(
                plant.getAbility() instanceof KiwibeastAbility kiwi
                        ? kiwi.stage(plant)
                        : KiwibeastMuzzles.hpStage(plant));
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
}
