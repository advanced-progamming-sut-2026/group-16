package view.cli;

import view.api.ConveyBeltView;

import java.util.List;

public class ConveyBeltLevelViewCli extends SpecialLevelViewCli implements ConveyBeltView {

    @Override
    public void showConveyorBelt(List<String> plantsOnBelt) {
        if (plantsOnBelt.isEmpty()) {
            displayMessage("Conveyor belt: (empty)");
            return;
        }
        displayMessage("Conveyor belt plants ready to plant: " + String.join(", ", plantsOnBelt));
    }

    @Override
    public void showConveyorBeltPlantArrived(String plantName) {
        displayMessage("The conveyor belt brought a " + plantName + "!");
    }
}
