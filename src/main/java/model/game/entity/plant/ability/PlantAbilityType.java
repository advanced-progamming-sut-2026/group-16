package model.game.entity.plant.ability;

public enum PlantAbilityType {
    PRODUCE_SUN,
    INSTANT_SUN_BURST,
    SHOOT_PROJECTILE,
    MELEE_ATTACK,
    DELAYED_EXPLOSIVE,
    INSTANT_EXPLOSIVE,
    PASSIVE_SHIELD,
    MODIFIER_UTILITY,
    MINT_FAMILY_BOOST;

    public static PlantAbilityType fromDefinition(String value) {
        return PlantAbilityType.valueOf(value);
    }
}
