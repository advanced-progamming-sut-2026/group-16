package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.projectile.TangleKelpMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;


public final class TangleKelpAbility implements PlantAbility {

    public enum Phase {
        IDLE,
        SUBMERGE,
        ATTACK,
        EMERGE,
        PLANT_FOOD_ON,
        PLANT_FOOD,
        PLANT_FOOD_OFF
    }

    private Phase phase = Phase.IDLE;
    private int phaseTicksRemaining;
    private String targetZombieId;
    private boolean plantFoodActive;

    public Phase phase() {
        return phase;
    }

    public boolean isGrabbing() {
        return phase == Phase.SUBMERGE || phase == Phase.ATTACK || phase == Phase.EMERGE;
    }

    public boolean isPlantFoodActive() {
        return plantFoodActive;
    }

    public int phaseTicksRemaining() {
        return phaseTicksRemaining;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        resetDragState(plant, context);
    }

    public void startGrab(Plant plant, Zombie zombie, GameContext context) {
        if (phase != Phase.IDLE || plant.isDead() || zombie == null || zombie.isDead()) {
            return;
        }
        plant.setArmedTrap(false);
        plant.setAttacking(true);
        targetZombieId = zombie.getId();
        if (!isGargantuar(zombie)) {
            zombie.setSubmerged(true);
        }
        enterPhase(Phase.SUBMERGE);
    }

    public void startPlantFood(Plant plant, GameContext context) {
        resetDragState(plant, context);
        plantFoodActive = true;
        plant.setAttacking(true);
        pullRandomZombies(plant, context);
        enterPhase(Phase.PLANT_FOOD_ON);
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.IDLE || plant.isDead()) {
            return;
        }
        if (phaseTicksRemaining > 0) {
            phaseTicksRemaining--;
            return;
        }
        switch (phase) {
            case SUBMERGE -> {
                enterPhase(Phase.ATTACK);
                resolveGrabTarget(plant, context);
            }
            case ATTACK -> enterPhase(Phase.EMERGE);
            case EMERGE -> finishGrab(plant, context);
            case PLANT_FOOD_ON -> enterPhase(Phase.PLANT_FOOD);
            case PLANT_FOOD -> enterPhase(Phase.PLANT_FOOD_OFF);
            case PLANT_FOOD_OFF -> finishPlantFood(plant, context);
            default -> {
            }
        }
    }

    public void resetDragState(Plant plant, GameContext context) {
        phase = Phase.IDLE;
        phaseTicksRemaining = 0;
        targetZombieId = null;
        plantFoodActive = false;
        plant.setAttacking(false);
        if (plant.isAlive()) {
            context.armTrap(plant);
        }
    }

    public static boolean isGargantuar(Zombie zombie) {
        if (zombie == null) {
            return false;
        }
        String type = zombie.getType();
        return type != null && type.contains("Gargantuar");
    }

    private void enterPhase(Phase next) {
        phase = next;
        phaseTicksRemaining = TangleKelpMuzzles.phaseTicks(next);
    }

    private void finishGrab(Plant plant, GameContext context) {
        phase = Phase.IDLE;
        phaseTicksRemaining = 0;
        targetZombieId = null;
        plant.setAttacking(false);
        context.armTrap(plant);
    }

    private void finishPlantFood(Plant plant, GameContext context) {
        plantFoodActive = false;
        phase = Phase.IDLE;
        phaseTicksRemaining = 0;
        plant.setAttacking(false);
        context.armTrap(plant);
    }

    private void resolveGrabTarget(Plant plant, GameContext context) {
        Zombie zombie = findTarget(context);
        if (zombie == null) {
            return;
        }
        applyPull(plant, zombie, context);
    }

    private void pullRandomZombies(Plant plant, GameContext context) {
        int targetCount = (int) Math.max(1, plant.getDefinition().getPlantFoodValue())
                + (int) plant.getStats().specialModifier(PlantSpecialModifiers.BONUS_GRAB_TARGETS);
        List<Zombie> candidates = new ArrayList<>();
        for (Zombie zombie : context.getAllZombies()) {
            if (zombie.isDead() || zombie.isHypnotized()) {
                continue;
            }
            candidates.add(zombie);
        }
        context.shuffle(candidates);
        int pulled = 0;
        for (Zombie zombie : candidates) {
            if (pulled >= targetCount) {
                break;
            }
            applyPull(plant, zombie, context);
            int col = (int) Math.floor(zombie.getX());
            context.enqueueTangleKelpGrabMark(col, zombie.getRow());
            pulled++;
        }
    }

    private void applyPull(Plant plant, Zombie zombie, GameContext context) {
        if (isGargantuar(zombie)) {
            zombie.takeDamage(plant.getStats().damage());
        } else {
            zombie.setSubmerged(true);
            zombie.takeDirectDamage(zombie.getMaxHealth());
        }
        if (zombie.isDead()) {
            context.onZombieKilled(zombie);
        }
    }

    private Zombie findTarget(GameContext context) {
        if (targetZombieId == null) {
            return null;
        }
        for (Zombie zombie : context.getAllZombies()) {
            if (targetZombieId.equals(zombie.getId())) {
                return zombie;
            }
        }
        return null;
    }
}
