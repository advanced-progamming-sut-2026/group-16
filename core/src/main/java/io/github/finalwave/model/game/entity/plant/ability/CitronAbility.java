package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;


public final class CitronAbility implements PlantAbility {

    public static final int ATTACK_TICKS = 13;
    public static final int FIRE_AT_TICK = 7;
    public static final int MUZZLE_TICKS = FIRE_AT_TICK;
    public static final int RECOVERY_TICKS = 14;
    public static final double FIRE_PHASE_SECONDS = 2.5;

    private final ProjectileProfile profile;
    private int attackTicksRemaining;
    private boolean firedThisAttack;

    public CitronAbility(ProjectileProfile profile) {
        this.profile = profile;
    }

    public ProjectileProfile getProfile() {
        return profile;
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (attackTicksRemaining > 0) {
            attackTicksRemaining--;
            int elapsed = ATTACK_TICKS - attackTicksRemaining;
            if (!firedThisAttack && elapsed >= FIRE_AT_TICK) {
                context.spawnProjectile(plant, plant.getStats().damage(), 1, profile);
                firedThisAttack = true;
            }
            if (attackTicksRemaining == 0) {
                plant.setAttacking(false);
                plant.setRecoveryTicksRemaining(RECOVERY_TICKS);
                return true;
            }
            return false;
        }
        if (!ProjectileAttackAbility.hasAhead(plant, context)) {
            return false;
        }
        attackTicksRemaining = ATTACK_TICKS;
        firedThisAttack = false;
        plant.setAttacking(true);
        return false;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        if (!firedThisAttack) {
            context.spawnProjectile(plant, plant.getStats().damage(), 1, profile);
            firedThisAttack = true;
        }
        plant.setRecoveryTicksRemaining(RECOVERY_TICKS);
    }

    @Override
    public int actionWindupTicks() {
        return FIRE_AT_TICK;
    }
}
