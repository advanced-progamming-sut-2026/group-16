package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.projectile.Projectile;
import model.game.entity.projectile.ProjectileEffect;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

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
        if (!zombie.tryBeginAbilityAction()) {
            return;
        }
        triggered = true;
        zombie.setPosition(0.0, zombie.getRow());
        zombie.setMovingRight(true);
    }

    @Override
    public boolean interceptProjectile(Zombie zombie, Projectile projectile, GameContext context) {
        if (!triggered && projectile.getEffect() == ProjectileEffect.ICE) {
            extinguished = true;
        }
        return false;
    }
}
