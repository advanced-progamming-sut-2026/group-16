package io.github.finalwave.model.game.entity.plant;

public enum PlantTag {
    DAY,
    NIGHT,
    SHROOM,
    WARM_UP,
    PEA,
    ICE,
    FIRE,
    STACK,
    CHARGE,
    MAGIC,
    POISON,
    WATER,
    AOE,
    TRAP,
    MOVE_ZOMBIE,
    SUN,
    EXPLOSIVE;

    public static PlantTag fromDefinition(String value) {
        return PlantTag.valueOf(value);
    }
}
