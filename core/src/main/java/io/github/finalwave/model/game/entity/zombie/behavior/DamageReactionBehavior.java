package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class DamageReactionBehavior implements ZombieBehavior {

    private final Trigger trigger;
    private final double healthThreshold;
    private final double speedMultiplier;
    private final double eatingDamageMultiplier;
    private boolean triggered = false;

    public DamageReactionBehavior(Trigger trigger, double healthThreshold, double speedMultiplier) {
        this(trigger, healthThreshold, speedMultiplier, 1.0);
    }

    public DamageReactionBehavior(Trigger trigger, double healthThreshold,
                                  double speedMultiplier, double eatingDamageMultiplier) {
        this.trigger = trigger;
        this.healthThreshold = healthThreshold;
        this.speedMultiplier = speedMultiplier;
        this.eatingDamageMultiplier = eatingDamageMultiplier;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (triggered) return;

        boolean shouldTrigger = switch (trigger) {
            case ARMOR_DESTROYED -> zombie.getArmorLayers().stream().allMatch(Armor::isDestroyed);
            case HEALTH_BELOW_RATIO -> zombie.getHealthRatio() <= healthThreshold;
        };

        if (shouldTrigger) {
            triggered = true;
            zombie.multiplySpeed(speedMultiplier);
            zombie.multiplyEatingDamage(eatingDamageMultiplier);
            if (trigger == Trigger.ARMOR_DESTROYED) {
                zombie.beginAbility("newspaper_defeat", 12);
            }
        }
    }

    public enum Trigger {ARMOR_DESTROYED, HEALTH_BELOW_RATIO}
}