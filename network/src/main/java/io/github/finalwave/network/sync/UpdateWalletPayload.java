package io.github.finalwave.network.sync;

public final class UpdateWalletPayload {
    private int coins;
    private int diamonds;
    private int plantFood;
    private int gamesPlayed;
    private String questDay;
    private String dailyOfferPlant;
    private String dailyOfferDate;
    private boolean dailyOfferPurchased;

    public UpdateWalletPayload() {
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = diamonds;
    }

    public int getPlantFood() {
        return plantFood;
    }

    public void setPlantFood(int plantFood) {
        this.plantFood = plantFood;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public String getQuestDay() {
        return questDay;
    }

    public void setQuestDay(String questDay) {
        this.questDay = questDay;
    }

    public String getDailyOfferPlant() {
        return dailyOfferPlant;
    }

    public void setDailyOfferPlant(String dailyOfferPlant) {
        this.dailyOfferPlant = dailyOfferPlant;
    }

    public String getDailyOfferDate() {
        return dailyOfferDate;
    }

    public void setDailyOfferDate(String dailyOfferDate) {
        this.dailyOfferDate = dailyOfferDate;
    }

    public boolean isDailyOfferPurchased() {
        return dailyOfferPurchased;
    }

    public void setDailyOfferPurchased(boolean dailyOfferPurchased) {
        this.dailyOfferPurchased = dailyOfferPurchased;
    }
}
