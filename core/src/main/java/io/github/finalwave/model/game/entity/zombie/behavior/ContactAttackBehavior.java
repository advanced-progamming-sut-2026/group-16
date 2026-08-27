package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class ContactAttackBehavior implements ZombieBehavior {

    private final boolean oneUse;
    private final double speedMultiplierAfterHit;
    private final boolean selfDestructOnHit;
    private boolean used;

    public ContactAttackBehavior(boolean oneUse, double speedMultiplierAfterHit) {
        this(oneUse, speedMultiplierAfterHit, false);
    }

    public ContactAttackBehavior(boolean oneUse, double speedMultiplierAfterHit, boolean selfDestructOnHit) {
        this.oneUse = oneUse;
        this.speedMultiplierAfterHit = speedMultiplierAfterHit;
        this.selfDestructOnHit = selfDestructOnHit;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (used && oneUse) {
            return;
        }
        Plant target = context.getPlantInFront(zombie.getX(), zombie.getRow());
        Zombie hypnotizedTarget = nearestHypnotizedCollision(zombie, context);
        boolean canHitPlant = target != null && target.canBeTargetedByZombie();
        if (!canHitPlant && hypnotizedTarget == null) {
            return;
        }
        if ("ZombieModernAllStar".equals(zombie.getType())) {
            if (!zombie.beginAbility("kick", 16)) {
                return;
            }
        } else if (!zombie.tryBeginAbilityAction()) {
            return;
        }
        if (canHitPlant) {
            target.takeDamage(target.getHealth());
            context.onPlantDestroyed(target);
        } else {
            hypnotizedTarget.takeDirectDamage(hypnotizedTarget.getHealth());
            context.onZombieKilled(hypnotizedTarget);
        }
        used = true;
        if (speedMultiplierAfterHit > 0 && speedMultiplierAfterHit != 1.0) {
            zombie.multiplySpeed(speedMultiplierAfterHit);
        }
        if (selfDestructOnHit) {
            zombie.takeDirectDamage(zombie.getHealth());
            context.onZombieKilled(zombie);
        }
    }

    private Zombie nearestHypnotizedCollision(Zombie zombie, GameContext context) {
        Zombie nearest = null;
        double distance = Double.MAX_VALUE;
        for (Zombie candidate : context.getZombiesInRow(zombie.getRow())) {
            double current = Math.abs(candidate.getX() - zombie.getX());
            if (candidate != zombie && candidate.isAlive() && candidate.isHypnotized()
                    && current <= 0.6 && current < distance) {
                nearest = candidate;
                distance = current;
            }
        }
        return nearest;
    }
}
