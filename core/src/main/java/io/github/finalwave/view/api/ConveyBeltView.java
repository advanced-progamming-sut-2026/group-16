package io.github.finalwave.view.api;

import java.util.List;

public interface ConveyBeltView extends SpecialLevelView {

    void showConveyorBelt(List<String> plantsOnBelt);

    void showConveyorBeltPlantArrived(String plantName);
}
