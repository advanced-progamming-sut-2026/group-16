package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.concurrent.ThreadLocalRandom;

public final class PlantShotPatterns {

    public static final double TILE_SPEED = 0.3;
    public static final double PLANT_FOOD_PEA_SPEED = 0.5;
    public static final int VOLLEY_STAGGER_TICKS = 3;
    public static final int MEGA_GATLING_VOLLEY_STAGGER_TICKS = 1;
    public static final int RAPID_FIRE_INTERVAL_TICKS = 1;
    public static final int RAPID_FIRE_DURATION_TICKS = 60;
    public static final int ROTOBAGA_PLANT_FOOD_TICKS = 32;
    public static final int GIANT_PEA_PHASE_TICKS = 24;
    public static final int GIANT_PEA_FIRE_DELAY_TICKS = 17;
    public static final int GIANT_PEA_DAMAGE_MULTIPLIER = 20;
    public static final float GIANT_PEA_SCALE = 1.2f;
    public static final float GIANT_STAR_SCALE = 1.35f;
    public static final int GIANT_STAR_DAMAGE_MULTIPLIER = 4;

    private PlantShotPatterns() {
    }

    public static boolean isRotobaga(String name) {
        return "Rotobaga".equals(name);
    }

    public static boolean isRepeater(String name) {
        return "Repeater".equals(name);
    }

    public static boolean isStarfruit(String name) {
        return "Starfruit".equals(name);
    }

    public static void fireForward(Plant plant, GameContext context, ProjectileProfile profile) {
        context.spawnProjectile(plant, plant.getStats().damage(), 1, profile);
    }

    public static void fireReverse(Plant plant, GameContext context, ProjectileProfile profile) {
        context.spawnReverseProjectile(plant, plant.getStats().damage(), 1, profile);
    }

    public static void fireScatteredPeas(Plant plant, GameContext context) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = 2 + random.nextInt(2);
        int damage = plant.getStats().damage();
        for (int i = 0; i < count; i++) {
            double laneOffset = (random.nextDouble() - 0.5) * 0.9;
            double extraX = i * 0.18 + random.nextDouble() * 0.1;
            context.spawnDirectedProjectile(plant, damage, PLANT_FOOD_PEA_SPEED, 0, 1f, laneOffset, extraX);
        }
    }

    public static void fireRotobaga(Plant plant, GameContext context) {
        int damage = plant.getStats().damage();
        double speed = TILE_SPEED;
        context.spawnDirectedProjectile(plant, damage, speed, -speed, 1f);
        context.spawnDirectedProjectile(plant, damage, speed, speed, 1f);
        context.spawnDirectedProjectile(plant, damage, -speed, -speed, 1f);
        context.spawnDirectedProjectile(plant, damage, -speed, speed, 1f);
    }

    public static void fireGiantPea(Plant plant, GameContext context) {
        int damage = plant.getStats().damage() * GIANT_PEA_DAMAGE_MULTIPLIER;
        context.spawnDirectedProjectile(plant, damage, TILE_SPEED, 0, GIANT_PEA_SCALE);
    }

    public static void fireStarfruit(Plant plant, GameContext context, ProjectileProfile profile) {
        double speed = TILE_SPEED;
        context.spawnDirectedProjectile(
                plant, plant.getStats().damage(), speed, 0, 1f, 0, 0, ProjectileEffect.STAR);
        context.spawnDirectedProjectile(
                plant, plant.getStats().damage(), speed, -speed, 1f, 0, 0, ProjectileEffect.STAR);
        context.spawnDirectedProjectile(
                plant, plant.getStats().damage(), speed, speed, 1f, 0, 0, ProjectileEffect.STAR);
        context.spawnDirectedProjectile(
                plant, plant.getStats().damage(), 0, -speed, 1f, 0, 0, ProjectileEffect.STAR);
        context.spawnDirectedProjectile(
                plant, plant.getStats().damage(), 0, speed, 1f, 0, 0, ProjectileEffect.STAR);
    }

    public static void fireStarfruitGiant(Plant plant, GameContext context, int rotation) {
        int damage = plant.getStats().damage() * GIANT_STAR_DAMAGE_MULTIPLIER;
        double speed = PLANT_FOOD_PEA_SPEED;
        int[][] directions = starfruitDirections(rotation % 5);
        for (int[] direction : directions) {
            context.spawnDirectedProjectile(
                    plant,
                    damage,
                    speed * direction[0],
                    speed * direction[1],
                    GIANT_STAR_SCALE,
                    0,
                    0,
                    ProjectileEffect.STAR_PF);
        }
    }

    private static int[][] starfruitDirections(int rotation) {
        int[][] base = {
                {1, 0},
                {1, -1},
                {1, 1},
                {0, -1},
                {0, 1}
        };
        int[][] rotated = new int[5][2];
        for (int i = 0; i < 5; i++) {
            int index = (i + rotation) % 5;
            rotated[i] = base[index];
        }
        return rotated;
    }

    public static boolean hasStarfruitTarget(Plant plant, GameContext context) {
        int plantRow = plant.getRow();
        int plantCol = plant.getCol();
        for (Zombie zombie : context.getAllZombies()) {
            if (!ProjectileAttackAbility.isAttackableTarget(zombie)) {
                continue;
            }
            int zombieCol = (int) Math.floor(zombie.getX());
            for (int zombieRow : zombie.occupiedRows()) {
                if (isStarfruitReachable(plantCol, plantRow, zombieCol, zombieRow)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isStarfruitReachable(int plantCol, int plantRow, int zombieCol, int zombieRow) {
        int colDiff = zombieCol - plantCol;
        int rowDiff = zombieRow - plantRow;
        if (colDiff == 0 && rowDiff == 0) {
            return true;
        }
        if (colDiff > 0 && rowDiff == 0) {
            return true;
        }
        if (colDiff > 0 && Math.abs(rowDiff) == colDiff) {
            return true;
        }
        if (colDiff == 0 && rowDiff != 0) {
            return true;
        }
        return false;
    }

    public static boolean hasDiagonalTarget(Plant plant, GameContext context) {
        int plantRow = plant.getRow();
        int plantCol = plant.getCol();
        for (Zombie zombie : context.getAllZombies()) {
            if (!ProjectileAttackAbility.isLivingTarget(zombie)) {
                continue;
            }
            int zombieCol = (int) Math.round(zombie.getX());
            for (int zombieRow : zombie.occupiedRows()) {
                int rowDiff = Math.abs(zombieRow - plantRow);
                int colDiff = Math.abs(zombieCol - plantCol);
                if (rowDiff > 0 && rowDiff == colDiff) {
                    return true;
                }
            }
        }
        return false;
    }
}
