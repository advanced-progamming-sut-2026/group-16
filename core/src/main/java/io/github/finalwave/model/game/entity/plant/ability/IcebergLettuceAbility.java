package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.projectile.IcebergLettuceMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;


public final class IcebergLettuceAbility implements PlantAbility {

    public enum Phase {
        IDLE,
        ATTACK,
        PLANT_FOOD
    }

    private Phase phase = Phase.IDLE;
    private int phaseTicksRemaining;
    private boolean plantFoodActive;

    public Phase phase() {
        return phase;
    }

    public boolean isFreezing() {
        return phase == Phase.ATTACK;
    }

    public boolean isPlantFoodActive() {
        return plantFoodActive;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        phase = Phase.IDLE;
        phaseTicksRemaining = 0;
        plantFoodActive = false;
        context.armTrap(plant);
    }

    public void startTrapFreeze(Plant plant, Zombie zombie, GameContext context) {
        if (phase != Phase.IDLE || plant.isDead() || zombie == null || zombie.isDead()) {
            return;
        }
        if (isAirborne(zombie, plant)) {
            return;
        }
        plant.setArmedTrap(false);
        plant.setAttacking(true);
        applyCold(plant, zombie, context, IcebergLettuceMuzzles.trapFreezeTicks());
        enterPhase(Phase.ATTACK);
    }

    public void startPlantFood(Plant plant, GameContext context) {
        if (phase != Phase.IDLE || plant.isDead()) {
            return;
        }
        plantFoodActive = true;
        plant.setAttacking(true);
        enterPhase(Phase.PLANT_FOOD);
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.IDLE || plant.isDead()) {
            return;
        }
        if (phaseTicksRemaining > 0) {
            phaseTicksRemaining--;
            return;
        }
        if (phase == Phase.ATTACK) {
            finishTrap(plant);
            return;
        }
        if (phase == Phase.PLANT_FOOD) {
            finishPlantFood(plant, context);
        }
    }

    public static boolean isAirborne(Zombie zombie, Plant plant) {
        if (zombie == null) {
            return true;
        }
        if (zombie.isTrapImmune()) {
            return true;
        }
        if (plant != null && zombie.shouldBypass(plant)) {
            return true;
        }
        String type = zombie.getType();
        if (type == null) {
            return false;
        }
        return type.contains("Balloon")
                || type.contains("Seagull")
                || type.contains("Bug")
                || type.contains("Dodo");
    }

    public static boolean shouldChillInsteadOfFreeze(Zombie zombie, GameContext context) {
        if (context != null && context.areZombiesImmuneToChill()) {
            return true;
        }
        String type = zombie == null ? null : zombie.getType();
        return type != null && type.contains("IceAge");
    }

    private void enterPhase(Phase next) {
        phase = next;
        phaseTicksRemaining = IcebergLettuceMuzzles.phaseTicks(next);
    }

    private void finishTrap(Plant plant) {
        phase = Phase.IDLE;
        phaseTicksRemaining = 0;
        plant.setAttacking(false);
        plant.consumeInstantly();
    }

    private void finishPlantFood(Plant plant, GameContext context) {
        double extension = plant.getStats()
                .specialModifier(PlantSpecialModifiers.FREEZE_DURATION_EXT);
        context.freezeGroundedZombiesForIceberg(
                plant,
                plant.getDefinition().getPlantFoodValue(),
                extension);
        context.enqueueIcebergFlash();
        plantFoodActive = false;
        phase = Phase.IDLE;
        phaseTicksRemaining = 0;
        plant.setAttacking(false);
        context.armTrap(plant);
    }

    private static void applyCold(Plant plant, Zombie zombie, GameContext context, int ticks) {
        if (shouldChillInsteadOfFreeze(zombie, context)) {
            zombie.applyChill(ticks);
        } else {
            zombie.applyFreeze(ticks);
        }
    }
}
