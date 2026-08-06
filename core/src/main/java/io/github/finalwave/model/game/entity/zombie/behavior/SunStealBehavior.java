package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class SunStealBehavior implements ZombieBehavior {

    public enum Mode {GROUND, CHARGE_AND_LASER}

    private final Mode mode;
    private final int durationTicks;
    private final int pulseTicks;
    private final int amountPerPulse;
    private final double range;
    private final int laserColumns;
    private int chargeTicks;
    private int stolen;

    public SunStealBehavior(Mode mode, int durationTicks, int pulseTicks,
                            int amountPerPulse, double range, int laserColumns) {
        this.mode = mode;
        this.durationTicks = durationTicks;
        this.pulseTicks = Math.max(1, pulseTicks);
        this.amountPerPulse = amountPerPulse;
        this.range = range;
        this.laserColumns = laserColumns;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (mode == Mode.GROUND) {
            if (zombie.getTickAge() % pulseTicks == 0 && stolen < amountPerPulse) {
                stolen += context.stealGroundSun(amountPerPulse - stolen);
            }
            return;
        }
        if (!hasPlantInRange(zombie, context)) {
            chargeTicks = 0;
            return;
        }
        chargeTicks++;
        if (chargeTicks % pulseTicks == 0) {
            stolen += context.withdrawSun(amountPerPulse);
        }
        if (chargeTicks >= durationTicks && zombie.tryBeginAbilityAction()) {
            destroyPlantsAhead(zombie, context);
            chargeTicks = 0;
        }
    }

    @Override
    public void onDeath(Zombie zombie, GameContext context) {
        context.returnSun(mode == Mode.GROUND ? stolen : stolen / 2);
        stolen = 0;
    }

    private boolean hasPlantInRange(Zombie zombie, GameContext context) {
        for (Plant plant : context.getAllPlants()) {
            if (plant.canBeTargetedByZombie() && plant.getRow() == zombie.getRow()
                    && plant.getCol() <= zombie.getX()
                    && zombie.getX() - plant.getCol() <= range) {
                return true;
            }
        }
        return false;
    }

    private void destroyPlantsAhead(Zombie zombie, GameContext context) {
        int start = (int) Math.floor(zombie.getX());
        int end = Math.max(0, start - laserColumns);
        for (int col = start; col >= end; col--) {
            Plant plant = context.getPlantAt(col, zombie.getRow());
            if (plant != null && plant.canBeTargetedByZombie()) {
                plant.takeDamage(plant.getHealth());
                context.onPlantDestroyed(plant);
            }
        }
    }
}
