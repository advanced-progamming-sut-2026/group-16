package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class PlantAbilityFactory {

    private PlantAbilityFactory() {
    }

    public static PlantAbility create(PlantDefinition definition, PlantCategory category) {
        PlantAbilityType abilityType = PlantAbilityType.fromDefinition(definition.getAbilityType());
        String name = definition.getName();
        return switch (abilityType) {
            case PRODUCE_SUN -> new SunProductionAbility(definition.getAbilityValue());
            case INSTANT_SUN_BURST -> new InstantSunBurstAbility(definition.getAbilityValue());
            case SHOOT_PROJECTILE -> createProjectileAbility(definition, category, name);
            case MELEE_ATTACK -> new MeleeAttackAbility(definition.hasTag("AOE"));
            case DELAYED_EXPLOSIVE -> new ExplosiveAbility(resolveExplosionRadius(definition), false);
            case INSTANT_EXPLOSIVE -> new ExplosiveAbility(resolveExplosionRadius(definition), true);
            case GRAVE_BUSTER -> new GraveBusterAbility();
            case PASSIVE_SHIELD -> new PassiveShieldAbility((int) definition.getAbilityValue());
            case MODIFIER_UTILITY -> new ModifierUtilityAbility(definition.getAbilityValue());
            case MINT_FAMILY_BOOST -> new MintFamilyBoostAbility(category, definition.getAbilityValue());
        };
    }

    private static PlantAbility createProjectileAbility(
            PlantDefinition definition, PlantCategory category, String name) {
        ProjectileProfile profile = resolveProjectileProfile(category, name);
        if ("Bowling Bulb".equals(name)) {
            return new BowlingBulbAbility(profile);
        }
        if ("Cactus".equals(name)) {
            return new CactusAbility((int) definition.getAbilityValue(), profile);
        }
        if ("Citron".equals(name)) {
            return new CitronAbility(profile);
        }
        return new ProjectileAttackAbility((int) definition.getAbilityValue(), profile);
    }

    private static ProjectileProfile resolveProjectileProfile(PlantCategory category, String name) {
        if ("Citron".equals(name) || "Caulipower".equals(name) || "Electric Blueberry".equals(name)) {
            return ProjectileProfile.homingProfile();
        }
        if ("Bowling Bulb".equals(name)) {
            return ProjectileProfile.straight();
        }
        return switch (category) {
            case LOBBER -> ProjectileProfile.arcing();
            case STRIKE_THROUGH -> ProjectileProfile.piercingProfile();
            case HOMING -> ProjectileProfile.homingProfile();
            default -> ProjectileProfile.straight();
        };
    }

    private static double resolveExplosionRadius(PlantDefinition definition) {
        double affectedTiles = Math.max(1.0, definition.getAbilityValue());
        double radius = Math.max(1.0, Math.floor(Math.sqrt(affectedTiles) / 2.0));
        return definition.hasTag("AOE") ? Math.max(1.5, radius) : radius;
    }
}
