package view.api;

import model.game.LockedPlantsMode;

import java.util.List;

public interface LockedPlantsView extends SpecialLevelView {

    void showLockedPlantsSummary(LockedPlantsMode mode, List<String> locked);
}
