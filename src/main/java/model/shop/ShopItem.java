package model.shop;

public class ShopItem {
    private final String id;
    private final String name;
    private final int price;
    private final String currency;
    private final int packetAmount;
    private final String description;

    public ShopItem(String id, String name, int price, String currency, int packetAmount, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.packetAmount = packetAmount;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public int getPacketAmount() {
        return packetAmount;
    }

    public String getDescription() {
        return description;
    }

    public String formatLine() {
        return id + " - " + name + " - " + price + " " + currency + " - " + description;
    }
}
