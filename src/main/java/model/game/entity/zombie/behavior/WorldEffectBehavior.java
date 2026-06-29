package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;
import model.game.entity.zombie.ZombieState;

public final class WorldEffectBehavior implements ZombieBehavior {

    private final EffectTrigger trigger;
    private final double healthThreshold;
    private final int timerTicks;
    private final String effectType;
    private final int effectDurationTicks;
    private boolean fired = false;
    private int ticksElapsed = 0;

    public WorldEffectBehavior(EffectTrigger trigger, double healthThreshold,
                               int timerTicks, String effectType, int effectDurationTicks) {
        this.trigger = trigger;
        this.healthThreshold = healthThreshold;
        this.timerTicks = timerTicks;
        this.effectType = effectType;
        this.effectDurationTicks = effectDurationTicks;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (fired) return;

        boolean shouldFire = switch (trigger) {
            case ON_DEATH -> zombie.isDead();
            case ON_HEALTH_RATIO -> zombie.getHealthRatio() <= healthThreshold;
            case ON_TIMER -> {
                ticksElapsed++;
                yield ticksElapsed >= timerTicks;
            }
        };

        if (shouldFire) {
            fired = true;
            zombie.setState(ZombieState.ABILITY);
            context.applyRowEffect(zombie.getRow(), effectType, effectDurationTicks);

            if (trigger == EffectTrigger.ON_TIMER) {
                zombie.takeDamage(zombie.getHealth());
            }
        }
    }

    public enum EffectTrigger {ON_DEATH, ON_HEALTH_RATIO, ON_TIMER}
}