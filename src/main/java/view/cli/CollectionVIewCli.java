package view.cli;

import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import view.api.CollectionView;

import java.util.List;

public class CollectionVIewCli extends CliView implements CollectionView {
    // collection show-plant command
    @Override
    public void showPlant(Plant plant) {
        // TODO: implement after the Plant class is done.
    }

    // collection show-plants command
    @Override
    public void showPlants(List<Plant> plants) {
        // TODO: implement after the Plant class is done.
    }

    // collection show-all-plants command
    @Override
    public void showAllPlants(List<Plant> plants) {
        // TODO: implement after the Plant class is done.
    }

    // collection show-zombie command
    @Override
    public void showZombie(Zombie zombie) {
        // TODO: implement after the Zombie class is done.
    }

    // collection show-zombies command
    @Override
    public void showZombies(List<Zombie> zombies) {
        // TODO: implement after the Zombie class is done.
    }

    // collection show-all-zombies command
    @Override
    public void showAllZombies(List<Zombie> zombies) {
        // TODO: implement after the Zombie class is done.
    }

    // collection upgrade-plant
    @Override
    public void errorNotEnoughSeedCoinForUpgrade() {
        displayError("You don't have enough coins to upgrade.");
    }

    @Override
    public void errorNotEnoughForUpgrade() {
        displayError("You don't have enough seed packets to upgrade.");
    }

    // collection purchase-plant
    @Override
    public void errorNotEnoughSeedCoinToPurchase() {
        displayError("You don't have enough coins to purchase.");
    }
}
