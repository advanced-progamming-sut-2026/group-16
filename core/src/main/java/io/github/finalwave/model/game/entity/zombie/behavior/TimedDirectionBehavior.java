package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class TimedDirectionBehavior implements ZombieBehavior {

    private final int triggerTicks;
    private boolean triggered;
    private boolean extinguished;

    public TimedDirectionBehavior(int triggerTicks) {
        this.triggerTicks = Math.max(1, triggerTicks);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (triggered || extinguished || zombie.getTickAge() < triggerTicks) {
            return;
        }
        if (!zombie.beginBlastOffFlight(0.0, 8, 12, 6)) {
            return;
        }
        triggered = true;
    }

    @Override
    public boolean interceptProjectile(Zombie zombie, Projectile projectile, GameContext context) {
        if (!triggered && projectile.getEffect() == ProjectileEffect.ICE) {
            extinguished = true;
        }
        return false;
    }
}
