package view.cli;

import model.game.entity.plant.Plant;
import view.api.PlantSelectionView;

import java.util.List;

public class PlantSelectionViewCli extends CliView implements PlantSelectionView {
    // show all plants command
    @Override
    public void showAllPlants(List<Plant> plants) {
        // TODO: implement after the Plant class is done.
    }

    // show available plants command
    @Override
    public void showAvailablePlants(List<Plant> plants) {
        // TODO: implement after the Plant class is done.
    }

    // add plant command errors
    // and remove plant command errors
    @Override
    public void errorPlantNotFound() {
        displayError("Could not find this plant.");
    }

    @Override
    public void errorPlantAlreadySelected() {
        displayError("This plant is already selected.");
    }

    @Override
    public void errorPlantNotSelected() {
        displayError("This plant is not selected.");
    }

    @Override
    public void showGameStarted() {
        displayMessage("The Game Started!");
    }
}
