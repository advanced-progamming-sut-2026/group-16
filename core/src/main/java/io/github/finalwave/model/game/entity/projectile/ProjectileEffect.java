package io.github.finalwave.model.game.entity.projectile;

public enum ProjectileEffect {
    PEA,
    FIRE,
    ICE,
    POISON,
    BUTTER,
    SNOWBALL,
    LASER,
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
            default -> GENERIC;
        };
    }
}
