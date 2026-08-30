package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public final class MagnetShroomAbility implements PlantAbility {

    private static final int STEAL_ANIM_TICKS = 13;
    private static final int HELD_METAL_TICKS = 35;
    private static final int RECHARGE_TICKS = GameSession.TICKS_PER_SECOND * 10;

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        plant.primeActionCooldown();
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (plant.getMagnetBusyTicks() > 0 || plant.getMagnetStealAnimTicks() > 0) {
            return false;
        }
        int range = 1 + (int) plant.getStats().specialModifier(PlantSpecialModifiers.TILE_RANGE_EXT);
        Zombie target = findFrontZombie(plant, context, range);
        if (target == null) {
            return false;
        }
        Armor stolen = target.stripArmorViaMagnet();
        if (stolen == null) {
            return false;
        }
        plant.setMagnetStealAnimTicks(STEAL_ANIM_TICKS);
        plant.setMagnetHeldMetalAlias(stolen.getAlias());
        plant.setMagnetHeldMetalTicks(HELD_METAL_TICKS);
        plant.setMagnetBusyTicks(RECHARGE_TICKS);
        return true;
    }

    @Override
    public void onTick(Plant plant, GameContext context) {
        if (plant.getMagnetHeldMetalTicks() <= 0 || plant.getMagnetStealAnimTicks() > 0) {
            return;
        }
        plant.decrementMagnetHeldMetalTicks();
        if (plant.getMagnetHeldMetalTicks() == 0) {
            plant.setMagnetHeldMetalAlias(null);
        }
    }

    private static Zombie findFrontZombie(Plant plant, GameContext context, int range) {
        Zombie closest = null;
        double closestX = Double.MAX_VALUE;
        int row = plant.getRow();
        int col = plant.getCol();
        double maxDistance = range + 0.5;
        for (Zombie zombie : context.getZombiesInRow(row)) {
            if (!zombie.isAlive() || zombie.isHypnotized()) {
                continue;
            }
            double distance = zombie.getX() - col;
            if (distance >= -0.15 && distance <= maxDistance && zombie.getX() < closestX) {
                closest = zombie;
                closestX = zombie.getX();
            }
        }
        return closest;
    }
}
