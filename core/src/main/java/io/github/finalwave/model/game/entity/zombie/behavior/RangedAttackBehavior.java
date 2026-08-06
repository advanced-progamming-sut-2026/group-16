package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.*;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;
import io.github.finalwave.model.game.entity.zombie.ZombieState;

public final class RangedAttackBehavior implements ZombieBehavior {

    private final int damage;
    private final double rangeInColumns;
    private final int cooldownTicks;
    private final String projectileType;
    private int ticksUntilNextShot;

    public RangedAttackBehavior(int damage, int cooldownTicks,
                                String projectileType, double rangeInColumns) {
        this.damage = damage;
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.projectileType = projectileType;
        this.rangeInColumns = rangeInColumns;
        this.ticksUntilNextShot = this.cooldownTicks;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (zombie.getState() == ZombieState.DYING) return;

        if (ticksUntilNextShot > 0) {
            ticksUntilNextShot--;
            return;
        }

        if (hasTargetInRange(zombie, context)) {
            if (!zombie.tryBeginAbilityAction()) {
                return;
            }
            context.spawnProjectile(
                    zombie,
                    zombie.getRow(),
                    zombie.getX() - 0.5,
                    damage,
                    projectileType
            );
        }

        ticksUntilNextShot = cooldownTicks;
    }

    private boolean hasTargetInRange(Zombie zombie, GameContext context) {
        int row = zombie.getRow();
        int startCol = (int) Math.floor(zombie.getX());
        int endCol = Math.max(0, (int) (startCol - rangeInColumns));

        for (int col = startCol; col >= endCol; col--) {
            Plant p = context.getPlantAt(col, row);
            if (p != null && p.canBeTargetedByZombie()) {
                return true;
            }
        }
        return false;
    }
}