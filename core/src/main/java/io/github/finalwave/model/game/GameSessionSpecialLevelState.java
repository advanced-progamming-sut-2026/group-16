package io.github.finalwave.model.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


final class GameSessionSpecialLevelState {

    private final List<String> conveyorBeltPlants = new ArrayList<>();
    private boolean conveyorBeltActive;

    private final Set<String> levelLockedPlants = new HashSet<>();

    private final Set<String> protectedSeedPlantIds = new HashSet<>();
    private final List<SeedPlacement> protectedSeedPlacements = new ArrayList<>();

    private boolean timedWarActive;
    private TimedWarRules timedWarRules;
    private int timedWarTicksElapsed;
    private int timedWarProgress;

    private Integer deadLineColumn;
    private Integer loveYourPlantsMaxLoss;

    private boolean plantWhatYouGetActive;
    private boolean prepPhaseActive;

    void activateConveyorBelt() {
        conveyorBeltActive = true;
    }

    boolean isConveyorBeltActive() {
        return conveyorBeltActive;
    }

    void addConveyorBeltPlant(String plantName) {
        if (plantName != null) {
            conveyorBeltPlants.add(plantName);
        }
    }

    List<String> getConveyorBeltPlants() {
        return List.copyOf(conveyorBeltPlants);
    }

    boolean hasConveyorBeltPlant(String plantName) {
        return conveyorBeltPlants.contains(plantName);
    }

    void removeConveyorBeltPlant(String plantName) {
        conveyorBeltPlants.remove(plantName);
    }

    void activateLockedPlants(LockedPlantsRules rules) {
        levelLockedPlants.clear();
        if (rules != null) {
            levelLockedPlants.addAll(rules.getLockedPlants());
        }
    }

    Set<String> getLevelLockedPlants() {
        return Set.copyOf(levelLockedPlants);
    }

    boolean isLevelLockedPlant(String plantName) {
        return plantName != null && levelLockedPlants.contains(plantName);
    }

    void registerProtectedSeed(String plantId, String plantName, int col, int row) {
        protectedSeedPlantIds.add(plantId);
        protectedSeedPlacements.add(new SeedPlacement(plantName, col, row));
    }

    boolean isProtectedSeedId(String plantId) {
        return plantId != null && protectedSeedPlantIds.contains(plantId);
    }

    List<SeedPlacement> getProtectedSeedPlacements() {
        return List.copyOf(protectedSeedPlacements);
    }

    List<Integer> getDangerRows() {
        Set<Integer> rows = new LinkedHashSet<>();
        for (SeedPlacement placement : protectedSeedPlacements) {
            rows.add(placement.getRow());
        }
        return List.copyOf(rows);
    }

    void activateTimedWar(TimedWarRules rules) {
        if (rules == null || !rules.isActiveRules()) {
            timedWarActive = false;
            timedWarRules = null;
            timedWarTicksElapsed = 0;
            timedWarProgress = 0;
            return;
        }
        timedWarActive = true;
        timedWarRules = rules;
        timedWarTicksElapsed = 0;
        timedWarProgress = 0;
    }

    boolean isTimedWarActive() {
        return timedWarActive;
    }

    TimedWarRules getTimedWarRules() {
        return timedWarRules;
    }

    int getTimedWarProgress() {
        return timedWarProgress;
    }

    int getTimedWarRemainingTicks() {
        if (!timedWarActive || timedWarRules == null) {
            return 0;
        }
        return Math.max(0, timedWarRules.getDurationTicks() - timedWarTicksElapsed);
    }

    boolean isTimedWarGoalMet() {
        return timedWarActive && timedWarRules != null && timedWarRules.isGoalMet(timedWarProgress);
    }

    void advanceTimedWarTick() {
        if (timedWarActive) {
            timedWarTicksElapsed++;
        }
    }

    void registerTimedWarKill() {
        if (timedWarActive && timedWarRules != null && timedWarRules.getMode() == TimedWarMode.KILL) {
            timedWarProgress++;
        }
    }

    void addTimedWarSunProgress(int sunValue) {
        if (timedWarActive && timedWarRules != null && timedWarRules.getMode() == TimedWarMode.SUN) {
            timedWarProgress += Math.max(0, sunValue);
        }
    }

    void activateDeadLine(int column) {
        deadLineColumn = column;
    }

    boolean isDeadLineActive() {
        return deadLineColumn != null;
    }

    int getDeadLineColumn() {
        if (deadLineColumn == null) {
            throw new IllegalStateException("dead line is not active");
        }
        return deadLineColumn;
    }

    void activateLoveYourPlants(int maxLoss) {
        if (maxLoss < 1) {
            throw new IllegalArgumentException("maxLoss must be at least 1");
        }
        loveYourPlantsMaxLoss = maxLoss;
    }

    boolean isLoveYourPlantsActive() {
        return loveYourPlantsMaxLoss != null;
    }

    int getLoveYourPlantsMaxLoss() {
        if (loveYourPlantsMaxLoss == null) {
            throw new IllegalStateException("love your plants mode is not active");
        }
        return loveYourPlantsMaxLoss;
    }

    int getLoveYourPlantsRemaining(int plantsLost) {
        if (loveYourPlantsMaxLoss == null) {
            throw new IllegalStateException("love your plants mode is not active");
        }
        return Math.max(0, loveYourPlantsMaxLoss - plantsLost);
    }

    void activatePlantWhatYouGet() {
        plantWhatYouGetActive = true;
        prepPhaseActive = true;
    }

    boolean isPlantWhatYouGetActive() {
        return plantWhatYouGetActive;
    }

    boolean isPrepPhaseActive() {
        return prepPhaseActive;
    }

    void endPrepPhase() {
        prepPhaseActive = false;
    }
}
