package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class MeleeAttackAbility implements PlantAbility {

    private final boolean areaOfEffect;

    public MeleeAttackAbility(boolean areaOfEffect) {
        this.areaOfEffect = areaOfEffect;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        context.dealMeleeDamage(plant, plant.getStats().damage(), areaOfEffect);
    }
}
