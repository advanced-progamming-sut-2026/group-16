package io.github.finalwave.model.game.entity.plant;

public enum PlantCategory {
    SUN_PRODUCER,
    SHOOTER,
    LOBBER,
    EXPLOSIVE,
    MELEE,
    WALL_NUT,
    MODIFIER,
    STRIKE_THROUGH,
    HOMING;

    public static PlantCategory fromDefinition(String value) {
        return PlantCategory.valueOf(value);
    }
}
