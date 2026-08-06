package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;

public final class ProjectileAttackAbility implements PlantAbility {

    private final int projectileCount;
    private final ProjectileProfile profile;

    public ProjectileAttackAbility(int projectileCount, ProjectileProfile profile) {
        this.projectileCount = projectileCount;
        this.profile = profile;
    }

    @Override
    public void onActionReady(Plant plant, GameContext context) {
        context.spawnProjectile(plant, plant.getStats().damage(), projectileCount, profile);
    }

    public ProjectileProfile getProfile() {
        return profile;
    }
}
