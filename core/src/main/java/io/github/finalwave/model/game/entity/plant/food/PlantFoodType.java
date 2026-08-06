package io.github.finalwave.model.game.entity.plant.food;

public enum PlantFoodType {
    NONE,
    SPAWN_SUN_ITEMS,
    PROJECTILE_BURST,
    RANDOM_HYPNOTIZE,
    KNOCKBACK_BLAST,
    MAP_WIDE_FREEZE,
    GRANT_PERMANENT_ARMOR,
    SPAWN_CLONES,
    PULL_UNDERWATER,
    LOCAL_AOE_ATTACK;

    public static PlantFoodType fromDefinition(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return PlantFoodType.valueOf(value);
    }
}
