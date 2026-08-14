package io.github.finalwave.view.gui;

import io.github.finalwave.controller.PlantSelectionController;
import io.github.finalwave.model.game.LockedPlantsMode;
import io.github.finalwave.view.api.LockedPlantsSelectionView;
import io.github.finalwave.view.api.PlantSelectionView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class PlantSelectionViewGui extends GuiViewBase implements PlantSelectionView, LockedPlantsSelectionView {
    public PlantSelectionViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(PlantSelectionController controller) {
    }

    @Override
    public void showAllPlants(List<String> plants) {
        router.refreshPlantSelection();
    }

    @Override
    public void showAvailablePlants(List<String> plants) {
        router.refreshPlantSelection();
    }

    @Override
    public void showSelectedPlants(List<String> plants) {
        router.refreshPlantSelection();
    }

    @Override
    public void showPlantAdded(String type) {
        router.refreshPlantSelection();
    }

    @Override
    public void showPlantRemoved(String type) {
        router.refreshPlantSelection();
    }

    @Override
    public void showPlantBoosted(String type) {
        toast(type + " is boosted for this level.");
        router.refreshPlantSelection();
    }

    @Override
    public void showPlantUpgraded(String type, int newLevel) {
        toast(type + " upgraded to level " + newLevel + ".");
        router.refreshPlantSelection();
    }

    @Override
    public void showCurrentMenu() {
        router.refreshPlantSelection();
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
        toastError("Add " + type + " to the loadout before boosting.");
    }

    @Override
    public void errorNotEnoughDiamonds() {
        toastError("Not enough gems.");
    }

    @Override
    public void errorLoadoutEmpty() {
        toastError("Select at least one plant.");
    }

    @Override
    public void errorUpgradeFailed(String message) {
        toastError(message);
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid command.");
    }

    @Override
    public void showLockedPlantsRules(LockedPlantsMode mode) {
        if (mode != null) {
            toast("Locked plants: " + mode.name());
        }
        router.refreshPlantSelection();
    }

    @Override
    public void showLockedPlants(List<String> locked) {
        router.refreshPlantSelection();
    }

    @Override
    public void errorPlantLockedForLevel(String type) {
        toastError(type + " is locked for this level.");
    }
}
