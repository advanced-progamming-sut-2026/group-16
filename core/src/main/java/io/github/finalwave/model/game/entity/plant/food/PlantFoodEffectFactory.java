package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;

public final class PlantFoodEffectFactory {

    private PlantFoodEffectFactory() {
    }

    public static PlantFoodEffect create(PlantDefinition definition) {
        PlantFoodType type = PlantFoodType.fromDefinition(definition.getPlantFoodType());
        String name = definition.getName();
        double value = definition.getPlantFoodValue();
        return switch (type) {
            case SPAWN_SUN_ITEMS -> new SunBurstPlantFoodEffect(value);
            case PROJECTILE_BURST -> rapidFireOrBurst(name, value);
            case RANDOM_HYPNOTIZE -> randomTarget(name, (int) value);
            default -> new GenericPlantFoodEffect(type, value);
        };
    }

    private static PlantFoodEffect rapidFireOrBurst(String name, double value) {
        if ("Repeater".equals(name)) {
            return new RapidFirePlantFoodEffect(true, 1, false);
        }
        if ("Rotobaga".equals(name)) {
            return new RapidFirePlantFoodEffect(true, 0, true);
        }
        if ("Peashooter".equals(name)) {
            return new RapidFirePlantFoodEffect(true, 0, false);
        }
        if ("Snow Pea".equals(name)) {
            return new IcyRapidFirePlantFoodEffect();
        }
        if ("Split Pea".equals(name)) {
            return new BidirectionalRapidFirePlantFoodEffect();
        }
        if ("Citron".equals(name)) {
            return new LaneClearPlantFoodEffect((int) value, ProjectileEffect.PLASMA_PF, 14, 45);
        }
        if ("Cactus".equals(name)) {
            return new LaneClearPlantFoodEffect(1800, ProjectileEffect.SPIKE_PF, 10, 40);
        }
        if ("Bowling Bulb".equals(name)) {
            return new BowlingBulbPlantFoodEffect();
        }
        if ("Fire Peashooter".equals(name)) {
            return new FirePeashooterPlantFoodEffect();
        }
        if ("Starfruit".equals(name)) {
            return new StarfruitPlantFoodEffect();
        }
        if ("Goo Peashooter".equals(name)) {
            return new GooPeashooterPlantFoodEffect();
        }
        if ("Mega Gatling Pea".equals(name)) {
            return new MegaGatlingPlantFoodEffect();
        }
        if ("Puff-shroom".equals(name)) {
            return new PuffShroomPlantFoodEffect();
        }
        if ("Sea-shroom".equals(name)) {
            return new SeaShroomPlantFoodEffect();
        }
        return new GenericPlantFoodEffect(PlantFoodType.PROJECTILE_BURST, value);
    }

    private static PlantFoodEffect randomTarget(String name, int value) {
        if ("Caulipower".equals(name)) {
            return new CaulipowerPlantFoodEffect(value);
        }
        if ("Electric Blueberry".equals(name)) {
            return new ElectricBlueberryPlantFoodEffect(value);
        }
        return new GenericPlantFoodEffect(PlantFoodType.RANDOM_HYPNOTIZE, value);
    }
}
