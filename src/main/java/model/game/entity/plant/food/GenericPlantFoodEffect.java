package model.game.entity.plant.food;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;

public final class GenericPlantFoodEffect implements PlantFoodEffect {

    private final PlantFoodType type;
    private final double value;

    public GenericPlantFoodEffect(PlantFoodType type, double value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        switch (type) {
            case NONE -> {
            }
            case SPAWN_SUN_ITEMS -> context.spawnSun(plant, value);
            case PROJECTILE_BURST -> context.projectileBurst(plant, value);
            case RANDOM_HYPNOTIZE -> context.hypnotizeRandomZombies(plant, (int) value);
            case KNOCKBACK_BLAST -> context.knockbackBlast(plant);
            case MAP_WIDE_FREEZE -> context.freezeAllZombies(plant, value);
            case GRANT_PERMANENT_ARMOR -> context.grantArmor(plant, (int) value);
            case SPAWN_CLONES -> context.spawnClones(plant, (int) value);
            case PULL_UNDERWATER -> context.pullUnderwater(plant, value);
            case LOCAL_AOE_ATTACK -> context.localAreaAttack(plant, value);
        }
    }
}
