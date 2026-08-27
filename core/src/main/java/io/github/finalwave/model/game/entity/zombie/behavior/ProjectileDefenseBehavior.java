package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class ProjectileDefenseBehavior implements ZombieBehavior {

    public enum Mode {JESTER, SNORKEL, BOUNCE_LOBBERS}

    private static final int SPIN_TICKS = 24;

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
        if (mode == Mode.JESTER) {
            zombie.setJuggling(spinTicks > 0);
        }
        if (spinTicks > 0) {
            spinTicks--;
            if (spinTicks >= SPIN_TICKS - 4) {
                zombie.setPresentationClip("spinup");
            } else if (spinTicks <= 3) {
                zombie.setPresentationClip("spindown");
            } else if (zombie.getCurrentSpeed() > zombie.getBaseSpeed() * 0.05) {
                zombie.setPresentationClip("spin_walk");
            } else {
                zombie.setPresentationClip("spin");
            }
            if (spinTicks == 0 && speedApplied) {
                zombie.multiplySpeed(1.0 / spinSpeedMultiplier);
                speedApplied = false;
                zombie.setJuggling(false);
                zombie.setPresentationClip("idle");
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
        if (jesterCatches(projectile)) {
            spinTicks = Math.max(spinTicks, SPIN_TICKS);
            zombie.setJuggling(true);
            zombie.setPresentationClip("spinup");
            if (!speedApplied) {
                zombie.multiplySpeed(spinSpeedMultiplier);
                speedApplied = true;
            }
            reflect(zombie, projectile, context);
            return true;
        }
        return false;
    }

    private boolean jesterCatches(Projectile projectile) {
        ProjectileEffect effect = projectile.getEffect();
        if (effect == ProjectileEffect.FUME || effect == ProjectileEffect.POISON
                || effect == ProjectileEffect.LASER) {
            return false;
        }
        boolean direct = projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.STRAIGHT;
        return direct || effect == ProjectileEffect.BUTTER;
    }

    private void reflect(Zombie zombie, Projectile projectile, GameContext context) {
        context.reflectProjectile(zombie, projectile);
    }

    public boolean isSpinning() {
        return spinTicks > 0;
    }
}
