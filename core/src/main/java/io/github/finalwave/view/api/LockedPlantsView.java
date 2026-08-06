package io.github.finalwave.view.api;

import io.github.finalwave.model.game.LockedPlantsMode;

import java.util.List;

public interface LockedPlantsView extends SpecialLevelView {

    void showLockedPlantsSummary(LockedPlantsMode mode, List<String> locked);
}
