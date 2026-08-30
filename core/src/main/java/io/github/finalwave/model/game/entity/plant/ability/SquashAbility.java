package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.HashSet;
import java.util.Set;

public final class SquashAbility implements PlantAbility {

    public enum Phase {
        IDLE,
        JUMP_UP_RIGHT,
        JUMP_DOWN_RIGHT,
        TURN,
        JUMP_UP_LEFT,
        JUMP_DOWN_LEFT,
        DONE
    }

    public static final int BASE_SMASH_CHARGES = 2;
    public static final int PLANT_FOOD_MAX_TARGETS = 5;
    public static final int PLANT_FOOD_RADIUS = 1;

    public static final float JUMP_UP_SECONDS = 0.38f;
    public static final float JUMP_DOWN_SECONDS = 0.38f;
    public static final float TURN_SECONDS = 0.65f;
    public static final float PF_JUMP_DOWN_RIGHT_SECONDS = 0.35f;
    public static final float PF_JUMP_DOWN_LEFT_SECONDS = 0.33f;

    private Phase phase = Phase.IDLE;
    private int phaseTicksRemaining;
    private int targetCol = -1;
    private int targetRow = -1;
    private int homeCol = -1;
    private int homeRow = -1;
    private int segmentFromCol = -1;
    private int segmentFromRow = -1;
    private int segmentToCol = -1;
    private int segmentToRow = -1;
    private int smashesRemaining;
    private int plantFoodHitsRemaining;
    private int plantFoodHitsDone;
    private boolean plantFoodActive;
    private boolean plantFoodPending;
    private boolean plantFoodChain;
    private final Set<String> plantFoodVisitedTiles = new HashSet<>();

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        smashesRemaining = maxSmashes(plant);
    }

    public Phase phase() {
        return phase;
    }

    public int targetCol() {
        return targetCol;
    }

    public int targetRow() {
        return targetRow;
    }

    public int segmentFromCol() {
        return segmentFromCol;
    }

    public int segmentFromRow() {
        return segmentFromRow;
    }

    public int segmentToCol() {
        return segmentToCol;
    }

    public int segmentToRow() {
        return segmentToRow;
    }

    public int smashesRemaining() {
        return smashesRemaining;
    }

    public int plantFoodHitsDone() {
        return plantFoodHitsDone;
    }

    public boolean plantFoodActive() {
        return plantFoodActive;
    }

    public boolean plantFoodChain() {
        return plantFoodChain;
    }

    public int phaseTicksRemaining() {
        return phaseTicksRemaining;
    }

    public static float phaseSeconds(Phase phase, boolean plantFood) {
        return switch (phase) {
            case JUMP_UP_RIGHT, JUMP_UP_LEFT -> JUMP_UP_SECONDS;
            case JUMP_DOWN_RIGHT -> plantFood ? PF_JUMP_DOWN_RIGHT_SECONDS : JUMP_DOWN_SECONDS;
            case JUMP_DOWN_LEFT -> plantFood ? PF_JUMP_DOWN_LEFT_SECONDS : JUMP_DOWN_SECONDS;
            case TURN -> TURN_SECONDS;
            default -> 0f;
        };
    }

    public static int phaseTicksFor(Phase phase, boolean plantFood) {
        return secondsToTicks(phaseSeconds(phase, plantFood));
    }

    public boolean isSmashing() {
        return phase != Phase.IDLE && phase != Phase.DONE;
    }

    public void startPlantFoodSmash() {
        plantFoodPending = true;
        plantFoodActive = true;
        smashesRemaining = Math.max(smashesRemaining, BASE_SMASH_CHARGES);
    }

    public void tickSmash(Plant plant, GameContext context) {
        if (phase == Phase.DONE) {
            return;
        }
        if (phase == Phase.IDLE) {
            if (smashesRemaining > 0 && tryBeginFromIdle(plant, context)) {
                return;
            }
            clearPlantFoodIntent();
            return;
        }
        if (phaseTicksRemaining > 0) {
            phaseTicksRemaining--;
            return;
        }
        advancePhase(plant, context);
    }

    private boolean tryBeginFromIdle(Plant plant, GameContext context) {
        if (plantFoodPending) {
            if (!beginPlantFoodChain(plant, context)) {
                clearPlantFoodIntent();
                return false;
            }
            return true;
        }
        if (plantFoodActive) {
            clearPlantFoodIntent();
            return false;
        }
        if (!findNormalTarget(plant, context)) {
            return false;
        }
        homeCol = plant.getCol();
        homeRow = plant.getRow();
        beginSmash(plant, false);
        return true;
    }

    private boolean beginPlantFoodChain(Plant plant, GameContext context) {
        homeCol = plant.getCol();
        homeRow = plant.getRow();
        resetSegmentCoords();
        plantFoodVisitedTiles.clear();
        plantFoodHitsDone = 0;
        plantFoodPending = false;
        plantFoodChain = true;
        plantFoodActive = true;
        plantFoodHitsRemaining = PLANT_FOOD_MAX_TARGETS;
        if (!selectNextPlantFoodTarget(plant, context, homeCol, homeRow)) {
            clearPlantFoodIntent();
            return false;
        }
        beginSmash(plant, true);
        return true;
    }

    private static int maxSmashes(Plant plant) {
        int bonus = (int) plant.getStats().specialModifier(PlantSpecialModifiers.BONUS_SMASH_CHARGES);
        return BASE_SMASH_CHARGES + bonus;
    }

    private boolean findNormalTarget(Plant plant, GameContext context) {
        int row = plant.getRow();
        int wantedCol = plant.getCol() + 1;
        for (Zombie zombie : context.getZombiesInRow(row)) {
            if (!zombie.isAlive()) {
                continue;
            }
            if ((int) Math.floor(zombie.getX()) == wantedCol) {
                targetCol = wantedCol;
                targetRow = row;
                return true;
            }
        }
        return false;
    }

    private boolean selectNextPlantFoodTarget(Plant plant,
                                              GameContext context,
                                              int fromCol,
                                              int fromRow) {
        Zombie nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (Zombie zombie : context.getAllZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zombieCol = (int) Math.floor(zombie.getX());
            int zombieRow = zombie.getRow();
            if (!inPlantFoodRange(plant, zombieCol, zombieRow)) {
                continue;
            }
            if (zombieCol == fromCol && zombieRow == fromRow) {
                continue;
            }
            if (plantFoodVisitedTiles.contains(tileKey(zombieCol, zombieRow))) {
                continue;
            }
            double distance = tileDistance(fromCol, fromRow, zombieCol, zombieRow);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = zombie;
            }
        }
        if (nearest == null) {
            return false;
        }
        targetCol = (int) Math.floor(nearest.getX());
        targetRow = nearest.getRow();
        segmentToCol = targetCol;
        segmentToRow = targetRow;
        return true;
    }

    private static String tileKey(int col, int row) {
        return col + "," + row;
    }

    private void markPlantFoodTileVisited(int col, int row) {
        plantFoodVisitedTiles.add(tileKey(col, row));
    }

    private boolean inPlantFoodRange(Plant plant, int col, int row) {
        return Math.abs(col - homeCol) <= PLANT_FOOD_RADIUS
                && Math.abs(row - homeRow) <= PLANT_FOOD_RADIUS;
    }

    private static double tileDistance(int fromCol, int fromRow, int toCol, int toRow) {
        return Math.abs(toCol - fromCol) + Math.abs(toRow - fromRow);
    }

    private void beginSmash(Plant plant, boolean plantFood) {
        if (homeCol < 0) {
            homeCol = plant.getCol();
            homeRow = plant.getRow();
        }
        segmentFromCol = homeCol;
        segmentFromRow = homeRow;
        segmentToCol = targetCol;
        segmentToRow = targetRow;
        plantFoodChain = plantFood || plantFoodChain;
        plantFoodActive = plantFood || plantFoodActive;
        plant.setAttacking(true);
        phase = Phase.JUMP_UP_RIGHT;
        phaseTicksRemaining = jumpUpTicks();
    }

    private void assignJumpSegment(int fromCol, int fromRow, int toCol, int toRow) {
        segmentFromCol = fromCol;
        segmentFromRow = fromRow;
        segmentToCol = toCol;
        segmentToRow = toRow;
    }

    private void advancePhase(Plant plant, GameContext context) {
        switch (phase) {
            case JUMP_UP_RIGHT -> {
                phase = Phase.JUMP_DOWN_RIGHT;
                phaseTicksRemaining = jumpDownTicks(usePlantFoodClips());
            }
            case JUMP_DOWN_RIGHT -> onForwardLanding(plant, context);
            case TURN -> {
                phase = returningHome() ? Phase.JUMP_UP_LEFT : Phase.JUMP_UP_RIGHT;
                phaseTicksRemaining = jumpUpTicks();
            }
            case JUMP_UP_LEFT -> {
                phase = Phase.JUMP_DOWN_LEFT;
                phaseTicksRemaining = jumpDownLeftTicks(usePlantFoodClips());
            }
            case JUMP_DOWN_LEFT -> onReturnLanding(plant);
            default -> {
            }
        }
    }

    private boolean usePlantFoodClips() {
        return plantFoodChain;
    }

    private void onForwardLanding(Plant plant, GameContext context) {
        applySmashDamage(plant, context);
        if (plantFoodChain) {
            markPlantFoodTileVisited(targetCol, targetRow);
        }
        segmentFromCol = targetCol;
        segmentFromRow = targetRow;
        if (plantFoodChain) {
            plantFoodHitsDone++;
            plantFoodHitsRemaining--;
            if (plantFoodHitsRemaining > 0
                    && selectNextPlantFoodTarget(plant, context, segmentFromCol, segmentFromRow)) {
                assignJumpSegment(segmentFromCol, segmentFromRow, segmentToCol, segmentToRow);
                phase = Phase.TURN;
                phaseTicksRemaining = turnTicks();
                return;
            }
            beginReturnHome();
            return;
        }
        smashesRemaining--;
        if (smashesRemaining <= 0) {
            finishOnTarget(plant);
            return;
        }
        beginReturnHome();
    }

    private void beginReturnHome() {
        segmentToCol = homeCol;
        segmentToRow = homeRow;
        phase = Phase.TURN;
        phaseTicksRemaining = turnTicks();
    }

    private void onReturnLanding(Plant plant) {
        if (plantFoodChain) {
            smashesRemaining = Math.max(0, smashesRemaining - 1);
        }
        clearPlantFoodIntent();
        phase = Phase.IDLE;
        plant.setAttacking(false);
        targetCol = -1;
        targetRow = -1;
        resetSegmentCoords();
        homeCol = -1;
        homeRow = -1;
    }

    private void resetSegmentCoords() {
        segmentFromCol = -1;
        segmentFromRow = -1;
        segmentToCol = -1;
        segmentToRow = -1;
    }

    private void clearPlantFoodIntent() {
        plantFoodPending = false;
        plantFoodActive = false;
        plantFoodChain = false;
        plantFoodHitsRemaining = 0;
        plantFoodVisitedTiles.clear();
    }

    private boolean returningHome() {
        return segmentToCol == homeCol && segmentToRow == homeRow;
    }

    private void finishOnTarget(Plant plant) {
        phase = Phase.DONE;
        clearPlantFoodIntent();
        resetSegmentCoords();
        homeCol = -1;
        homeRow = -1;
        plant.setAttacking(false);
        plant.consumeInstantly();
    }

    private void applySmashDamage(Plant plant, GameContext context) {
        int damage = plant.getStats().damage();
        for (Zombie zombie : context.getAllZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            if ((int) Math.floor(zombie.getX()) == targetCol && zombie.getRow() == targetRow) {
                zombie.takeDamage(damage);
                if (zombie.isDead()) {
                    context.onZombieKilled(zombie);
                }
            }
        }
    }

    private static int jumpUpTicks() {
        return secondsToTicks(JUMP_UP_SECONDS);
    }

    private static int jumpDownTicks(boolean plantFood) {
        return secondsToTicks(plantFood ? PF_JUMP_DOWN_RIGHT_SECONDS : JUMP_DOWN_SECONDS);
    }

    private static int jumpDownLeftTicks(boolean plantFood) {
        return secondsToTicks(plantFood ? PF_JUMP_DOWN_LEFT_SECONDS : JUMP_DOWN_SECONDS);
    }

    private static int turnTicks() {
        return secondsToTicks(TURN_SECONDS);
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
