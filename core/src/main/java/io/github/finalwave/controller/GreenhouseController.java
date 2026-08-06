package io.github.finalwave.controller;

import io.github.finalwave.model.command.GreenhouseMenuCommands;
import io.github.finalwave.model.greenhouse.GreenhouseService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.GreenhouseView;

import java.util.regex.Matcher;

public class GreenhouseController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final GreenhouseService greenhouseService;

    public GreenhouseController(User user, UserDatabase userDatabase) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.greenhouseService = new GreenhouseService(io.github.finalwave.model.App.getInstance().getPlantRegistry());
    }

    @Override
    public void displayMenu() {
        getGreenhouseView().showCurrentMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (GreenhouseMenuCommands cmd : GreenhouseMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null)
                continue;

            switch (cmd) {
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case SHOW_GREENHOUSE -> handleShowGreenhouse();
                case PLANT_POT_AT -> handlePlantPotAt(matcher.group("x"), matcher.group("y"));
                case COLLECT -> handleCollect(matcher.group("x"), matcher.group("y"));
                case GROW -> handleGrow(matcher.group("x"), matcher.group("y"));
                case ENTER_SHOP -> handleEnterShop();
            }
            return;
        }
        getGreenhouseView().errorInvalidCommand();
    }

    private void handleShowCurrent() {
        getGreenhouseView().showCurrentMenu();
    }

    private void handleMenuExit() {
        navigator.pop();
    }

    private void handleShowGreenhouse() {
        getGreenhouseView().showGreenhouse(greenhouseService.formatDisplay(user));
    }

    private void handlePlantPotAt(String x, String y) {
        int potX = parseCoordinate(x);
        int potY = parseCoordinate(y);
        if (potX < 0 || potY < 0) {
            getGreenhouseView().errorInvalidPotLocation(potX, potY);
            return;
        }
        GreenhouseService.PlantingResult result = greenhouseService.plant(user, potX, potY);
        switch (result.status()) {
            case "invalid_location" -> getGreenhouseView().errorInvalidPotLocation(potX, potY);
            case "locked" -> getGreenhouseView().errorPotLocked(potX, potY);
            case "occupied" -> getGreenhouseView().errorPotAlreadyOccupied(potX, potY);
            default -> {
                userDatabase.saveUserWallet(user);
                getGreenhouseView().showPlantPlantedInPot(potX, potY, result.plantType());
            }
        }
    }

    private void handleCollect(String x, String y) {
        int potX = parseCoordinate(x);
        int potY = parseCoordinate(y);
        if (potX < 0 || potY < 0) {
            getGreenhouseView().errorInvalidPotLocation(potX, potY);
            return;
        }
        GreenhouseService.CollectResult result = greenhouseService.collect(user, potX, potY);
        switch (result.status()) {
            case "invalid_location" -> getGreenhouseView().errorInvalidPotLocation(potX, potY);
            case "no_plant" -> getGreenhouseView().errorNoPlantToCollect(potX, potY);
            case "not_ready" -> getGreenhouseView().errorPlantNotReady(potX, potY);
            default -> {
                userDatabase.saveUserWallet(user);
                getGreenhouseView().showPotCollected(potX, potY, result.reward());
            }
        }
    }

    private void handleGrow(String x, String y) {
        int potX = parseCoordinate(x);
        int potY = parseCoordinate(y);
        if (potX < 0 || potY < 0) {
            getGreenhouseView().errorInvalidPotLocation(potX, potY);
            return;
        }
        GreenhouseService.GrowResult result = greenhouseService.grow(user, potX, potY);
        switch (result.status()) {
            case "invalid_location" -> getGreenhouseView().errorInvalidPotLocation(potX, potY);
            case "no_plant" -> getGreenhouseView().errorNoPlantToCollect(potX, potY);
            case "already_ready" -> getGreenhouseView().errorCannotAccelerateReadyPlant(potX, potY);
            case "not_enough_diamonds" -> getGreenhouseView().errorNotEnoughDiamondsForAccelerate();
            default -> {
                userDatabase.saveUserWallet(user);
                getGreenhouseView().showPlantGrowthAccelerated(potX, potY, result.diamondsSpent());
            }
        }
    }

    private void handleEnterShop() {
        navigator.push(new ShopController(user, userDatabase));
    }

    private int parseCoordinate(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private GreenhouseView getGreenhouseView() {
        return (GreenhouseView) view;
    }
}
