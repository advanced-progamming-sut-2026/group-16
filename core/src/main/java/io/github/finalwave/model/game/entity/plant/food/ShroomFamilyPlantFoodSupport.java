package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.plant.ability.ProjectileAttackAbility;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

final class ShroomFamilyPlantFoodSupport {

    private ShroomFamilyPlantFoodSupport() {
    }

    static void resetFamilyLifespan(GameContext context, String plantName) {
        for (Plant other : context.getAllPlants()) {
            if (other == null || !other.isAlive() || !plantName.equals(other.getName())) {
                continue;
            }
            double lifespanSeconds = 60.0
                    + other.getStats().specialModifier(PlantSpecialModifiers.LIFESPAN_EXT);
            other.resetLifespanTicks((int) Math.ceil(
                    lifespanSeconds * context.getTicksPerSecond()));
        }
    }

    static void triggerFamilyShots(GameContext context, String plantName) {
        for (Plant other : context.getAllPlants()) {
            if (other == null || !other.isAlive() || !plantName.equals(other.getName())) {
                continue;
            }
            if (!ProjectileAttackAbility.hasAhead(other, context)) {
                continue;
            }
            context.spawnProjectile(
                    other,
                    other.getStats().damage(),
                    1,
                    ProjectileProfile.straight());
        }
    }
}
