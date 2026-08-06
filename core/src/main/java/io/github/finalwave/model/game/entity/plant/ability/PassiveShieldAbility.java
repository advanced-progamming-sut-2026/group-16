package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class PassiveShieldAbility implements PlantAbility {

    private final int armorValue;

    public PassiveShieldAbility(int armorValue) {
        this.armorValue = armorValue;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        if (armorValue > 0) {
            context.grantArmor(plant, armorValue);
        }
    }
}
