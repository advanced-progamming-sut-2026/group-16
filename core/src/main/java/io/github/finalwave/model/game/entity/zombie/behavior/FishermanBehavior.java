package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class FishermanBehavior implements ZombieBehavior {

    private final int cooldownTicks;
    private final double minimumRange;
    private final double maximumRange;
    private int cooldown;

    public FishermanBehavior(int cooldownTicks, double minimumRange, double maximumRange) {
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.minimumRange = minimumRange;
        this.maximumRange = maximumRange;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        zombie.setStationary(true);
        zombie.setPosition(Math.max(0, context.getColCount() - 1), zombie.getRow());
        if (cooldown-- > 0) {
            return;
        }
        Plant target = findTarget(zombie, context);
        if (target == null || !zombie.tryBeginAbilityAction()) {
            cooldown = cooldownTicks;
            return;
        }
        if (zombie.getX() - target.getCol() <= 1.5) {
            target.takeDamage(target.getHealth());
            context.onPlantDestroyed(target);
        } else {
            context.movePlant(target, target.getCol() + 1, target.getRow());
        }
        cooldown = cooldownTicks;
    }

    private Plant findTarget(Zombie zombie, GameContext context) {
        Plant nearest = null;
        int greatestColumn = -1;
        for (Plant plant : context.getAllPlants()) {
            double distance = zombie.getX() - plant.getCol();
            if (plant.canBeTargetedByZombie()
                    && plant.getRow() == zombie.getRow() && distance >= minimumRange
                    && distance <= maximumRange && plant.getCol() > greatestColumn) {
                nearest = plant;
                greatestColumn = plant.getCol();
            }
        }
        return nearest;
    }
}
