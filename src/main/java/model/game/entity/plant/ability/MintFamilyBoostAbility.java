package model.game.entity.plant.ability;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantCategory;

public final class MintFamilyBoostAbility implements PlantAbility {

    private final PlantCategory boostedFamily;
    private final double duration;

    public MintFamilyBoostAbility(PlantCategory boostedFamily, double duration) {
        this.boostedFamily = boostedFamily;
        this.duration = duration;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        double extendedDuration = duration + plant.getStats().specialModifier("DURATION_EXT");
        context.boostFamily(plant, boostedFamily, extendedDuration);
        if (plant.getStats().hasSpecialModifier("RESET_FAMILY_COOLDOWNS")) {
            context.resetFamilyCooldowns(boostedFamily);
        }
        plant.consumeInstantly();
    }
}
