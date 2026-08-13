package io.github.finalwave.view.gui;

import io.github.finalwave.controller.ShopController;
import io.github.finalwave.view.api.ShopView;
import io.github.finalwave.view.gui.screen.ScreenRouter;


public final class ShopViewGui extends GuiViewBase implements ShopView {
    public ShopViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(ShopController controller) {
    }

    @Override
    public void showCurrentMenu() {
        router.refreshShop();
    }

    @Override
    public void showShopList(String display) {
        router.refreshShop();
    }

    @Override
    public void showDailyOffer(String display) {
        router.refreshShop();
    }

    @Override
    public void showItemPurchased(String itemName, int count, String extraInfo) {
        String message = "Successfully purchased " + count + " " + itemName;
        if (extraInfo != null && !extraInfo.isEmpty()) {
            message += " (" + extraInfo + ")";
        }
        toast(message + ".");
        router.refreshShop();
    }

    @Override
    public void errorInsufficientCoins() {
        toastError("You don't have enough coins.");
    }

    @Override
    public void errorInsufficientDiamonds() {
        toastError("You don't have enough diamonds.");
    }

    @Override
    public void errorItemNotFound(String itemId) {
        toastError("Item '" + itemId + "' not found in shop.");
    }

    @Override
    public void errorDailyOfferAlreadyPurchased() {
        toastError("You have already purchased today's daily offer.");
        router.refreshShop();
    }

    @Override
    public void errorMaxCapacityReached(String itemName) {
        toastError("You have reached the maximum capacity for " + itemName + ".");
        router.refreshShop();
    }

    @Override
    public void errorPlantTypeRequired() {
        toastError("Plant type is required for selective seed packets.");
    }

    @Override
    public void errorPlantNotUnlocked(String plantType) {
        toastError("Plant " + plantType + " is not unlocked yet.");
    }

    @Override
    public void errorInvalidBuyCount() {
        toastError("Invalid purchase count.");
    }

    @Override
    public void errorInvalidShopCommand() {
        toastError("Invalid shop command or parameters.");
    }
}
