package view.api;

import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;

import java.util.List;

public interface CollectionView extends View {
    void showPlant(Plant plant);

    void showPlants(List<Plant> plants);

    void showAllPlants(List<Plant> plants);

    void showZombie(Zombie zombie);

    void showZombies(List<Zombie> zombies);

    void showAllZombies(List<Zombie> zombies);

    void errorNotEnoughSeedCoinForUpgrade();

    void errorNotEnoughForUpgrade();

    void errorNotEnoughSeedCoinToPurchase();

    void errorInvalidCommand();
}