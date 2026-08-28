package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public final class CactusAbility implements PlantAbility {

    public static final int MUZZLE_TICKS = 4;

    private final int projectileCount;
    private final ProjectileProfile profile;
    private int windupRemaining;

    public CactusAbility(int projectileCount, ProjectileProfile profile) {
        this.projectileCount = projectileCount;
        this.profile = profile;
    }

    public ProjectileProfile getProfile() {
        return profile;
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
    public void onActionReady(Plant plant, GameContext context) {
        if (hasZombieOnTile(plant, context)) {
            context.dealMeleeDamage(plant, plant.getStats().damage(), false);
            return;
        }
        context.spawnProjectile(plant, plant.getStats().damage(), projectileCount, profile);
    }

    @Override
    public int actionWindupTicks() {
        return MUZZLE_TICKS;
    }

    static boolean hasZombieOnTile(Plant plant, GameContext context) {
        int col = plant.getCol();
        int row = plant.getRow();
        for (Zombie zombie : context.getAllZombies()) {
            if (!ProjectileAttackAbility.isLivingTarget(zombie) || !zombie.occupiesRow(row)) {
                continue;
            }
            if ((int) Math.floor(zombie.getX()) == col) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTarget(Plant plant, GameContext context) {
        return ProjectileAttackAbility.hasAhead(plant, context) || hasZombieOnTile(plant, context);
    }
}
