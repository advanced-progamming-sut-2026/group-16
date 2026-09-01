package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;

public final class PlantFoodEffectFactory {

    private PlantFoodEffectFactory() {
    }

    public static PlantFoodEffect create(PlantDefinition definition) {
        return create(definition, definition.getName());
    }

    public static PlantFoodEffect create(PlantDefinition definition, String plantName) {
        PlantFoodType type = PlantFoodType.fromDefinition(definition.getPlantFoodType());
        String name = plantName == null ? definition.getName() : plantName;
        double value = definition.getPlantFoodValue();
        return switch (type) {
            case SPAWN_SUN_ITEMS -> new SunBurstPlantFoodEffect(value);
            case PROJECTILE_BURST -> rapidFireOrBurst(name, value);
            case RANDOM_HYPNOTIZE -> randomTarget(name, (int) value);
            case KNOCKBACK_BLAST -> knockbackOrBurst(name);
            case GRANT_PERMANENT_ARMOR -> armorGrant(name, value);
            case SPAWN_CLONES -> cloneSpawn(name, value);
            default -> new GenericPlantFoodEffect(type, value);
        };
    }

    private static PlantFoodEffect armorGrant(String name, double value) {
        return switch (name) {
            case "Sweet Potato" -> new SweetPotatoPlantFoodEffect();
            case "Explode-o-nut" -> new ExplodeONutPlantFoodEffect(value);
            case "Pumpkin" -> new PumpkinPlantFoodEffect(value);
            case "Sun Bean" -> new SunBeanPlantFoodEffect(value);
            default -> new GenericPlantFoodEffect(PlantFoodType.GRANT_PERMANENT_ARMOR, value);
        };
    }

    private static PlantFoodEffect knockbackOrBurst(String name) {
        if ("Garlic".equals(name)) {
            return new GarlicPlantFoodEffect();
        }
        if ("Magnet-shroom".equals(name)) {
            return new MagnetShroomPlantFoodEffect();
        }
        return new GenericPlantFoodEffect(PlantFoodType.KNOCKBACK_BLAST, 0);
    }

    private static PlantFoodEffect cloneSpawn(String name, double value) {
        if ("Lily Pad".equals(name)) {
            return new LilyPadPlantFoodEffect(value);
        }
        return new GenericPlantFoodEffect(PlantFoodType.SPAWN_CLONES, value);
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
            return new CitronPlantFoodEffect((int) value);
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
        if ("Torchwood".equals(name)) {
            return new TorchwoodPlantFoodEffect();
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
        if ("Hypno-shroom".equals(name)) {
            return new HypnoShroomPlantFoodEffect();
        }
        return new GenericPlantFoodEffect(PlantFoodType.RANDOM_HYPNOTIZE, value);
    }
}
