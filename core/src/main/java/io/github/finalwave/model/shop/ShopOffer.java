package io.github.finalwave.model.shop;

public record ShopOffer(
        String id,
        String name,
        String description,
        int price,
        String currency,
        boolean daily,
        boolean soldOut,
        String remainingLabel,
        String previewPlant,
        String previewImageId,
        boolean requiresPlantType,
        int purchaseCount,
        String quantityLabel,
        ShopTab tab
) {
    public String unitName() {
        String raw = currency == null ? "" : currency.toLowerCase();
        if (raw.contains("diamond")) {
            return "diamonds";
        }
        return "coins";
    }

    public String priceLabel() {
        return price + " " + unitName();
    }

    public boolean pricedInDiamonds() {
        return "diamonds".equals(unitName());
    }
}
