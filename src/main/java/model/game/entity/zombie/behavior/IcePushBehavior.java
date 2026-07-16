package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

public final class IcePushBehavior implements ZombieBehavior {

    private final int cooldownTicks;
    private final int initialIceBlocks;
    private int cooldown;
    private boolean initialized;

    public IcePushBehavior(int cooldownTicks) {
        this(cooldownTicks, 0);
    }

    public IcePushBehavior(int cooldownTicks, int initialIceBlocks) {
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.initialIceBlocks = Math.max(0, initialIceBlocks);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (!initialized) {
            context.createIceBlocks(zombie.getRow(), (int) Math.floor(zombie.getX()) - 1,
                    initialIceBlocks);
            initialized = true;
        }
        if (cooldown-- > 0) {
            return;
        }
        context.pushIceInRow(zombie.getRow());
        cooldown = cooldownTicks;
    }
}
