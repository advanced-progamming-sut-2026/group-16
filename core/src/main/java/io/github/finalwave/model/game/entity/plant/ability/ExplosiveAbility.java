package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class ExplosiveAbility implements PlantAbility {

    private static final String HOT_POTATO = "Hot Potato";

    private final double radius;
    private final boolean triggersOnPlant;
    private boolean armed;
    private boolean detonated;
    private boolean delayedDetonation;

    public ExplosiveAbility(double radius, boolean triggersOnPlant) {
        this.radius = radius;
        this.triggersOnPlant = triggersOnPlant;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        if (triggersOnPlant) {
            if (HOT_POTATO.equals(plant.getName())) {
                plant.setAttacking(true);
                delayedDetonation = true;
                plant.consumeAfter(Math.max(1, context.getTicksPerSecond() / 2));
                return;
            }
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

    @Override
    public void onConsumeDelayFinished(Plant plant, GameContext context) {
        if (delayedDetonation) {
            detonate(plant, context);
            return;
        }
        plant.consumeInstantly();
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
