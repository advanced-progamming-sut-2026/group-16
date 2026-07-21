package view.cli;

import model.game.LockedPlantsMode;
import view.api.LockedPlantsSelectionView;

import java.util.List;

public class LockedPlantsSelectionViewCli extends PlantSelectionViewCli implements LockedPlantsSelectionView {

    @Override
    public void showLockedPlantsRules(LockedPlantsMode mode) {
        if (mode == LockedPlantsMode.SPECIFIC) {
            displayMessage("Locked Plants level (specific mode): some plants are locked for this level.");
            return;
        }
        displayMessage("Locked Plants level (family mode): only one plant per category can be selected.");
    }

    @Override
    public void showLockedPlants(List<String> locked) {
        if (locked.isEmpty()) {
            displayMessage("Locked for this level: (none)");
            return;
        }
        displayMessage("Locked for this level: " + String.join(", ", locked));
    }

    @Override
    public void errorPlantLockedForLevel(String type) {
        displayError("Plant " + type + " is locked for this level.");
    }
}
