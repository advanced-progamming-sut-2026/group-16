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
    GOO,
    MEGA_GATLING_PEA,
    SEA_SHROOM,
    ROTOBAGA,
    GIANT_PEA,
    PLASMA,
    PLASMA_PF,
    MAGIC_BEAM,
    LIGHTNING,
    BOWLING_CYAN,
    BOWLING_BLUE,
    BOWLING_ORANGE,
    BOWLING_PF,
    SPIKE_PF,
    STAR,
    STAR_PF,
    GOO_PF,
    GENERIC;

    public static ProjectileEffect fromString(String type) {
        if (type == null || type.isBlank()) {
            return GENERIC;
        }
        try {
            return ProjectileEffect.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
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
            case "rotobaga", "rotobaga_seed" -> ROTOBAGA;
            case "giant_pea", "giantpea" -> GIANT_PEA;
            case "plasma" -> PLASMA;
            case "magic_beam", "magic" -> MAGIC_BEAM;
            case "lightning" -> LIGHTNING;
            case "bowling_cyan" -> BOWLING_CYAN;
            case "bowling_blue" -> BOWLING_BLUE;
            case "bowling_orange" -> BOWLING_ORANGE;
            default -> GENERIC;
        };
    }
}
