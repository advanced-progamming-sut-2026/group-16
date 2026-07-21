package view.cli;

import view.api.CollectionView;

import java.util.List;

public class CollectionViewCli extends CliView implements CollectionView {
    @Override
    public void showCurrentMenu() {
        displayMessage("You are in the collection menu.");
    }

    @Override
    public void showPlantList(List<String> lines) {
        displayMessage("Owned plants:");
        for (String line : lines) {
            displayMessage("- " + line);
        }
    }

    @Override
    public void showAllPlants(List<String> lines) {
        displayMessage("All plants:");
        for (String line : lines) {
            displayMessage("- " + line);
        }
    }

    @Override
    public void showZombieList(List<String> lines) {
        displayMessage("Seen zombies:");
        for (String line : lines) {
            displayMessage("- " + line);
        }
    }

    @Override
    public void showAllZombies(List<String> lines) {
        displayMessage("All zombies:");
        for (String line : lines) {
            displayMessage("- " + line);
        }
    }

    @Override
    public void showPlantDetails(String details) {
        displayMessage(details);
    }

    @Override
    public void showZombieDetails(String details) {
        displayMessage(details);
    }

    @Override
    public void showPlantUpgraded(String plantName, int newLevel) {
        displayMessage(plantName + " upgraded to level " + newLevel + ".");
    }

    @Override
    public void showPlantPurchased(String plantName) {
        displayMessage("Successfully purchased " + plantName + ".");
    }

    @Override
    public void errorPlantNotFound(String plantName) {
        displayError("Plant '" + plantName + "' not found.");
    }

    @Override
    public void errorZombieNotFound(String zombieName) {
        displayError("Zombie '" + zombieName + "' not found.");
    }

    @Override
    public void errorZombieNotSeen(String zombieName) {
        displayError("Zombie '" + zombieName + "' has not been seen yet.");
    }

    @Override
    public void errorPlantNotOwned(String plantName) {
        displayError("You do not own " + plantName + ".");
    }

    @Override
    public void errorAlreadyOwned(String plantName) {
        displayError("You already own " + plantName + ".");
    }

    @Override
    public void errorMaxLevel(String plantName) {
        displayError(plantName + " is already at maximum level.");
    }

    @Override
    public void errorNotEnoughCoinsForUpgrade() {
        displayError("You don't have enough coins to upgrade.");
    }

    @Override
    public void errorNotEnoughSeedPacketsForUpgrade() {
        displayError("You don't have enough seed packets to upgrade.");
    }

    @Override
    public void errorNotEnoughCoinsToPurchase() {
        displayError("You don't have enough coins to purchase.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid collection command.");
    }
}
