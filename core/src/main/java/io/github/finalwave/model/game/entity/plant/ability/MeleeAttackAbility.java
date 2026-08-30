package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class MeleeAttackAbility implements PlantAbility {

    private final boolean areaOfEffect;
    private int windupRemaining;

    public MeleeAttackAbility(boolean areaOfEffect) {
        this.areaOfEffect = areaOfEffect;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        context.dealMeleeDamage(plant, plant.getStats().damage(), areaOfEffect);
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
        windupRemaining = ProjectileAttackAbility.MUZZLE_TICKS;
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return ProjectileAttackAbility.MUZZLE_TICKS;
    }
}
