package io.github.finalwave.view.api;

public interface ShopView extends View {
    void showCurrentMenu();

    void showShopList(String display);

    void showDailyOffer(String display);

    void showItemPurchased(String itemName, int count, String extraInfo);

    void errorInsufficientCoins();

    void errorInsufficientDiamonds();

    void errorItemNotFound(String itemId);

    void errorDailyOfferAlreadyPurchased();

    void errorMaxCapacityReached(String itemName);

    void errorPlantTypeRequired();

    void errorPlantNotUnlocked(String plantType);

    void errorInvalidBuyCount();

    void errorInvalidShopCommand();
}
