package view.cli;

import view.api.PlantSelectionView;

import java.util.List;

public class PlantSelectionViewCli extends CliView implements PlantSelectionView {

    @Override
    public void showAllPlants(List<String> plants) {
        displayMessage("All plants:");
        for (String plant : plants) {
            displayMessage("- " + plant);
        }
    }

    @Override
    public void showAvailablePlants(List<String> plants) {
        displayMessage("Available plants for this level:");
        for (String plant : plants) {
            displayMessage("- " + plant);
        }
    }

    @Override
    public void showSelectedPlants(List<String> plants) {
        displayMessage("Selected loadout (" + plants.size() + "):");
        for (String plant : plants) {
            displayMessage("- " + plant);
        }
    }

    @Override
    public void showPlantAdded(String type) {
        displayMessage("Added " + type + " to loadout.");
    }

    @Override
    public void showPlantRemoved(String type) {
        displayMessage("Removed " + type + " from loadout.");
    }

    @Override
    public void showPlantBoosted(String type) {
        displayMessage("Boosted " + type + " for this level (2 diamonds).");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: plant-selection");
    }

    @Override
    public void showGameStarted() {
        displayMessage("The Game Started!");
    }

    @Override
    public void errorPlantNotFound(String type) {
        displayError("Plant " + type + " does not exist.");
    }

    @Override
    public void errorPlantAlreadySelected(String type) {
        displayError("Plant " + type + " is already selected.");
    }

    @Override
    public void errorPlantNotSelected(String type) {
        displayError("Plant " + type + " is not selected to be removed.");
    }

    @Override
    public void errorPlantLocked(String type) {
        displayError("Plant " + type + " is locked.");
    }

    @Override
    public void errorSunProducerBanned(String type) {
        displayError("Sun producers like Sunflower are not allowed in Plant What You Get.");
    }

    @Override
    public void errorLoadoutFull(int maxSlots) {
        displayError("Loadout is full (max " + maxSlots + " plants).");
    }

    @Override
    public void errorCannotBoostPlant(String type) {
        displayError("Cannot boost plant " + type + ".");
    }

    @Override
    public void errorNotEnoughDiamonds() {
        displayError("You don't have enough diamonds to boost this plant.");
    }

    @Override
    public void errorLoadoutEmpty() {
        displayError("Select at least one plant before starting the game.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid plant selection command.");
    }
}
