package io.github.finalwave.view.api;

import io.github.finalwave.model.game.LockedPlantsMode;

import java.util.List;

public interface LockedPlantsSelectionView extends PlantSelectionView {

    void showLockedPlantsRules(LockedPlantsMode mode);

    void showLockedPlants(List<String> locked);

    void errorPlantLockedForLevel(String type);
}
