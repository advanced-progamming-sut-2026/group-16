package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class LaneShiftBehavior implements ZombieBehavior {

    private final int cooldownTicks;
    private final double range;
    private int cooldown;
    private boolean shiftDown;

    public LaneShiftBehavior(int cooldownTicks, double range) {
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.range = range;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (context.lockZombieLanes()) {
            return;
        }
        if (cooldown-- > 0) {
            return;
        }
        boolean shifted = false;
        for (Zombie target : context.getAllZombies()) {
            if (target == zombie || target.isDead()
                    || Math.abs(target.getX() - zombie.getX()) > range) {
                continue;
            }
            int direction = shiftDown ? 1 : -1;
            int row = target.getRow() + direction;
            if (row < 0 || row >= context.getRowCount()) {
                row = target.getRow() - direction;
            }
            if (row >= 0 && row < context.getRowCount()) {
                target.setRow(row);
                shiftDown = !shiftDown;
                shifted = true;
            }
        }
        if (shifted) {
            zombie.beginAbility("play", 10);
        }
        cooldown = cooldownTicks;
    }
}
