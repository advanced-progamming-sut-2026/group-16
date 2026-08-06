package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.entity.zombie.Zombie;

@FunctionalInterface
public interface ProjectileKillCallback {
    void accept(Zombie zombie, String killerPlantType, String projectileId);
}
