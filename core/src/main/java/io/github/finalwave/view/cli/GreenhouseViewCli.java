package io.github.finalwave.view.cli;

import io.github.finalwave.view.api.GreenhouseView;

public class GreenhouseViewCli extends CliView implements GreenhouseView {
    @Override
    public void showCurrentMenu() {
        displayMessage("You are in the greenhouse menu.");
    }

    @Override
    public void showGreenhouse(String display) {
        displayMessage(display);
    }


    @Override
    public void showPlantPlantedInPot(int x, int y, String plantType) {
        displayMessage("Planted " + plantType + " in pot at (" + x + ", " + y + ")");
    }


    @Override
    public void showPotCollected(int x, int y, String reward) {
        displayMessage("Collected from pot at (" + x + ", " + y + "). Reward: " + reward);
    }


    @Override
    public void showPlantGrowthAccelerated(int x, int y, int diamondsSpent) {
        displayMessage("Growth accelerated for pot at (" + x + ", " + y + ") using " + diamondsSpent + " diamond(s).");
    }

    @Override
    public void showPotUnlocked(int x, int y) {
        displayMessage("Unlocked pot at (" + x + ", " + y + ").");
    }

    @Override
    public void errorPotLocked(int x, int y) {
        displayError("Pot at (" + x + ", " + y + ") is locked. Unlock it first.");
    }

    @Override
    public void errorPotAlreadyOccupied(int x, int y) {
        displayError("Pot at (" + x + ", " + y + ") is already occupied.");
    }

    @Override
    public void errorNoPotToPlant(int x, int y) {
        displayError("There is no available pot at (" + x + ", " + y + ").");
    }

    @Override
    public void errorNoPlantToCollect(int x, int y) {
        displayError("There is no fully grown plant to collect at (" + x + ", " + y + ").");
    }

    @Override
    public void errorPlantNotReady(int x, int y) {
        displayError("The plant in pot (" + x + ", " + y + ") is not ready yet.");
    }

    @Override
    public void errorNotEnoughDiamondsForAccelerate() {
        displayError("You don't have enough diamonds to accelerate growth.");
    }

    @Override
    public void errorCannotAccelerateReadyPlant(int x, int y) {
        displayError("The plant at (" + x + ", " + y + ") is already ready. No need to accelerate.");
    }

    @Override
    public void errorInvalidPotLocation(int x, int y) {
        displayError("Invalid pot location (" + x + ", " + y + ").");
    }

    @Override
    public void errorNotEnoughDiamondsToUnlock() {
        displayError("You don't have enough diamonds to unlock this pot.");
    }

    @Override
    public void errorPotAlreadyUnlocked(int x, int y) {
        displayError("Pot at (" + x + ", " + y + ") is already unlocked.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid greenhouse command.");
    }
}