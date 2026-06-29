package model.game.entity.zombie.behavior;

import model.game.entity.zombie.Armor;
import model.game.entity.GameContext;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

public final class DamageReactionBehavior implements ZombieBehavior {

    private final Trigger trigger;
    private final double healthThreshold;
    private final double speedMultiplier;
    private boolean triggered = false;

    public DamageReactionBehavior(Trigger trigger, double healthThreshold, double speedMultiplier) {
        this.trigger = trigger;
        this.healthThreshold = healthThreshold;
        this.speedMultiplier = speedMultiplier;
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
            zombie.setCurrentSpeed(zombie.getCurrentSpeed() * speedMultiplier);
        }
    }

    public enum Trigger {ARMOR_DESTROYED, HEALTH_BELOW_RATIO}
}