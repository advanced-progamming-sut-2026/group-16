package io.github.finalwave.network.auth;

public final class RegisterOkPayload {
    private long userId;
    private String username;
    private String nickname;
    private String email;
    private String gender;
    private int coins;
    private int diamonds;
    private int plantFood;

    public RegisterOkPayload() {
    }

    public RegisterOkPayload(long userId, String username, String nickname, String email, String gender,
                           int coins, int diamonds, int plantFood) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.coins = coins;
        this.diamonds = diamonds;
        this.plantFood = plantFood;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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
}
