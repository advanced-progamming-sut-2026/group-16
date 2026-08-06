package io.github.finalwave.model.game.entity.zombie;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.projectile.Projectile;

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