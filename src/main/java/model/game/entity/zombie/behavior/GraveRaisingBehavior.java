package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

public final class GraveRaisingBehavior implements ZombieBehavior {

    private final int graveCount;
    private final int cooldownTicks;
    private int cooldown;

    public GraveRaisingBehavior(int graveCount, int cooldownTicks) {
        this.graveCount = Math.max(1, graveCount);
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.cooldown = this.cooldownTicks;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (--cooldown > 0 || !zombie.tryBeginAbilityAction()) {
            return;
        }
        context.createGraves(graveCount);
        cooldown = cooldownTicks;
    }
}
