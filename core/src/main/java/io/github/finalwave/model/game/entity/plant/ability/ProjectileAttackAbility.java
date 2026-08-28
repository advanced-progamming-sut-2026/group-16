package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;

import java.util.List;


public final class ProjectileAttackAbility implements PlantAbility {
    public static final int MUZZLE_TICKS = 4;

    private static final String SPLIT_PEA = "Split Pea";

    private final int projectileCount;
    private final ProjectileProfile profile;
    private int windupRemaining;
    private int pendingShots;
    private int pendingDelay;
    private boolean pendingForward;
    private boolean pendingBackward;
    private boolean pendingDiagonal;

    public ProjectileAttackAbility(int projectileCount, ProjectileProfile profile) {
        this.projectileCount = projectileCount;
        this.profile = profile;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        if ("Caulipower".equals(plant.getName()) || "Electric Blueberry".equals(plant.getName())) {
            plant.rotateVisualIdleVariant();
        }
        fireVolley(plant, context, true);
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (pendingShots > 0) {
            if (pendingDelay > 0) {
                pendingDelay--;
                if (pendingDelay > 0) {
                    return false;
                }
            }
            firePending(plant, context);
            pendingShots--;
            if (pendingShots > 0) {
                pendingDelay = PlantShotPatterns.VOLLEY_STAGGER_TICKS;
                return false;
            }
            plant.setAttacking(false);
            clearSplitFireVisual(plant);
            return true;
        }
        if (windupRemaining > 0) {
            windupRemaining--;
            if (windupRemaining == 0) {
                onActionReady(plant, context);
                if (pendingShots > 0) {
                    return false;
                }
                plant.setAttacking(false);
                clearSplitFireVisual(plant);
                return true;
            }
            return false;
        }
        if (!hasTarget(plant, context)) {
            return false;
        }
        prepareSplitFireVisual(plant, context);
        windupRemaining = MUZZLE_TICKS;
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return MUZZLE_TICKS;
    }

    public ProjectileProfile getProfile() {
        return profile;
    }

    private void fireVolley(Plant plant, GameContext context, boolean startPending) {
        boolean ahead = hasAhead(plant, context);
        boolean behind = SPLIT_PEA.equals(plant.getName()) && hasBehind(plant, context);
        if (PlantShotPatterns.isRotobaga(plant.getName())) {
            PlantShotPatterns.fireRotobaga(plant, context);
            pendingShots = 0;
            return;
        }
        if (PlantShotPatterns.isStarfruit(plant.getName())) {
            PlantShotPatterns.fireStarfruit(plant, context, profile);
            pendingShots = 0;
            return;
        }
        if (PlantShotPatterns.isRepeater(plant.getName())) {
            PlantShotPatterns.fireForward(plant, context, profile);
            if (startPending) {
                pendingShots = 1;
                pendingDelay = PlantShotPatterns.VOLLEY_STAGGER_TICKS;
                pendingForward = true;
                pendingBackward = false;
                pendingDiagonal = false;
            }
            return;
        }
        if ("Mega Gatling Pea".equals(plant.getName())) {
            int shots = plant.isMegaGatlingBoosted() ? 5 : 4;
            if (ahead) {
                PlantShotPatterns.fireForward(plant, context, profile);
                if (startPending && shots > 1) {
                    pendingShots = shots - 1;
                    pendingDelay = PlantShotPatterns.MEGA_GATLING_VOLLEY_STAGGER_TICKS;
                    pendingForward = true;
                    pendingBackward = false;
                    pendingDiagonal = false;
                }
            }
            if (behind) {
                PlantShotPatterns.fireReverse(plant, context, profile);
            }
            return;
        }
        int shots = Plant.isPeaPod(plant.getName()) ? plant.getStackCount() : projectileCount;
        if (ahead) {
            context.spawnProjectile(plant, plant.getStats().damage(), shots, profile);
        }
        if (behind) {
            PlantShotPatterns.fireReverse(plant, context, profile);
        }
    }

    private void firePending(Plant plant, GameContext context) {
        if (pendingDiagonal) {
            PlantShotPatterns.fireRotobaga(plant, context);
            return;
        }
        if (pendingForward) {
            PlantShotPatterns.fireForward(plant, context, profile);
        }
        if (pendingBackward) {
            PlantShotPatterns.fireReverse(plant, context, profile);
        }
    }

    private static void prepareSplitFireVisual(Plant plant, GameContext context) {
        if (!SPLIT_PEA.equals(plant.getName())) {
            return;
        }
        boolean ahead = hasAhead(plant, context);
        boolean behind = hasBehind(plant, context);
        if (ahead && behind) {
            plant.setSplitFireVisual(Plant.SplitFireVisual.BOTH);
        } else if (behind) {
            plant.setSplitFireVisual(Plant.SplitFireVisual.BACKWARD);
        } else {
            plant.setSplitFireVisual(Plant.SplitFireVisual.FORWARD);
        }
    }

    private static void clearSplitFireVisual(Plant plant) {
        if (SPLIT_PEA.equals(plant.getName())) {
            plant.setSplitFireVisual(Plant.SplitFireVisual.NONE);
        }
    }

    private static boolean hasTarget(Plant plant, GameContext context) {
        if (PlantShotPatterns.isRotobaga(plant.getName())) {
            return PlantShotPatterns.hasDiagonalTarget(plant, context);
        }
        if (PlantShotPatterns.isStarfruit(plant.getName())) {
            return PlantShotPatterns.hasStarfruitTarget(plant, context);
        }
        if (plant.getCategory() == PlantCategory.HOMING) {
            return hasHomingTarget(context);
        }
        return hasAhead(plant, context) || (SPLIT_PEA.equals(plant.getName()) && hasBehind(plant, context));
    }

    public static boolean hasAhead(Plant plant, GameContext context) {
        List<Zombie> row = context.getZombiesInRow(plant.getRow());
        for (Zombie zombie : row) {
            if (isAttackableTarget(zombie) && zombie.getX() >= plant.getCol()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBehind(Plant plant, GameContext context) {
        List<Zombie> row = context.getZombiesInRow(plant.getRow());
        for (Zombie zombie : row) {
            if (isAttackableTarget(zombie) && zombie.getX() < plant.getCol()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasHomingTarget(GameContext context) {
        for (Zombie zombie : context.getAllZombies()) {
            if (isAttackableTarget(zombie)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAttackableTarget(Zombie zombie) {
        if (!isLivingTarget(zombie)) {
            return false;
        }
        return !zombie.isHypnotized();
    }

    public static boolean isLivingTarget(Zombie zombie) {
        if (zombie == null || !zombie.isAlive()) {
            return false;
        }
        ZombieState state = zombie.getState();
        return state != ZombieState.SPAWNING && state != ZombieState.DYING;
    }
}
