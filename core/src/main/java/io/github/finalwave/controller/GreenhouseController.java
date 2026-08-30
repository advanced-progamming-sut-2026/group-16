package io.github.finalwave.controller;

import io.github.finalwave.model.command.GreenhouseMenuCommands;
import io.github.finalwave.model.greenhouse.GreenhouseService;
import io.github.finalwave.model.greenhouse.GreenhouseSlotState;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.GreenhouseView;

import java.util.List;
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

    public User getUser() {
        return user;
    }

    public List<GreenhouseSlotState> slotStates() {
        return greenhouseService.slotStates(user);
    }

    public int plantableCount() {
        return greenhouseService.plantableCount(user);
    }

    public boolean cheatUnlockNextPot() {
        if (!user.isDebugMode()) {
            return false;
        }
        GreenhouseService.UnlockResult result = greenhouseService.unlockNextLockedFree(user);
        if (!"success".equals(result.status())) {
            return false;
        }
        persistWalletAndPot(result.x(), result.y());
        getGreenhouseView().showPotUnlocked(result.x(), result.y());
        return true;
    }

    @Override
    public void displayMenu() {
        getGreenhouseView().showCurrentMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (GreenhouseMenuCommands cmd : GreenhouseMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }

            switch (cmd) {
                case MENU_SHOW_CURRENT -> showCurrent();
                case MENU_EXIT -> back();
                case SHOW_GREENHOUSE -> showGreenhouse();
                case PLANT_POT_AT -> plantPot(parseCoordinate(matcher.group("x")), parseCoordinate(matcher.group("y")));
                case COLLECT -> collectPot(parseCoordinate(matcher.group("x")), parseCoordinate(matcher.group("y")));
                case GROW -> growPot(parseCoordinate(matcher.group("x")), parseCoordinate(matcher.group("y")));
                case ENTER_SHOP -> openShop();
            }
            return;
        }
        getGreenhouseView().errorInvalidCommand();
    }

    public void showCurrent() {
        getGreenhouseView().showCurrentMenu();
    }

    public void showGreenhouse() {
        getGreenhouseView().showGreenhouse(greenhouseService.formatDisplay(user));
    }

    public void back() {
        navigator.pop();
    }

    public void plantPot(int potX, int potY) {
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
                persistWalletAndPot(potX, potY);
                getGreenhouseView().showPlantPlantedInPot(potX, potY, result.plantType());
            }
        }
    }

    public void collectPot(int potX, int potY) {
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
                persistWalletAndPot(potX, potY);
                getGreenhouseView().showPotCollected(potX, potY, result.reward());
            }
        }
    }

    public void growPot(int potX, int potY) {
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
                persistWalletAndPot(potX, potY);
                getGreenhouseView().showPlantGrowthAccelerated(potX, potY, result.diamondsSpent());
            }
        }
    }

    public void unlockPot(int potX, int potY) {
        if (potX < 0 || potY < 0) {
            getGreenhouseView().errorInvalidPotLocation(potX, potY);
            return;
        }
        GreenhouseService.UnlockResult result = greenhouseService.unlock(user, potX, potY);
        switch (result.status()) {
            case "invalid_location" -> getGreenhouseView().errorInvalidPotLocation(potX, potY);
            case "already_unlocked" -> getGreenhouseView().errorPotAlreadyUnlocked(potX, potY);
            case "not_enough_diamonds" -> getGreenhouseView().errorNotEnoughDiamondsToUnlock();
            default -> {
                persistWalletAndPot(potX, potY);
                getGreenhouseView().showPotUnlocked(potX, potY);
            }
        }
    }

    public void openShop() {
        navigator.push(new ShopController(user, userDatabase));
    }

    private void persistWalletAndPot(int potX, int potY) {
        userDatabase.saveUserWallet(user);
        GreenhousePot pot = user.getPotAt(potX, potY);
        if (pot != null) {
            userDatabase.saveGreenhousePot(user, pot);
        }
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
