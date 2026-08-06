package io.github.finalwave.view.api;

import java.util.List;

public interface CollectionView extends View {
    void showCurrentMenu();

    void showPlantList(List<String> lines);

    void showAllPlants(List<String> lines);

    void showZombieList(List<String> lines);

    void showAllZombies(List<String> lines);

    void showPlantDetails(String details);

    void showZombieDetails(String details);

    void showPlantUpgraded(String plantName, int newLevel);

    void showPlantPurchased(String plantName);

    void errorPlantNotFound(String plantName);

    void errorZombieNotFound(String zombieName);

    void errorZombieNotSeen(String zombieName);

    void errorPlantNotOwned(String plantName);

    void errorAlreadyOwned(String plantName);

    void errorMaxLevel(String plantName);

    void errorNotEnoughCoinsForUpgrade();

    void errorNotEnoughSeedPacketsForUpgrade();

    void errorNotEnoughCoinsToPurchase();

    void errorInvalidCommand();
}
