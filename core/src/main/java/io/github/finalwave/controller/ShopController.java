package io.github.finalwave.controller;

import io.github.finalwave.model.command.ShopMenuCommands;
import io.github.finalwave.model.shop.ShopManager;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.ShopView;

import java.util.regex.Matcher;

public class ShopController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final GreenhouseController greenhouseController;
    private final ShopManager shopManager;

    public ShopController(User user, UserDatabase userDatabase, GreenhouseController greenhouseController) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.greenhouseController = greenhouseController;
        this.shopManager = new ShopManager();
    }

    @Override
    public void displayMenu() {
        getShopView().showCurrentMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (ShopMenuCommands cmd : ShopMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null)
                continue;

            switch (cmd) {
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case SHOP_LIST -> handleShopList();
                case SHOP_DAILY -> handleShopDaily();
                case SHOP_BUY -> handleShopBuy(matcher.group("itemId"), matcher.group("count"),
                        matcher.group("plantType"));
            }
            return;
        }
        getShopView().errorInvalidShopCommand();
    }

    private void handleShowCurrent() {
        getShopView().showCurrentMenu();
    }

    private void handleMenuExit() {
        parser.switchController(greenhouseController);
    }

    private void handleShopList() {
        getShopView().showShopList(shopManager.formatPermanentItems());
    }

    private void handleShopDaily() {
        shopManager.refreshDailyOfferIfNeeded(user);
        userDatabase.saveUserWallet(user);
        getShopView().showDailyOffer(shopManager.formatDailyOffer(user));
    }

    private void handleShopBuy(String itemId, String count, String plantType) {
        int itemCount;
        try {
            itemCount = Integer.parseInt(count.trim());
        } catch (NumberFormatException e) {
            getShopView().errorInvalidBuyCount();
            return;
        }

        ShopManager.PurchaseResult result = shopManager.purchase(user, itemId, itemCount, plantType);
        switch (result.status()) {
            case "invalid_count" -> getShopView().errorInvalidBuyCount();
            case "item_not_found" -> getShopView().errorItemNotFound(result.errorArg());
            case "insufficient_coins" -> getShopView().errorInsufficientCoins();
            case "insufficient_diamonds" -> getShopView().errorInsufficientDiamonds();
            case "daily_purchased" -> getShopView().errorDailyOfferAlreadyPurchased();
            case "max_capacity" -> getShopView().errorMaxCapacityReached(result.itemName());
            case "plant_type_required" -> getShopView().errorPlantTypeRequired();
            case "plant_not_unlocked" -> getShopView().errorPlantNotUnlocked(result.errorArg());
            default -> {
                userDatabase.saveUserWallet(user);
                userDatabase.savePlantProgress(user);
                getShopView().showItemPurchased(result.itemName(), result.count(), result.extraInfo());
            }
        }
    }

    private ShopView getShopView() {
        return (ShopView) view;
    }
}
