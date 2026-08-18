package io.github.finalwave.view.gui;

import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.model.game.LockedPlantsMode;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.SeedPlacement;
import io.github.finalwave.model.game.TimedWarMode;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.view.api.ConveyBeltView;
import io.github.finalwave.view.api.DeadLineView;
import io.github.finalwave.view.api.GamePlayView;
import io.github.finalwave.view.api.LockedPlantsView;
import io.github.finalwave.view.api.LoveYourPlantsView;
import io.github.finalwave.view.api.NightOpsView;
import io.github.finalwave.view.api.PlantWhatYouGetView;
import io.github.finalwave.view.api.SaveOurSeedsView;
import io.github.finalwave.view.api.SpecialLevelView;
import io.github.finalwave.view.api.TimedWarView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class GamePlayViewGui extends GuiViewBase implements
        GamePlayView,
        SpecialLevelView,
        ConveyBeltView,
        SaveOurSeedsView,
        DeadLineView,
        TimedWarView,
        LoveYourPlantsView,
        PlantWhatYouGetView,
        LockedPlantsView,
        NightOpsView {

    public GamePlayViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(GamePlayController controller) {
        if (controller != null) {
            controller.setDeferMatchExit(true);
        }
    }

    @Override
    public void showAdvanceTime(int ticks) {
    }

    @Override
    public void showSunProduced(Plant plant, int x, int y) {
    }

    @Override
    public void showSunDropped(Sun sun, int x, int y) {
    }

    @Override
    public void showSunReachedGround(int x, int y) {
    }

    @Override
    public void showSunAmount(int amount) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showCheatAddedSuns(int suns) {
        playToast("Added " + suns + " sun.");
        router.refreshGamePlayHud();
    }

    @Override
    public void showPlantPlanted(String plantType, int x, int y) {
    }

    @Override
    public void showPlantPlucked(int x, int y) {
    }

    @Override
    public void showPlantDestroyed(Plant plant, int x, int y) {
    }

    @Override
    public void showLawnMowerTriggered(int row) {
    }

    @Override
    public void showLawnMowerFailed(int row) {
        playError("No lawn mower in that row.");
    }

    @Override
    public void showWaveStarted(int waveNumber) {
        router.showGamePlayAlert("Wave " + waveNumber);
    }

    @Override
    public void showFinalWave() {
        router.showGamePlayAlert("A huge wave of zombies is approaching!");
    }

    @Override
    public void showZombieSpawned(String zombieType, int wave, int lane, int cost) {
    }

    @Override
    public void showZombieDied(String zombieType, double x, double y) {
    }

    @Override
    public void showNukeActivated() {
        playToast("Nuke activated.");
    }

    @Override
    public void showCheatCooldownRemoved() {
        playToast("Cooldowns cleared.");
    }

    @Override
    public void showGlowingZombieDroppedFood(int currentFoods) {
        playToast("Plant food dropped. You have " + currentFoods + ".");
        router.refreshGamePlayHud();
    }

    @Override
    public void showPlantFed(int x, int y) {
    }

    @Override
    public void showCheatAddedPlantFood() {
        playToast("Added plant food.");
        router.refreshGamePlayHud();
    }

    @Override
    public void showWinMessage() {
        router.showGamePlayResult(MatchResult.WON);
    }

    @Override
    public void showLoseMessage() {
        router.showGamePlayResult(MatchResult.LOST);
    }

    @Override
    public void showMap(String mapRepresentation) {
    }

    @Override
    public void showPlantsStatus(String status) {
    }

    @Override
    public void showTileStatus(String tileInfo) {
    }

    @Override
    public void showItemDropped(String itemType, int count) {
        if ("coin".equals(itemType)) {
            playToast("Coins: " + count);
        } else if ("diamond".equals(itemType)) {
            playToast("Gems: " + count);
        } else if ("pot".equals(itemType)) {
            playToast("Greenhouse pot unlocked.");
        }
        router.refreshGamePlayHud();
    }

    @Override
    public void showZombiesInfo(List<Zombie> zombies) {
    }

    @Override
    public void showCheatSpawnZombie(String zombieType, double x, double y) {
    }

    @Override
    public void errorPlantNotSelected(String type) {
        playError(type + " is not selected.");
    }

    @Override
    public void errorPlantNotOnConveyorBelt(String type) {
        playError(type + " is not on the conveyor.");
    }

    @Override
    public void errorLevelPlantLocked(String type) {
        playError(type + " is locked for this level.");
    }

    @Override
    public void errorPlantAlreadySelected(String type) {
        playError(type + " is already selected.");
    }

    @Override
    public void errorPlantLocked(String type) {
        playError(type + " is locked.");
    }

    @Override
    public void errorPlantNotFound(String type) {
        playError("Plant not found: " + type);
    }

    @Override
    public void errorNoPlantToRemove(String type) {
        playError("No " + type + " to remove.");
    }

    @Override
    public void errorNotEnoughSun() {
        playError("Not enough sun.");
    }

    @Override
    public void errorInvalidLocation(int x, int y) {
        playError("Invalid location.");
    }

    @Override
    public void errorCannotPlantHere(int x, int y) {
        playError("Cannot plant here.");
    }

    @Override
    public void errorPlantOnCooldown(String type) {
        playError(type + " is on cooldown.");
    }

    @Override
    public void errorNoPlantToPluck(int x, int y) {
        playError("Nothing to pluck.");
    }

    @Override
    public void errorCannotPluckProtectedSeed(int x, int y) {
        playError("Cannot pluck that seed.");
    }

    @Override
    public void errorNoPlantFood() {
        playError("No plant food.");
    }

    @Override
    public void errorCannotFeedHere(int x, int y) {
        playError("Cannot feed here.");
    }

    @Override
    public void errorCannotBoostPlant(String type) {
        playError("Cannot boost " + type + ".");
    }

    @Override
    public void errorNotEnoughDiamonds() {
        playError("Not enough gems.");
    }

    @Override
    public void errorGameNotStarted() {
        playError("Game has not started.");
    }

    @Override
    public void errorInvalidCommand() {
        playError("Invalid command.");
    }

    @Override
    public void errorInvalidTickCount() {
        playError("Invalid tick count.");
    }

    @Override
    public void errorNegativeTickCount() {
        playError("Tick count cannot be negative.");
    }

    @Override
    public void errorNoSunAt(int col, int row) {
        playError("No sun there.");
    }

    @Override
    public void errorInvalidSunCount() {
        playError("Invalid sun count.");
    }

    @Override
    public void errorInvalidZombieLocation() {
        playError("Invalid zombie location.");
    }

    @Override
    public void errorZombieSpawnFailed(String message) {
        playError(message);
    }

    @Override
    public void showLawnMowerKilledZombie(String zombieType) {
    }

    @Override
    public void showGraveCreated(int col, int row, String lootType) {
    }

    @Override
    public void showConveyorBelt(List<String> plantsOnBelt) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showConveyorBeltPlantArrived(String plantName) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showProtectedSeeds(List<SeedPlacement> seeds) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showDangerRows(List<Integer> rows) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showProtectedSeedDestroyed(String plantName, int x, int y) {
        playError(plantName + " was destroyed.");
    }

    @Override
    public void showDeadLineRule(int column) {
    }

    @Override
    public void showDeadLineBreached(int column, String zombieType) {
        playError("Zombies crossed the deadline.");
    }

    @Override
    public void showTimedWarStatus(TimedWarMode mode, int remainingSeconds, int durationSeconds, int progress, int goal) {
    }

    @Override
    public void showTimedWarTimeUp() {
        playToast("Time is up.");
    }

    @Override
    public void showTimedWarGoalReached(TimedWarMode mode, int progress) {
        playToast("Goal reached.");
    }

    @Override
    public void showLoveYourPlantsRule(int maxPlantsLost) {
    }

    @Override
    public void showPlantLossStatus(int plantsLost, int maxAllowed) {
    }

    @Override
    public void showLoveYourPlantsLimitReached(int plantsLost, int maxAllowed) {
        playError("Too many plants lost.");
    }

    @Override
    public void showPlantWhatYouGetRule(int startingSun) {
    }

    @Override
    public void showPrepPhaseHint() {
        playToast("Start waves when you are ready.");
    }

    @Override
    public void showWavesStartedFromPrep() {
    }

    @Override
    public void showLockedPlantsSummary(LockedPlantsMode mode, List<String> locked) {
    }

    @Override
    public void showNightOpsMode() {
    }

    private void playToast(String message) {
        router.toastGamePlayMessage(message);
    }

    private void playError(String message) {
        router.toastGamePlayError(message);
    }
}
