package model.game.entity.plant.ability;

import model.definition.plant.PlantDefinition;
import model.game.entity.plant.PlantCategory;
import model.game.entity.projectile.ProjectileProfile;

public final class PlantAbilityFactory {

    private PlantAbilityFactory() {
    }

    public static PlantAbility create(PlantDefinition definition, PlantCategory category) {
        PlantAbilityType abilityType = PlantAbilityType.fromDefinition(definition.getAbilityType());
        return switch (abilityType) {
            case PRODUCE_SUN -> new SunProductionAbility(definition.getAbilityValue());
            case INSTANT_SUN_BURST -> new InstantSunBurstAbility(definition.getAbilityValue());
            case SHOOT_PROJECTILE -> new ProjectileAttackAbility(
                    (int) definition.getAbilityValue(), resolveProjectileProfile(category));
            case MELEE_ATTACK -> new MeleeAttackAbility(definition.hasTag("AOE"));
            case DELAYED_EXPLOSIVE -> new ExplosiveAbility(resolveExplosionRadius(definition), false);
            case INSTANT_EXPLOSIVE -> new ExplosiveAbility(resolveExplosionRadius(definition), true);
            case PASSIVE_SHIELD -> new PassiveShieldAbility((int) definition.getAbilityValue());
            case MODIFIER_UTILITY -> new ModifierUtilityAbility(definition.getAbilityValue());
            case MINT_FAMILY_BOOST -> new MintFamilyBoostAbility(category, definition.getAbilityValue());
        };
    }

    private static ProjectileProfile resolveProjectileProfile(PlantCategory category) {
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
