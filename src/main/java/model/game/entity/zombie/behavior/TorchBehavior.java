package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;
import model.game.entity.projectile.Projectile;
import model.game.entity.projectile.ProjectileEffect;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

public final class TorchBehavior implements ZombieBehavior {

    private final double reach;
    private boolean lit = true;

    public TorchBehavior(double reach) {
        this.reach = reach;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (!lit) {
            return;
        }
        Plant target = context.getPlantInFront(zombie.getX(), zombie.getRow());
        if (target != null && target.canBeTargetedByZombie()
                && zombie.getX() - target.getCol() < reach
                && zombie.tryBeginAbilityAction()) {
            target.takeDamage(target.getHealth());
            context.onPlantDestroyed(target);
        }
    }

    @Override
    public boolean interceptProjectile(Zombie zombie, Projectile projectile, GameContext context) {
        if (projectile.getEffect() == ProjectileEffect.ICE) {
            lit = false;
        } else if (projectile.getEffect() == ProjectileEffect.FIRE) {
            lit = true;
        }
        return false;
    }

    public boolean isLit() {
        return lit;
    }
}
