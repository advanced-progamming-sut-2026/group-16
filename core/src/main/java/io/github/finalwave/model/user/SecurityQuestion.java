package io.github.finalwave.model.user;

public enum SecurityQuestion {
    FIRST_PET(1, "What was the name of your first pet?"),
    BIRTH_CITY(2, "What city were you born in?"),
    FAVORITE_FRUIT(3, "What is your favorite fruit?");

    private final int number;
    private final String text;

    SecurityQuestion(int number, String text) {
        this.number = number;
        this.text = text;
    }

    public static SecurityQuestion fromNumber(int number) {
        for (SecurityQuestion question : values()) {
            if (question.number == number) {
                return question;
            }
        }
        return null;
    }

    public int getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }
}
