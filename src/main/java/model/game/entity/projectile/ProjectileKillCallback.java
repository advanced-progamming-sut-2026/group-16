package model.game.entity.projectile;

import model.game.entity.zombie.Zombie;

@FunctionalInterface
public interface ProjectileKillCallback {
    void accept(Zombie zombie, String killerPlantType, String projectileId);
}
