package io.github.finalwave.view.cli;

import io.github.finalwave.model.game.LockedPlantsMode;
import io.github.finalwave.view.api.LockedPlantsView;

import java.util.List;

public class LockedPlantsLevelViewCli extends SpecialLevelViewCli implements LockedPlantsView {

    @Override
    public void showLockedPlantsSummary(LockedPlantsMode mode, List<String> locked) {
        if (mode == LockedPlantsMode.SPECIFIC) {
            displayMessage("Locked Plants (specific mode) is active.");
        } else {
            displayMessage("Locked Plants (family mode) is active.");
        }
        if (locked.isEmpty()) {
            displayMessage("No plants are locked during gameplay.");
            return;
        }
        displayMessage("Plants locked this level: " + String.join(", ", locked));
    }
}
