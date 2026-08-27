package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class IcePushBehavior implements ZombieBehavior {

    private final int initialIceBlocks;
    private boolean initialized;

    public IcePushBehavior(int cooldownTicks) {
        this(cooldownTicks, 0);
    }

    public IcePushBehavior(int cooldownTicks, int initialIceBlocks) {
        this.initialIceBlocks = Math.max(0, initialIceBlocks);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (!initialized) {
            context.createIceBlocks(zombie.getRow(), (int) Math.floor(zombie.getX()) - 1,
                    initialIceBlocks);
            initialized = true;
        }
        int right = rightmostIceAhead(zombie, context);
        if (right < 0) {
            zombie.setStationary(false);
            return;
        }
        zombie.setPresentationClip("push");
        if (zombie.getX() >= right + 0.9) {
            zombie.setStationary(false);
            return;
        }
        if (iceBlocked(zombie, context, right)) {
            zombie.setStationary(true);
            return;
        }
        zombie.setStationary(false);
        context.pushIceAhead(zombie);
    }

    private static int rightmostIceAhead(Zombie zombie, GameContext context) {
        int row = zombie.getRow();
        int right = -1;
        for (int col = 0; col < context.getColCount(); col++) {
            var tile = context.getTileAt(col, row);
            if (tile != null && tile.isIce() && col < zombie.getX()) {
                right = col;
            }
        }
        return right;
    }

    private static boolean iceBlocked(Zombie zombie, GameContext context, int right) {
        int left = right;
        while (left > 0) {
            var tile = context.getTileAt(left - 1, zombie.getRow());
            if (tile == null || !tile.isIce()) {
                break;
            }
            left--;
        }
        int dest = left - 1;
        if (dest < 0) {
            return true;
        }
        var destination = context.getTileAt(dest, zombie.getRow());
        if (destination == null || destination.isWater()
                || destination.isGrave() || destination.isIce()
                || destination.blocksPlanting()) {
            return true;
        }
        var plant = context.getPlantAt(dest, zombie.getRow());
        return plant != null && !plant.canBeTargetedByZombie();
    }
}
