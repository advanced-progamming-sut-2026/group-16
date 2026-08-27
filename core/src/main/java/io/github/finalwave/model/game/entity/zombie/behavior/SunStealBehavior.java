package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class SunStealBehavior implements ZombieBehavior {

    public enum Mode {GROUND, CHARGE_AND_LASER}

    private static final int POWER_UP_TICKS = 7;
    private static final int ATTACK_TICKS = 20;
    private static final int POWER_DOWN_TICKS = 13;
    private static final int LASER_FIRE_DELAY = 6;

    private final Mode mode;
    private final int durationTicks;
    private final int pulseTicks;
    private final int amountPerPulse;
    private final double range;
    private final int laserColumns;
    private int chargeTicks;
    private int stolen;
    private boolean awaitingPowerDown;

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
                int gained = context.stealGroundSun(zombie, amountPerPulse - stolen);
                if (gained > 0) {
                    zombie.beginAbility(stolen == 0 ? "power_up" : "power", pulseTicks);
                    stolen += gained;
                }
            }
            return;
        }
        if (!hasPlantInRange(zombie, context)) {
            chargeTicks = 0;
            awaitingPowerDown = false;
            zombie.setStationary(false);
            return;
        }
        zombie.setStationary(true);
        if (awaitingPowerDown) {
            chargeTicks++;
            zombie.setPresentationClip("power_down");
            if (chargeTicks >= POWER_DOWN_TICKS) {
                awaitingPowerDown = false;
                chargeTicks = 0;
                zombie.setStationary(false);
            }
            return;
        }
        int chargeHold = Math.max(POWER_UP_TICKS + 1, durationTicks);
        chargeTicks++;
        if (chargeTicks <= POWER_UP_TICKS) {
            zombie.setPresentationClip("power_up");
            return;
        }
        if (chargeTicks <= chargeHold) {
            zombie.setPresentationClip("power");
            if (chargeTicks % pulseTicks == 0) {
                stolen += context.withdrawSun(amountPerPulse);
            }
            return;
        }
        if (zombie.beginAbility("attack", ATTACK_TICKS)) {
            int start = (int) Math.floor(zombie.getX());
            context.fireLaneLaser(zombie.getRow(), start, laserColumns, LASER_FIRE_DELAY, zombie.getX());
            awaitingPowerDown = true;
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
}
