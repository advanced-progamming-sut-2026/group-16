package io.github.finalwave.view.api;

import java.util.List;

public interface PlantSelectionView extends View {

    void showAllPlants(List<String> plants);

    void showAvailablePlants(List<String> plants);

    void showSelectedPlants(List<String> plants);

    void showPlantAdded(String type);

    void showPlantRemoved(String type);

    void showPlantBoosted(String type);

    void showPlantUpgraded(String type, int newLevel);

    void showCurrentMenu();

    void showGameStarted();

    void errorPlantNotFound(String type);

    void errorPlantAlreadySelected(String type);

    void errorPlantNotSelected(String type);

    void errorPlantLocked(String type);

    void errorSunProducerBanned(String type);

    void errorLoadoutFull(int maxSlots);

    void errorCannotBoostPlant(String type);

    void errorNotEnoughDiamonds();

    void errorLoadoutEmpty();

    void errorUpgradeFailed(String message);

    void errorInvalidCommand();
}
