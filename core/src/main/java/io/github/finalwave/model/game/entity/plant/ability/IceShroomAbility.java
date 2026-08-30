package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public final class IceShroomAbility implements PlantAbility {

    private static final int BASE_HEALTH = 300;
    private static final int BASE_DAMAGE = 40;
    private static final double ATTACK_INTERVAL = 1.5;
    private static final int AURA_RADIUS = 1;
    private static final int CHILL_TICKS = 20;
    private static final double MELEE_RANGE = 1.25;
    private static final int ATTACK_ANIM_TICKS = 13;

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        int damage = Math.max(BASE_DAMAGE, plant.getStats().damage());
        plant.tuneCombatStats(BASE_HEALTH, damage, ATTACK_INTERVAL, context);
        plant.primeActionCooldown();
    }

    @Override
    public void onTick(Plant plant, GameContext context) {
        chillAura(plant, context);
        if (plant.getIceShroomAttackTicks() <= 0 && findMeleeTarget(plant, context) != null) {
            plant.primeActionCooldown();
        }
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (plant.getIceShroomAttackTicks() > 0) {
            return false;
        }
        Zombie target = findMeleeTarget(plant, context);
        if (target == null) {
            return false;
        }
        plant.setIceShroomAttackTicks(ATTACK_ANIM_TICKS);
        int damage = Math.max(BASE_DAMAGE, plant.getStats().damage());
        target.takeDamage(damage);
        target.applyChill(chillDurationTicks(plant));
        if (target.isDead()) {
            context.onZombieKilled(target);
        }
        return true;
    }

    private void chillAura(Plant plant, GameContext context) {
        int centerCol = plant.getCol();
        int centerRow = plant.getRow();
        int chillTicks = chillDurationTicks(plant);
        for (Zombie zombie : context.getAllZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zombieCol = (int) Math.floor(zombie.getX());
            if (Math.abs(zombieCol - centerCol) <= AURA_RADIUS
                    && Math.abs(zombie.getRow() - centerRow) <= AURA_RADIUS) {
                zombie.applyChill(chillTicks);
            }
        }
    }

    private int chillDurationTicks(Plant plant) {
        return CHILL_TICKS + (int) Math.round(plant.getStats()
                .specialModifier(PlantSpecialModifiers.FREEZE_DURATION_EXT) * GameSession.TICKS_PER_SECOND);
    }

    private static Zombie findMeleeTarget(Plant plant, GameContext context) {
        Zombie closest = null;
        double closestX = Double.MAX_VALUE;
        for (Zombie zombie : context.getZombiesInRow(plant.getRow())) {
            if (!zombie.isAlive() || zombie.isHypnotized()) {
                continue;
            }
            double distance = zombie.getX() - plant.getCol();
            if (distance >= -0.1 && distance <= MELEE_RANGE && zombie.getX() < closestX) {
                closest = zombie;
                closestX = zombie.getX();
            }
        }
        return closest;
    }
}
