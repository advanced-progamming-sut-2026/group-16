package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.DoomShroomMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public final class DoomShroomAbility implements PlantAbility {

    public enum Phase {
        GROWING,
        PLANT_FOOD_TRANSFORM,
        DETONATING,
        DONE
    }

    private Phase phase = Phase.GROWING;
    private int detonationTicksRemaining;
    private int explosionDamageTicksRemaining;
    private int transformTicksRemaining;
    private boolean proximityAlert;
    private boolean plantFoodActive;
    private boolean explosionDamageApplied;

    public Phase phase() {
        return phase;
    }

    public boolean isDetonating() {
        return phase == Phase.DETONATING;
    }

    public boolean isPlantFoodTransforming() {
        return phase == Phase.PLANT_FOOD_TRANSFORM;
    }

    public boolean isPlantFoodActive() {
        return plantFoodActive;
    }

    public boolean isProximityAlert() {
        return proximityAlert;
    }

    public int growthStage(Plant plant) {
        return plant == null ? 0 : plant.getGrowthStage();
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        phase = Phase.GROWING;
        detonationTicksRemaining = 0;
        explosionDamageTicksRemaining = 0;
        transformTicksRemaining = 0;
        proximityAlert = false;
        plantFoodActive = false;
        explosionDamageApplied = false;
        plant.setGrowthStage(0);
        plant.setGrowthTicksRemaining(DoomShroomMuzzles.stage1ToStage2Ticks());
        plant.setAttacking(false);
    }

    public void startPlantFoodAdvance(Plant plant, GameContext context) {
        if (phase == Phase.DONE || plant.isDead()) {
            return;
        }
        plantFoodActive = true;
        if (plant.getGrowthStage() < 2) {
            plant.setGrowthStage(plant.getGrowthStage() + 1);
            plant.setGrowthTicksRemaining(0);
            phase = Phase.PLANT_FOOD_TRANSFORM;
            transformTicksRemaining = DoomShroomMuzzles.transformTicks();
            plant.setAttacking(false);
            return;
        }
        beginDetonation(plant, context);
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.DONE || plant.isDead()) {
            return;
        }
        if (phase == Phase.PLANT_FOOD_TRANSFORM) {
            tickPlantFoodTransform(plant, context);
            return;
        }
        if (phase == Phase.DETONATING) {
            tickDetonation(plant, context);
            return;
        }
        proximityAlert = hasProximityAlert(plant, context);
        if (!plantFoodActive && hasZombieOnTile(plant, context)) {
            beginDetonation(plant, context);
            tickDetonation(plant, context);
            return;
        }
        tickGrowth(plant);
    }

    private void tickPlantFoodTransform(Plant plant, GameContext context) {
        if (transformTicksRemaining > 0) {
            transformTicksRemaining--;
        }
        if (transformTicksRemaining > 0) {
            return;
        }
        beginDetonation(plant, context);
        tickDetonation(plant, context);
    }

    private void tickGrowth(Plant plant) {
        if (plant.getGrowthStage() >= 2) {
            return;
        }
        if (plant.getGrowthTicksRemaining() > 0) {
            plant.decrementGrowthTicks();
        }
        if (plant.getGrowthTicksRemaining() > 0) {
            return;
        }
        int nextStage = plant.getGrowthStage() + 1;
        plant.setGrowthStage(nextStage);
        if (nextStage < 2) {
            plant.setGrowthTicksRemaining(DoomShroomMuzzles.stage2ToStage3Ticks());
        }
    }

    private void beginDetonation(Plant plant, GameContext context) {
        phase = Phase.DETONATING;
        plant.setAttacking(true);
        detonationTicksRemaining = DoomShroomMuzzles.explodeTicks(plant.getGrowthStage());
        explosionDamageTicksRemaining = DoomShroomMuzzles.explosionDamageDelayTicks();
    }

    private void tickDetonation(Plant plant, GameContext context) {
        if (!explosionDamageApplied) {
            if (explosionDamageTicksRemaining > 0) {
                explosionDamageTicksRemaining--;
            }
            if (explosionDamageTicksRemaining <= 0) {
                applyExplosionDamage(plant, context);
            }
        }
        if (detonationTicksRemaining > 0) {
            detonationTicksRemaining--;
            return;
        }
        finishDetonation(plant, context);
    }

    private void applyExplosionDamage(Plant plant, GameContext context) {
        if (explosionDamageApplied) {
            return;
        }
        explosionDamageApplied = true;
        int stage = plant.getGrowthStage();
        context.explodeSquare(
                plant.getCol(), plant.getRow(),
                DoomShroomMuzzles.stageDamage(stage),
                DoomShroomMuzzles.stageBlastRadius(stage),
                plant);
    }

    private void finishDetonation(Plant plant, GameContext context) {
        if (phase == Phase.DONE) {
            return;
        }
        phase = Phase.DONE;
        int stage = plant.getGrowthStage();
        int col = plant.getCol();
        int row = plant.getRow();
        if (stage >= 1) {
            context.spawnDoomShroomSeedling(col, row, DoomShroomMuzzles.stageBlastRadius(stage), plant);
        }
        context.placeTimedCrater(col, row, DoomShroomMuzzles.CRATER_DURATION_SECONDS);
        plant.consumeInstantly();
    }

    private boolean hasZombieOnTile(Plant plant, GameContext context) {
        for (Zombie zombie : context.getAllZombies()) {
            if (zombie.isDead() || zombie.isHypnotized()) {
                continue;
            }
            if (zombie.getRow() != plant.getRow()) {
                continue;
            }
            if ((int) Math.floor(zombie.getX()) == plant.getCol()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasProximityAlert(Plant plant, GameContext context) {
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        int range = DoomShroomMuzzles.PROXIMITY_ALERT_TILES;
        for (Zombie zombie : context.getAllZombies()) {
            if (zombie.isDead() || zombie.isHypnotized()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            int colDistance = Math.abs(zCol - plantCol);
            int rowDistance = Math.abs(zombie.getRow() - plantRow);
            if (Math.max(colDistance, rowDistance) <= range) {
                return true;
            }
        }
        return false;
    }
}
