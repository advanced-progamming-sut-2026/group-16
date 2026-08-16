package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;

import java.util.List;


public final class ProjectileAttackAbility implements PlantAbility {
    public static final int MUZZLE_TICKS = 4;

    private static final String SPLIT_PEA = "Split Pea";

    private final int projectileCount;
    private final ProjectileProfile profile;
    private int windupRemaining;

    public ProjectileAttackAbility(int projectileCount, ProjectileProfile profile) {
        this.projectileCount = projectileCount;
        this.profile = profile;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        context.spawnProjectile(plant, plant.getStats().damage(), projectileCount, profile);
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (windupRemaining > 0) {
            windupRemaining--;
            if (windupRemaining == 0) {
                onActionReady(plant, context);
                plant.setAttacking(false);
                return true;
            }
            return false;
        }
        if (!hasTarget(plant, context)) {
            return false;
        }
        windupRemaining = MUZZLE_TICKS;
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return MUZZLE_TICKS;
    }

    public ProjectileProfile getProfile() {
        return profile;
    }

    private static boolean hasTarget(Plant plant, GameContext context) {
        if (plant.getCategory() == PlantCategory.HOMING) {
            for (Zombie zombie : context.getAllZombies()) {
                if (isValidTarget(zombie)) {
                    return true;
                }
            }
            return false;
        }
        boolean anyX = SPLIT_PEA.equals(plant.getName());
        List<Zombie> row = context.getZombiesInRow(plant.getRow());
        for (Zombie zombie : row) {
            if (!isValidTarget(zombie)) {
                continue;
            }
            if (anyX || zombie.getX() >= plant.getCol()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidTarget(Zombie zombie) {
        if (zombie == null || !zombie.isAlive()) {
            return false;
        }
        ZombieState state = zombie.getState();
        return state != ZombieState.SPAWNING && state != ZombieState.DYING;
    }
}
