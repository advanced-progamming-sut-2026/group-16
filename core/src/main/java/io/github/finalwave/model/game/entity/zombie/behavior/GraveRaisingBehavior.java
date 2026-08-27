package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class GraveRaisingBehavior implements ZombieBehavior {

    private static final int POWER_HOLD_TICKS = 30;
    private final int graveCount;
    private final int cooldownTicks;
    private final int maxGraves;
    private int cooldown;

    public GraveRaisingBehavior(int graveCount, int cooldownTicks) {
        this(graveCount, cooldownTicks, 6);
    }

    public GraveRaisingBehavior(int graveCount, int cooldownTicks, int maxGraves) {
        this.graveCount = Math.max(1, graveCount);
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.maxGraves = Math.max(this.graveCount, maxGraves);
        this.cooldown = this.cooldownTicks;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (--cooldown > 0) {
            return;
        }
        if (!context.canThrowTombBones(zombie, graveCount, maxGraves)) {
            cooldown = 1;
            return;
        }
        if (!zombie.beginAbility("power", POWER_HOLD_TICKS)) {
            return;
        }
        context.throwTombBones(zombie, graveCount, maxGraves);
        cooldown = cooldownTicks;
    }
}
