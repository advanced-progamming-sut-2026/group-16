package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class PlantAbilityFactory {

    private PlantAbilityFactory() {
    }

    public static PlantAbility create(PlantDefinition definition, PlantCategory category) {
        if ("Squash".equals(definition.getName())) {
            return new SquashAbility();
        }
        if ("Grapeshot".equals(definition.getName())) {
            return new GrapeshotAbility(resolveExplosionRadius(definition));
        }
        if ("Jalapeno".equals(definition.getName())) {
            return new JalapenoAbility();
        }
        if ("Doom-shroom".equals(definition.getName())) {
            return new DoomShroomAbility();
        }
        if ("Tangle Kelp".equals(definition.getName())) {
            return new TangleKelpAbility();
        }
        if ("Iceberg Lettuce".equals(definition.getName())) {
            return new IcebergLettuceAbility();
        }
        if ("Bonk Choy".equals(definition.getName())) {
            return new BonkChoyAbility();
        }
        if ("Wasabi Whip".equals(definition.getName())) {
            return new WasabiWhipAbility();
        }
        if ("Phat Beet".equals(definition.getName())) {
            return new PhatBeetAbility();
        }
        if ("Kiwibeast".equals(definition.getName())) {
            return new KiwibeastAbility();
        }
        if ("Endurian".equals(definition.getName())) {
            return new EndurianAbility();
        }
        if ("Chomper".equals(definition.getName())) {
            return new ChomperAbility();
        }
        PlantAbilityType abilityType = PlantAbilityType.fromDefinition(definition.getAbilityType());
        String name = definition.getName();
        return switch (abilityType) {
            case PRODUCE_SUN -> new SunProductionAbility(definition.getAbilityValue());
            case INSTANT_SUN_BURST -> new InstantSunBurstAbility(definition.getAbilityValue());
            case SHOOT_PROJECTILE -> createProjectileAbility(definition, category, name);
            case MELEE_ATTACK -> new MeleeAttackAbility(definition.hasTag("AOE"));
            case DELAYED_EXPLOSIVE -> new ExplosiveAbility(resolveExplosionRadius(definition), false);
            case INSTANT_EXPLOSIVE -> createInstantExplosive(definition, name);
            case GRAVE_BUSTER -> new GraveBusterAbility();
            case PASSIVE_SHIELD -> createPassiveShield(definition);
            case MODIFIER_UTILITY -> createModifierUtility(definition, name);
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

    private static PlantAbility createPassiveShield(PlantDefinition definition) {
        String name = definition.getName();
        if ("Sweet Potato".equals(name)) {
            return new SweetPotatoAbility();
        }
        if ("Explode-o-nut".equals(name)) {
            return new PassiveShieldAbility(0);
        }
        return new PassiveShieldAbility((int) definition.getAbilityValue());
    }

    private static PlantAbility createModifierUtility(PlantDefinition definition, String name) {
        if ("Magnet-shroom".equals(name)) {
            return new MagnetShroomAbility();
        }
        if ("Imitater".equals(name)) {
            return new ImitaterAbility();
        }
        return new ModifierUtilityAbility(definition.getAbilityValue());
    }

    private static PlantAbility createInstantExplosive(PlantDefinition definition, String name) {
        if ("Ice-shroom".equals(name)) {
            return new IceShroomAbility();
        }
        return new ExplosiveAbility(resolveExplosionRadius(definition), true);
    }
}
