package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.EndurianMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;


public final class EndurianAbility implements PlantAbility {

    private int ticksElapsed;
    private int pulseAt;
    private int attackUntil;
    private boolean pulsed;
    private boolean attacking;

    public void cancelWindup() {
        ticksElapsed = 0;
        pulseAt = 0;
        attackUntil = 0;
        pulsed = false;
        attacking = false;
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
                context.dealEndurianSpikes(plant, EndurianMuzzles.spikeDamage(plant));
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
        pulseAt = EndurianMuzzles.pulseTicks();
        attackUntil = EndurianMuzzles.attackDurationTicks();
        ticksElapsed = 0;
        pulsed = false;
        attacking = true;
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return attackUntil > 0 ? attackUntil : EndurianMuzzles.attackDurationTicks();
    }

    static boolean hasTarget(Plant plant, GameContext context) {
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        int radius = EndurianMuzzles.RADIUS;
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
