package model.game.entity.plant.ability;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;

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
