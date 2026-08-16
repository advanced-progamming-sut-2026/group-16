package io.github.finalwave.view.gui;

import io.github.finalwave.controller.GreenhouseController;
import io.github.finalwave.view.api.GreenhouseView;
import io.github.finalwave.view.gui.screen.ScreenRouter;


public final class GreenhouseViewGui extends GuiViewBase implements GreenhouseView {
    public GreenhouseViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(GreenhouseController controller) {
    }

    @Override
    public void showCurrentMenu() {
        router.refreshGreenhouse();
    }

    @Override
    public void showGreenhouse(String display) {
        router.refreshGreenhouse();
    }

    @Override
    public void showPlantPlantedInPot(int x, int y, String plantType) {
        toast("Planted " + plantType);
        router.refreshGreenhouse();
    }

    @Override
    public void showPotCollected(int x, int y, String reward) {
        router.showGreenhouseCollectReward(reward);
        router.refreshGreenhouse();
    }

    @Override
    public void showPlantGrowthAccelerated(int x, int y, int diamondsSpent) {
        toast("Growth accelerated (" + diamondsSpent + " gems)");
        router.refreshGreenhouse();
    }

    @Override
    public void showPotUnlocked(int x, int y) {
        toast("Pot unlocked");
        router.refreshGreenhouse();
    }

    @Override
    public void errorPotLocked(int x, int y) {
        toastError("This pot is locked.");
    }

    @Override
    public void errorPotAlreadyOccupied(int x, int y) {
        toastError("This pot already has a plant.");
    }

    @Override
    public void errorNoPotToPlant(int x, int y) {
        toastError("No pot available here.");
    }

    @Override
    public void errorNoPlantToCollect(int x, int y) {
        toastError("Nothing to harvest here.");
    }

    @Override
    public void errorPlantNotReady(int x, int y) {
        toastError("This plant is still growing.");
    }

    @Override
    public void errorNotEnoughDiamondsForAccelerate() {
        toastError("Not enough gems to speed up growth.");
    }

    @Override
    public void errorCannotAccelerateReadyPlant(int x, int y) {
        toastError("This plant is already ready.");
    }

    @Override
    public void errorInvalidPotLocation(int x, int y) {
        toastError("Invalid pot.");
    }

    @Override
    public void errorNotEnoughDiamondsToUnlock() {
        toastError("Not enough gems to unlock this pot.");
    }

    @Override
    public void errorPotAlreadyUnlocked(int x, int y) {
        toastError("This pot is already unlocked.");
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid greenhouse command.");
    }
}
