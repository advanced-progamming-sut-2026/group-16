package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.JalapenoMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class JalapenoFireSystem {

    private final List<ScheduledBurn> pending = new ArrayList<>();
    private final List<JalapenoFireMark> fireMarks = new ArrayList<>();

    public void scheduleRowFire(Plant plant, int damage, int currentTick, int cols) {
        if (plant == null) {
            return;
        }
        int row = plant.getRow();
        int originCol = plant.getCol();
        String killer = plant.getName();
        for (int col = 0; col < cols; col++) {
            int delay = Math.abs(col - originCol) * JalapenoMuzzles.PROPAGATION_DELAY_TICKS;
            pending.add(new ScheduledBurn(row, col, currentTick + delay, damage, killer));
        }
    }

    public void tick(GameBoard board, List<Zombie> zombies, GameContext context, int currentTick) {
        if (pending.isEmpty()) {
            return;
        }
        Iterator<ScheduledBurn> iterator = pending.iterator();
        while (iterator.hasNext()) {
            ScheduledBurn burn = iterator.next();
            if (burn.activateTick() > currentTick) {
                continue;
            }
            applyBurn(burn, board, zombies, context);
            fireMarks.add(new JalapenoFireMark(burn.row(), burn.col()));
            iterator.remove();
        }
    }

    public List<JalapenoFireMark> drainFireMarks() {
        if (fireMarks.isEmpty()) {
            return List.of();
        }
        List<JalapenoFireMark> drained = List.copyOf(fireMarks);
        fireMarks.clear();
        return drained;
    }

    public void clear() {
        pending.clear();
        fireMarks.clear();
    }

    private void applyBurn(ScheduledBurn burn, GameBoard board, List<Zombie> zombies, GameContext context) {
        if (context != null) {
            context.damageIceAt(burn.col(), burn.row(), IceTile.MAX_HEALTH);
        }
        for (Zombie zombie : zombies) {
            if (zombie.isDead() || zombie.isHypnotized()) {
                continue;
            }
            if (zombie.getRow() != burn.row()) {
                continue;
            }
            if ((int) Math.floor(zombie.getX()) != burn.col()) {
                continue;
            }
            zombie.markPowderDeath();
            zombie.takeDamage(burn.damage());
            if (zombie.isDead() && context != null) {
                context.onZombieKilled(zombie);
            }
        }
    }

    private record ScheduledBurn(int row, int col, int activateTick, int damage, String killer) {
    }
}
