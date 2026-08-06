package io.github.finalwave.model.user;

public enum Gender {
    MALE,
    FEMALE;

    public static Gender fromString(String name) {
        for(Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(name)) {
                return gender;
            }
        }
//        throw new IllegalArgumentException("Gender not found");
        return null;
    }
}
