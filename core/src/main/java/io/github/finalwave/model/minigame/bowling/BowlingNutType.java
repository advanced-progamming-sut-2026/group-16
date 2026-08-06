package io.github.finalwave.model.minigame.bowling;

public enum BowlingNutType {
    STANDARD,
    EXPLOSIVE,
    GIANT;

    public static BowlingNutType fromPlantName(String plantName) {
        if (plantName == null) {
            return null;
        }
        return switch (plantName) {
            case "Wall-nut" -> STANDARD;
            case "Explode-o-nut" -> EXPLOSIVE;
            case "Giant Wall-nut" -> GIANT;
            default -> null;
        };
    }
}
