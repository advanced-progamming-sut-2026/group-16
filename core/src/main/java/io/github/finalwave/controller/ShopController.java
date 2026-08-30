package io.github.finalwave.controller;

import io.github.finalwave.model.command.ShopMenuCommands;
import io.github.finalwave.model.shop.ShopManager;
import io.github.finalwave.model.shop.ShopOffer;
import io.github.finalwave.model.shop.ShopTab;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.ShopView;

import java.util.List;
import java.util.regex.Matcher;

public class ShopController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final ShopManager shopManager;

    public ShopController(User user, UserDatabase userDatabase) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.shopManager = new ShopManager();
    }

    public User getUser() {
        return user;
    }

    public void back() {
        navigator.pop();
    }

    public List<ShopOffer> offers() {
        List<ShopOffer> offers = shopManager.offers(user);
        userDatabase.saveUserWallet(user);
        return offers;
    }

    public List<ShopOffer> offers(ShopTab tab) {
        List<ShopOffer> offers = shopManager.offers(user, tab);
        userDatabase.saveUserWallet(user);
        return offers;
    }

    public List<String> unlockedPlantNames() {
        return user.getPlantProgress().getUnlockedPlantNames();
    }

    public void buy(String itemId, int count, String plantType) {
        applyPurchase(shopManager.purchase(user, itemId, count, plantType));
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
        navigator.pop();
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

        buy(itemId, itemCount, plantType);
    }

    private void applyPurchase(ShopManager.PurchaseResult result) {
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
                if (result.extraInfo() != null && !result.extraInfo().isBlank()) {
                    userDatabase.savePlant(user, result.extraInfo());
                }
                userDatabase.saveStoredBoosts(user);
                getShopView().showItemPurchased(result.itemName(), result.count(), result.extraInfo());
            }
        }
    }

    private ShopView getShopView() {
        return (ShopView) view;
    }
}
