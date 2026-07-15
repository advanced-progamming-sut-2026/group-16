package model.game.entity.plant.ability;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;

public final class ExplosiveAbility implements PlantAbility {

    private final double radius;
    private final boolean triggersOnPlant;
    private boolean armed;
    private boolean detonated;

    public ExplosiveAbility(double radius, boolean triggersOnPlant) {
        this.radius = radius;
        this.triggersOnPlant = triggersOnPlant;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        if (triggersOnPlant) {
            detonate(plant, context);
        } else if (plant.getStats().actionInterval() <= 0) {
            armed = true;
            context.armTrap(plant);
        }
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        if (!triggersOnPlant && !armed) {
            armed = true;
            context.armTrap(plant);
        }
    }

    public boolean isArmed() {
        return armed;
    }

    public void detonate(Plant plant, GameContext context) {
        if (detonated) {
            return;
        }
        detonated = true;
        context.explode(plant, plant.getStats().damage(), radius);
        plant.consumeInstantly();
    }
}
