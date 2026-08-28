package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public final class BowlingBulbAbility implements PlantAbility {

    public static final int MUZZLE_TICKS = 4;
    public static final int RELOAD_TOTAL_TICKS = 25;
    private static final int[] AMMO_DAMAGE_MULTIPLIER = {1, 3, 5};

    private final ProjectileProfile profile;
    private int windupRemaining;

    public BowlingBulbAbility(ProjectileProfile profile) {
        this.profile = profile;
    }

    public ProjectileProfile getProfile() {
        return profile;
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (plant.getReloadTicksRemaining() > 0) {
            return false;
        }
        if (windupRemaining > 0) {
            windupRemaining--;
            if (windupRemaining == 0) {
                fire(plant, context);
                plant.setAttacking(false);
                return true;
            }
            return false;
        }
        if (!ProjectileAttackAbility.hasAhead(plant, context)) {
            return false;
        }
        windupRemaining = MUZZLE_TICKS;
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return MUZZLE_TICKS;
    }

    public static ProjectileEffect effectForAmmo(int ammo) {
        return switch (ammo) {
            case 2 -> ProjectileEffect.BOWLING_BLUE;
            case 3 -> ProjectileEffect.BOWLING_ORANGE;
            default -> ProjectileEffect.BOWLING_CYAN;
        };
    }

    public static String fireClipForAmmo(int ammo) {
        return switch (ammo) {
            case 2 -> "special2";
            case 3 -> "special3";
            default -> "special";
        };
    }

    public static String reloadClipForAmmo(int ammo) {
        return switch (ammo) {
            case 2 -> "reload2";
            case 3 -> "reload3";
            default -> "reload";
        };
    }

    public static int damageForAmmo(Plant plant, int ammo) {
        int index = Math.max(0, Math.min(AMMO_DAMAGE_MULTIPLIER.length - 1, ammo - 1));
        return plant.getStats().damage() * AMMO_DAMAGE_MULTIPLIER[index];
    }

    private void fire(Plant plant, GameContext context) {
        int ammo = plant.getBowlingAmmo();
        int damage = damageForAmmo(plant, ammo);
        context.spawnBowlingProjectile(plant, damage, effectForAmmo(ammo), profile);
        if (ammo >= 3) {
            plant.setBowlingReloading(true);
            plant.setReloadTicksRemaining(RELOAD_TOTAL_TICKS);
        } else {
            plant.setBowlingAmmo(ammo + 1);
        }
    }
}
