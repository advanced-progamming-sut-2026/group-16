package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

public final class ContactAttackBehavior implements ZombieBehavior {

    private final boolean oneUse;
    private final double speedMultiplierAfterHit;
    private boolean used;

    public ContactAttackBehavior(boolean oneUse, double speedMultiplierAfterHit) {
        this.oneUse = oneUse;
        this.speedMultiplierAfterHit = speedMultiplierAfterHit;
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
        if (!zombie.tryBeginAbilityAction()) {
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
