package io.github.finalwave.model.game.entity.plant.support;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;

public final class SunBeanSupport {

    private static final double BASE_SUN_PER_DAMAGE = 0.2;
    private static final double POWERED_SUN_PER_HEALTH = 0.75;

    private SunBeanSupport() {
    }

    public static void onSunBeanConsumed(Zombie zombie, Plant sunBean, GameContext context) {
        if (zombie == null || sunBean == null || context == null || !sunBean.hasTag(PlantTag.SUN)) {
            return;
        }
        if (sunBean.isSunBeanPowered()) {
            int sun = (int) Math.round(zombie.getHealth() * POWERED_SUN_PER_HEALTH);
            if (sun > 0) {
                context.spawnSunAt(sunBean.getCol(), sunBean.getRow(), sun, SunType.NORMAL);
            }
            zombie.takeDirectDamage(zombie.getHealth());
            if (zombie.isDead()) {
                context.onZombieKilled(zombie);
            }
            return;
        }
        zombie.addSunBeanInfection();
    }

    public static void onZombieDamaged(Zombie zombie, int damage, GameContext context) {
        if (zombie == null || context == null || damage <= 0 || zombie.getSunBeanInfections() <= 0) {
            return;
        }
        double earned = damage * sunPerDamage(zombie);
        int col = (int) Math.floor(zombie.getX());
        int row = zombie.getRow();
        zombie.addSunBeanBank(earned);
        while (zombie.getSunBeanBank() >= 5.0) {
            context.spawnSunAt(col, row, 5, SunType.NORMAL);
            zombie.addSunBeanBank(-5.0);
        }
    }

    private static double sunPerDamage(Zombie zombie) {
        return BASE_SUN_PER_DAMAGE * zombie.getSunBeanInfections();
    }
}
