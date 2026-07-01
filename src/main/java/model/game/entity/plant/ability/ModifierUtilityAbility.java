package model.game.entity.plant.ability;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;

public final class ModifierUtilityAbility implements PlantAbility {

    private final double magnitude;

    public ModifierUtilityAbility(double magnitude) {
        this.magnitude = magnitude;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        context.applyFieldModifier(plant, magnitude);
    }
}
