package io.github.finalwave.model.game.entity.projectile;

public enum ProjectileEffect {
    PEA,
    FIRE,
    ICE,
    POISON,
    BUTTER,
    SNOWBALL,
    LASER,
    CABBAGE,
    KERNEL,
    MELON,
    WINTER_MELON,
    PEPPER,
    FUME,
    SPIKE,
    PUFF,
    GENERIC;

    public static ProjectileEffect fromString(String type) {
        if (type == null || type.isBlank()) {
            return GENERIC;
        }
        return switch (type.toLowerCase()) {
            case "pea" -> PEA;
            case "fire" -> FIRE;
            case "ice" -> ICE;
            case "snowball" -> SNOWBALL;
            case "poison" -> POISON;
            case "butter" -> BUTTER;
            case "laser" -> LASER;
            case "cabbage" -> CABBAGE;
            case "kernel", "corn" -> KERNEL;
            case "melon" -> MELON;
            case "wintermelon", "winter_melon" -> WINTER_MELON;
            case "pepper" -> PEPPER;
            case "fume" -> FUME;
            case "spike", "cactus" -> SPIKE;
            case "puff" -> PUFF;
            default -> GENERIC;
        };
    }
}
