package model.game.entity.zombie.behavior;

import model.game.entity.*;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantCategory;
import model.game.entity.plant.PlantTag;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;
import model.game.entity.zombie.ZombieState;

public final class MovementBehavior implements ZombieBehavior {

    private double eatDamageAccumulator = 0.0;

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (zombie.getState() == ZombieState.DYING) {
            return;
        }

        Plant target = context.getPlantInFront(zombie.getX(), zombie.getRow());

        if (target != null && target.isAlive()) {
            eatPlant(zombie, target, context);
        } else {
            walkLeft(zombie, context);
        }
    }

    private void eatPlant(Zombie zombie, Plant target, GameContext context) {
        if (target.hasTag(PlantTag.MAGIC)
                && target.getCategory() == PlantCategory.MODIFIER
                && target.getStats().actionInterval() <= 0) {
            double healthMultiplier = Math.max(1.0, target.getStats()
                    .specialModifier("ZOMBIE_HEALTH_MULTIPLIER"));
            double damageMultiplier = Math.max(1.0, target.getStats()
                    .specialModifier("ZOMBIE_DAMAGE_MULTIPLIER"));
            zombie.hypnotize(healthMultiplier, damageMultiplier);
            target.takeDamage(target.getMaxHealth());
            return;
        }
        zombie.setState(ZombieState.EATING);

        double dps = zombie.getDamage();
        eatDamageAccumulator += dps / context.getTicksPerSecond();

        if (eatDamageAccumulator >= 1.0) {
            int intDamage = (int) eatDamageAccumulator;
            zombie.attackPlant(target, intDamage);
            eatDamageAccumulator -= intDamage;
        }

        if (target.isDead()) {
            context.onPlantDestroyed(target);
        }
    }

    private void walkLeft(Zombie zombie, GameContext context) {
        zombie.setState(ZombieState.MOVING);

        double stepPerTick = zombie.getCurrentSpeed() / context.getTicksPerSecond();
        zombie.moveLeft(stepPerTick);

        if (zombie.getX() <= 0.0) {
            context.onZombieReachedHouse(zombie);
        }
    }
}