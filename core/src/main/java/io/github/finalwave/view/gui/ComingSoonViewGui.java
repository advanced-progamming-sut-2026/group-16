package io.github.finalwave.view.gui;

import io.github.finalwave.controller.ViewController;
import io.github.finalwave.model.game.LockedPlantsMode;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.view.api.ConveyBeltView;
import io.github.finalwave.view.api.LockedPlantsSelectionView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;

public final class ComingSoonViewGui extends GuiViewBase implements LockedPlantsSelectionView, ConveyBeltView {
    public ComingSoonViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(ViewController controller) {
    }

    @Override
    public void showAllPlants(List<String> plants) {
    }

    @Override
    public void showAvailablePlants(List<String> plants) {
    }

    @Override
    public void showSelectedPlants(List<String> plants) {
    }

    @Override
    public void showPlantAdded(String type) {
    }

    @Override
    public void showPlantRemoved(String type) {
    }

    @Override
    public void showPlantBoosted(String type) {
    }

    @Override
    public void showCurrentMenu() {
    }

    @Override
    public void showGameStarted() {
    }

    @Override
    public void errorPlantNotFound(String type) {
        toastError("Plant not found: " + type);
    }

    @Override
    public void errorPlantAlreadySelected(String type) {
        toastError(type + " is already selected.");
    }

    @Override
    public void errorPlantNotSelected(String type) {
        toastError(type + " is not selected.");
    }

    @Override
    public void errorPlantLocked(String type) {
        toastError(type + " is locked.");
    }

    @Override
    public void errorSunProducerBanned(String type) {
        toastError(type + " cannot be used here.");
    }

    @Override
    public void errorLoadoutFull(int maxSlots) {
        toastError("Loadout is full.");
    }

    @Override
    public void errorCannotBoostPlant(String type) {
        toastError("Cannot boost " + type + ".");
    }

    @Override
    public void errorNotEnoughDiamonds() {
        toastError("Not enough gems.");
    }

    @Override
    public void errorLoadoutEmpty() {
        toastError("Select plants first.");
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid command.");
    }

    @Override
    public void showLockedPlantsRules(LockedPlantsMode mode) {
    }

    @Override
    public void showLockedPlants(List<String> locked) {
    }

    @Override
    public void errorPlantLockedForLevel(String type) {
        toastError(type + " is locked for this level.");
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
    }

    @Override
    public void showCheatAddedSuns(int suns) {
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
    }

    @Override
    public void showWaveStarted(int waveNumber) {
    }

    @Override
    public void showFinalWave() {
    }

    @Override
    public void showZombieSpawned(String zombieType, int wave, int lane, int cost) {
    }

    @Override
    public void showZombieDied(String zombieType, double x, double y) {
    }

    @Override
    public void showNukeActivated() {
    }

    @Override
    public void showCheatCooldownRemoved() {
    }

    @Override
    public void showGlowingZombieDroppedFood(int currentFoods) {
    }

    @Override
    public void showPlantFed(int x, int y) {
    }

    @Override
    public void showCheatAddedPlantFood() {
    }

    @Override
    public void showWinMessage() {
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
    }

    @Override
    public void showZombiesInfo(List<Zombie> zombies) {
    }

    @Override
    public void showCheatSpawnZombie(String zombieType, double x, double y) {
    }

    @Override
    public void errorNoPlantToRemove(String type) {
        toastError("No " + type + " to remove.");
    }

    @Override
    public void errorNotEnoughSun() {
        toastError("Not enough sun.");
    }

    @Override
    public void errorInvalidLocation(int x, int y) {
        toastError("Invalid location.");
    }

    @Override
    public void errorCannotPlantHere(int x, int y) {
        toastError("Cannot plant here.");
    }

    @Override
    public void errorPlantOnCooldown(String type) {
        toastError(type + " is on cooldown.");
    }

    @Override
    public void errorNoPlantToPluck(int x, int y) {
        toastError("Nothing to pluck.");
    }

    @Override
    public void errorCannotPluckProtectedSeed(int x, int y) {
        toastError("Cannot pluck that seed.");
    }

    @Override
    public void errorNoPlantFood() {
        toastError("No plant food.");
    }

    @Override
    public void errorCannotFeedHere(int x, int y) {
        toastError("Cannot feed here.");
    }

    @Override
    public void errorGameNotStarted() {
        toastError("Game has not started.");
    }

    @Override
    public void errorInvalidTickCount() {
        toastError("Invalid tick count.");
    }

    @Override
    public void errorNegativeTickCount() {
        toastError("Tick count cannot be negative.");
    }

    @Override
    public void errorNoSunAt(int col, int row) {
        toastError("No sun there.");
    }

    @Override
    public void errorInvalidSunCount() {
        toastError("Invalid sun count.");
    }

    @Override
    public void errorInvalidZombieLocation() {
        toastError("Invalid zombie location.");
    }

    @Override
    public void errorZombieSpawnFailed(String message) {
        toastError(message);
    }

    @Override
    public void showLawnMowerKilledZombie(String zombieType) {
    }

    @Override
    public void showGraveCreated(int col, int row, String lootType) {
    }

    @Override
    public void errorPlantNotOnConveyorBelt(String type) {
        toastError(type + " is not on the conveyor.");
    }

    @Override
    public void errorLevelPlantLocked(String type) {
        toastError(type + " is locked for this level.");
    }

    @Override
    public void showConveyorBelt(List<String> plantsOnBelt) {
    }

    @Override
    public void showConveyorBeltPlantArrived(String plantName) {
    }
}
