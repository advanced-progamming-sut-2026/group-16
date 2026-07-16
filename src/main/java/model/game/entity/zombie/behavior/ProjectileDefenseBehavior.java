package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.projectile.Projectile;
import model.game.entity.projectile.ProjectileProfile;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

public final class ProjectileDefenseBehavior implements ZombieBehavior {

    public enum Mode {JESTER, SNORKEL, BOUNCE_LOBBERS}

    private final Mode mode;
    private final double spinSpeedMultiplier;
    private int spinTicks;
    private boolean speedApplied;

    public ProjectileDefenseBehavior(Mode mode, double spinSpeedMultiplier) {
        this.mode = mode;
        this.spinSpeedMultiplier = Math.max(0.01, spinSpeedMultiplier);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (mode == Mode.SNORKEL) {
            int col = (int) Math.floor(zombie.getX());
            zombie.setSubmerged(context.isWaterAt(col, zombie.getRow()));
        }
        if (spinTicks > 0) {
            spinTicks--;
            if (spinTicks == 0 && speedApplied) {
                zombie.multiplySpeed(1.0 / spinSpeedMultiplier);
                speedApplied = false;
            }
        }
    }

    @Override
    public boolean interceptProjectile(Zombie zombie, Projectile projectile, GameContext context) {
        boolean direct = projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.STRAIGHT;
        if (mode == Mode.SNORKEL) {
            return zombie.isSubmerged() && direct;
        }
        if (mode == Mode.BOUNCE_LOBBERS) {
            if (!direct) {
                reflect(zombie, projectile, context);
                return true;
            }
            return false;
        }
        if (direct) {
            spinTicks = Math.max(spinTicks, 10);
            if (!speedApplied) {
                zombie.multiplySpeed(spinSpeedMultiplier);
                speedApplied = true;
            }
            reflect(zombie, projectile, context);
            return true;
        }
        return false;
    }

    private void reflect(Zombie zombie, Projectile projectile, GameContext context) {
        context.reflectProjectile(zombie, projectile);
    }

    public boolean isSpinning() {
        return spinTicks > 0;
    }
}
