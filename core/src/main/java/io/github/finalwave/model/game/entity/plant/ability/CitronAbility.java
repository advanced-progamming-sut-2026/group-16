package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class CitronAbility implements PlantAbility {

    public static final int MUZZLE_TICKS = 4;
    public static final int RECOVERY_TICKS = 14;

    private final ProjectileProfile profile;
    private int windupRemaining;

    public CitronAbility(ProjectileProfile profile) {
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
        if (!ProjectileAttackAbility.hasAhead(plant, context)) {
            return false;
        }
        windupRemaining = MUZZLE_TICKS;
        plant.setAttacking(true);
        return false;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        context.spawnProjectile(plant, plant.getStats().damage(), 1, profile);
        plant.setRecoveryTicksRemaining(RECOVERY_TICKS);
    }

    @Override
    public int actionWindupTicks() {
        return MUZZLE_TICKS;
    }
}
