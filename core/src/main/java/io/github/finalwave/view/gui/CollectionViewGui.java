package io.github.finalwave.view.gui;

import io.github.finalwave.controller.CollectionController;
import io.github.finalwave.view.api.CollectionView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class CollectionViewGui extends GuiViewBase implements CollectionView {
    public CollectionViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(CollectionController controller) {
    }

    @Override
    public void showCurrentMenu() {
        router.refreshCollection();
    }

    @Override
    public void showPlantList(List<String> lines) {
        router.refreshCollection();
    }

    @Override
    public void showAllPlants(List<String> lines) {
        router.refreshCollection();
    }

    @Override
    public void showZombieList(List<String> lines) {
        router.refreshCollection();
    }

    @Override
    public void showAllZombies(List<String> lines) {
        router.refreshCollection();
    }

    @Override
    public void showPlantDetails(String details) {
        router.refreshCollection();
    }

    @Override
    public void showZombieDetails(String details) {
        router.refreshCollection();
    }

    @Override
    public void showPlantUpgraded(String plantName, int newLevel) {
        toast(plantName + " upgraded to level " + newLevel + ".");
        router.refreshCollection();
    }

    @Override
    public void showPlantPurchased(String plantName) {
        toast("Successfully purchased " + plantName + ".");
        router.refreshCollection();
    }

    @Override
    public void errorPlantNotFound(String plantName) {
        toastError("Plant '" + plantName + "' not found.");
    }

    @Override
    public void errorZombieNotFound(String zombieName) {
        toastError("Zombie '" + zombieName + "' not found.");
    }

    @Override
    public void errorZombieNotSeen(String zombieName) {
        toastError("Zombie '" + zombieName + "' has not been seen yet.");
    }

    @Override
    public void errorPlantNotOwned(String plantName) {
        toastError("You do not own " + plantName + ".");
    }

    @Override
    public void errorAlreadyOwned(String plantName) {
        toastError("You already own " + plantName + ".");
    }

    @Override
    public void errorMaxLevel(String plantName) {
        toastError(plantName + " is already at maximum level.");
    }

    @Override
    public void errorNotEnoughCoinsForUpgrade() {
        toastError("You don't have enough coins to upgrade.");
    }

    @Override
    public void errorNotEnoughSeedPacketsForUpgrade() {
        toastError("You don't have enough seed packets to upgrade.");
    }

    @Override
    public void errorNotEnoughCoinsToPurchase() {
        toastError("You don't have enough coins to purchase.");
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid collection command.");
    }
}
