package controller;

import model.command.CollectionMenuCommands;
import view.api.CollectionView;

import java.util.regex.Matcher;

public class CollectionController extends ViewController {
    @Override
    public void handleCommand(String input) {
        for (CollectionMenuCommands cmd : CollectionMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null)
                continue;

            switch (cmd) {
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case SHOW_PLANTS -> handleShowPlants();
                case SHOW_ALL_PLANTS -> handleShowAllPlants();
                case SHOW_ZOMBIES -> handleShowZombies();
                case SHOW_ALL_ZOMBIES -> handleShowAllZombies();
                case SHOW_PLANT -> handleShowPlant(matcher.group("plantName"));
                case SHOW_ZOMBIE -> handleShowZombie(matcher.group("zombieName"));
                case UPGRADE_PLANT -> handleUpgradePlant(matcher.group("plantName"));
                case PURCHASE_PLANT -> handlePurchasePlant(matcher.group("plantName"));
            }
            return;
        }
        getCollectionView().errorInvalidCommand();
    }


    private void handleShowCurrent() {
        // TODO: implement after Collection is done.
    }

    private void handleMenuExit() {
        // TODO: implement after menu navigation is done.
    }

    private void handleShowPlants() {
        // TODO: implement after Collection is done.
    }

    private void handleShowAllPlants() {
        // TODO: implement after Collection is done.
    }

    private void handleShowZombies() {
        // TODO: implement after Collection is done.
    }

    private void handleShowAllZombies() {
        // TODO: implement after Collection is done.
    }

    private void handleShowPlant(String plantName) {
        // TODO: implement after Collection is done.
    }

    private void handleShowZombie(String zombieName) {
        // TODO: implement after Collection is done.
    }

    private void handleUpgradePlant(String plantName) {
        // TODO: implement after Collection is done.
    }

    private void handlePurchasePlant(String plantName) {
        // TODO: implement after Collection is done.
    }

    private CollectionView getCollectionView() {
        return (CollectionView) view;
    }
}
