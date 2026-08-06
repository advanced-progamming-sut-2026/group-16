package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.collection.CollectionService;
import io.github.finalwave.model.collection.PlantCollection;
import io.github.finalwave.model.command.CollectionMenuCommands;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.CollectionView;

import java.util.regex.Matcher;

public class CollectionController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final GameController gameController;
    private final CollectionService collectionService;
    private final UnlockService unlockService = new UnlockService();

    public CollectionController(User user, UserDatabase userDatabase, GameController gameController) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.gameController = gameController;
        this.collectionService = CollectionService.createDefault(App.getInstance().getPlantRegistry());
    }

    @Override
    public void displayMenu() {
        getCollectionView().showCurrentMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (CollectionMenuCommands cmd : CollectionMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case SHOW_PLANTS -> handleShowPlants();
                case SHOW_ALL_PLANTS -> handleShowAllPlants();
                case SHOW_ZOMBIES -> handleShowZombies();
                case SHOW_ALL_ZOMBIES -> handleShowAllZombies();
                case SHOW_PLANT -> handleShowPlant(matcher.group("plantName").trim());
                case SHOW_ZOMBIE -> handleShowZombie(matcher.group("zombieName").trim());
                case UPGRADE_PLANT -> handleUpgradePlant(matcher.group("plantName").trim());
                case PURCHASE_PLANT -> handlePurchasePlant(matcher.group("plantName").trim());
            }
            return;
        }
        getCollectionView().errorInvalidCommand();
    }

    private void handleShowCurrent() {
        getCollectionView().showCurrentMenu();
    }

    private void handleMenuExit() {
        parser.switchController(gameController);
    }

    private void handleShowPlants() {
        getCollectionView().showPlantList(collectionService.formatOwnedPlants(user));
    }

    private void handleShowAllPlants() {
        getCollectionView().showAllPlants(collectionService.formatAllPlants(user));
    }

    private void handleShowZombies() {
        getCollectionView().showZombieList(collectionService.formatSeenZombies(user));
    }

    private void handleShowAllZombies() {
        getCollectionView().showAllZombies(collectionService.formatAllZombies(user));
    }

    private void handleShowPlant(String plantName) {
        if (!collectionService.isKnownPlant(plantName)) {
            getCollectionView().errorPlantNotFound(plantName);
            return;
        }
        getCollectionView().showPlantDetails(collectionService.formatPlantDetails(user, plantName));
    }

    private void handleShowZombie(String zombieName) {
        if (!collectionService.isKnownZombie(zombieName)) {
            getCollectionView().errorZombieNotFound(zombieName);
            return;
        }
        if (!user.getUnlockedZombies().contains(zombieName)) {
            getCollectionView().errorZombieNotSeen(zombieName);
            return;
        }
        getCollectionView().showZombieDetails(collectionService.formatZombieDetails(user, zombieName));
    }

    private void handleUpgradePlant(String plantName) {
        PlantCollection.UpgradeResult result = collectionService.upgradePlant(user, plantName);
        if (result.success()) {
            userDatabase.saveUserWallet(user);
            userDatabase.savePlantProgress(user);
            getCollectionView().showPlantUpgraded(plantName, result.newLevel());
            return;
        }
        handleUpgradeFailure(plantName, result.failure());
    }

    private void handleUpgradeFailure(String plantName, PlantCollection.UpgradeFailure failure) {
        switch (failure) {
            case UNKNOWN_PLANT -> getCollectionView().errorPlantNotFound(plantName);
            case NOT_OWNED -> getCollectionView().errorPlantNotOwned(plantName);
            case MAX_LEVEL -> getCollectionView().errorMaxLevel(plantName);
            case INSUFFICIENT_COINS -> getCollectionView().errorNotEnoughCoinsForUpgrade();
            case INSUFFICIENT_SEED_PACKETS -> getCollectionView().errorNotEnoughSeedPacketsForUpgrade();
        }
    }

    private void handlePurchasePlant(String plantName) {
        PlantCollection.PurchaseResult result = collectionService.purchasePlant(user, unlockService, plantName);
        if (result.success()) {
            userDatabase.saveUserWallet(user);
            userDatabase.savePlantProgress(user);
            if (result.newlyUnlocked()) {
                userDatabase.saveUserNews(user);
            }
            getCollectionView().showPlantPurchased(plantName);
            return;
        }
        handlePurchaseFailure(plantName, result.failure());
    }

    private void handlePurchaseFailure(String plantName, PlantCollection.PurchaseFailure failure) {
        switch (failure) {
            case UNKNOWN_PLANT -> getCollectionView().errorPlantNotFound(plantName);
            case ALREADY_OWNED -> getCollectionView().errorAlreadyOwned(plantName);
            case INSUFFICIENT_COINS -> getCollectionView().errorNotEnoughCoinsToPurchase();
        }
    }

    private CollectionView getCollectionView() {
        return (CollectionView) view;
    }
}
