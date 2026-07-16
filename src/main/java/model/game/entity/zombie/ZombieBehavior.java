package model.game.entity.zombie;

import model.game.entity.GameContext;
import model.game.entity.projectile.Projectile;

public interface ZombieBehavior {

    void execute(Zombie zombie, GameContext context);

    default void onDeath(Zombie zombie, GameContext context) {
    }

    default boolean interceptProjectile(Zombie zombie, Projectile projectile, GameContext context) {
        return false;
    }

    default boolean isMovementBehavior() {
        return false;
    }
}