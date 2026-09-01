package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.projectile.FumeMuzzles;
import io.github.finalwave.model.game.entity.projectile.KernelMuzzles;
import io.github.finalwave.model.game.entity.projectile.MelonMuzzles;
import io.github.finalwave.model.game.entity.projectile.PepperMuzzles;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;

import java.util.List;


public final class ProjectileAttackAbility implements PlantAbility {
    public static final int MUZZLE_TICKS = 4;
    public static final int PEA_POD_MUZZLE_TICKS = 7;
    public static final int PEA_POD_STAGGER_TICKS = 2;
    public static final int FUME_MUZZLE_TICKS = 8;

    private static final String SPLIT_PEA = "Split Pea";
    private static final String THREEPEATER = "Threepeater";
    private static final int[] THREEPEATER_LANE_OFFSETS = {-1, 0, 1};

    private final int projectileCount;
    private final ProjectileProfile profile;
    private int windupRemaining;
    private int pendingShots;
    private int pendingDelay;
    private boolean pendingForward;
    private boolean pendingBackward;
    private boolean pendingDiagonal;
    private int staggerRemaining;
    private int nextMuzzleIndex;
    private int volleyHeads;

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
                if (plant.isPeaPod()) {
                    return firePeaPodHead(plant, context, true);
                }
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
        if (nextMuzzleIndex > 0) {
            staggerRemaining--;
            if (staggerRemaining > 0) {
                return false;
            }
            return firePeaPodHead(plant, context, false);
        }
        if (!hasTarget(plant, context)) {
            return false;
        }
        prepareSplitFireVisual(plant, context);
        windupRemaining = muzzleTicks(plant);
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

    private static int muzzleTicks(Plant plant) {
        if (plant.isPeaPod()) {
            return PEA_POD_MUZZLE_TICKS;
        }
        if (plant.isFumeShroom()) {
            return FUME_MUZZLE_TICKS;
        }
        if (plant.isKernelPult()) {
            return KernelMuzzles.ATTACK_WINDUP_TICKS;
        }
        if (plant.isMelonPult() || plant.isWinterMelon()) {
            return MelonMuzzles.ATTACK_WINDUP_TICKS;
        }
        if (plant.isPepperPult()) {
            return PepperMuzzles.ATTACK_WINDUP_TICKS;
        }
        return MUZZLE_TICKS;
    }

    private boolean firePeaPodHead(Plant plant, GameContext context, boolean firstOfVolley) {
        if (firstOfVolley) {
            volleyHeads = Math.max(1, plant.getStackCount());
            nextMuzzleIndex = 0;
        }
        do {
            context.spawnProjectile(plant, plant.getStats().damage(), profile, nextMuzzleIndex);
            int firedIndex = nextMuzzleIndex;
            nextMuzzleIndex++;
            if (nextMuzzleIndex >= volleyHeads) {
                nextMuzzleIndex = 0;
                staggerRemaining = 0;
                volleyHeads = 0;
                plant.setAttacking(false);
                return true;
            }
            staggerRemaining = peaPodDelayAfter(firedIndex, volleyHeads);
        } while (staggerRemaining == 0);
        plant.setAttacking(true);
        return false;
    }

    private static int peaPodDelayAfter(int firedIndex, int heads) {
        if (firedIndex >= 1 && firedIndex < 3 && firedIndex < heads - 1) {
            return 0;
        }
        return PEA_POD_STAGGER_TICKS;
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
        if (plant.isFumeShroom()) {
            context.spawnProjectile(plant, plant.getStats().damage(), 1, profile);
            return;
        }
        int shots = Plant.isPeaPod(plant.getName()) ? plant.getStackCount() : projectileCount;
        if (ahead || THREEPEATER.equals(plant.getName()) || plant.isPeaPod()) {
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
        boolean anyX = SPLIT_PEA.equals(plant.getName());
        if (THREEPEATER.equals(plant.getName())) {
            for (int offset : THREEPEATER_LANE_OFFSETS) {
                int row = plant.getRow() + offset;
                if (row < 0 || row >= context.getRowCount()) {
                    continue;
                }
                if (rowHasTarget(plant, context, row, anyX)) {
                    return true;
                }
            }
            return false;
        }
        if (plant.isFumeShroom()) {
            return rowHasTarget(plant, context, plant.getRow(), anyX);
        }
        return hasAhead(plant, context) || (SPLIT_PEA.equals(plant.getName()) && hasBehind(plant, context));
    }

    private static boolean rowHasTarget(Plant plant, GameContext context, int row, boolean anyX) {
        List<Zombie> zombies = context.getZombiesInRow(row);
        for (Zombie zombie : zombies) {
            if (!isAttackableTarget(zombie)) {
                continue;
            }
            if (anyX || zombie.getX() >= plant.getCol()) {
                if (plant.isFumeShroom() && !fumeHasTarget(plant, zombie)) {
                    continue;
                }
                return true;
            }
        }
        return hasDamageableTileAhead(plant, context, row);
    }

    public static boolean hasAhead(Plant plant, GameContext context) {
        List<Zombie> row = context.getZombiesInRow(plant.getRow());
        for (Zombie zombie : row) {
            if (isAttackableTarget(zombie) && zombie.getX() >= plant.getCol()) {
                return true;
            }
        }
        return hasDamageableTileAhead(plant, context, plant.getRow());
    }

    private static boolean hasDamageableTileAhead(Plant plant, GameContext context, int row) {
        int plantCol = plant.getCol();
        if (plant.isFumeShroom()) {
            double origin = plantCol + 0.5;
            double range = FumeMuzzles.RANGE_TILES
                    + plant.getStats().specialModifier(PlantSpecialModifiers.TILE_RANGE_EXT);
            for (int col = plantCol; col < context.getColCount(); col++) {
                if (!isDamageableObstacleTile(context.getTileAt(col, row))) {
                    continue;
                }
                if (FumeMuzzles.inRangeFromCenter(origin, col + 0.5, range)) {
                    return true;
                }
            }
            return false;
        }
        for (int col = plantCol + 1; col < context.getColCount(); col++) {
            if (isDamageableObstacleTile(context.getTileAt(col, row))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDamageableObstacleTile(Tile tile) {
        return tile != null && (tile.isGrave() || tile.isIce());
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

    private static boolean fumeHasTarget(Plant plant, Zombie zombie) {
        double origin = plant.getCol() + 0.5;
        double range = FumeMuzzles.RANGE_TILES
                + plant.getStats().specialModifier(PlantSpecialModifiers.TILE_RANGE_EXT);
        return FumeMuzzles.inRangeFromCenter(origin, zombie.getX(), range);
    }
}
