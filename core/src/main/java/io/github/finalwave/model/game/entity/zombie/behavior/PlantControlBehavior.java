package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class PlantControlBehavior implements ZombieBehavior {

    public enum Mode {HUNTER_ICE, OCTOPUS, WIZARD}

    private final Mode mode;
    private final int cooldownTicks;
    private final double range;
    private int cooldown;

    public PlantControlBehavior(Mode mode, int cooldownTicks, double range) {
        this.mode = mode;
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.range = range;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        Plant target = nearestTarget(zombie, context);
        if (target == null) {
            return;
        }
        String clip = switch (mode) {
            case HUNTER_ICE -> "throw";
            case OCTOPUS -> "toss";
            case WIZARD -> "sheep";
        };
        if (!zombie.beginAbility(clip, mode == Mode.OCTOPUS ? 31 : 12)) {
            return;
        }
        if (mode == Mode.HUNTER_ICE) {
            context.spawnProjectile(zombie, zombie.getRow(), zombie.getX() - 0.5,
                    0, "snowball");
        } else if (mode == Mode.OCTOPUS) {
            context.coverPlant(target, PlantCovering.Type.OCTOPUS, 300, zombie);
        } else {
            target.transformIntoCat(zombie.getId());
        }
        cooldown = cooldownTicks;
    }

    @Override
    public void onDeath(Zombie zombie, GameContext context) {
        if (mode != Mode.WIZARD) {
            return;
        }
        for (Plant plant : context.getAllPlants()) {
            plant.restoreFromCat(zombie.getId());
        }
    }

    private Plant nearestTarget(Zombie zombie, GameContext context) {
        Plant nearest = null;
        double distance = Double.MAX_VALUE;
        for (Plant plant : context.getAllPlants()) {
            if (plant.getRow() != zombie.getRow() || !plant.canBeTargetedByZombie()) {
                continue;
            }
            double current = zombie.getX() - plant.getCol();
            if (current >= 0 && current <= range && current < distance
                    && (mode != Mode.OCTOPUS || context.getPlantCoverings().stream()
                    .noneMatch(covering -> covering.isAlive()
                            && covering.getCoveredPlant() == plant
                            && covering.getType() == PlantCovering.Type.OCTOPUS))) {
                nearest = plant;
                distance = current;
            }
        }
        return nearest;
    }
}
