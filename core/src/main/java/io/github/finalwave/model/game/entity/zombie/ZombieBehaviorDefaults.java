package io.github.finalwave.model.game.entity.zombie;

import io.github.finalwave.model.definition.zombie.ZombieDefinition;

public final class ZombieBehaviorDefaults {

    public static final int TICKS_PER_SECOND = 10;
    public static final int STANDARD_COOLDOWN_TICKS = 30;
    public static final int LONG_COOLDOWN_TICKS = 50;
    public static final double STANDARD_RANGE = 4.0;
    public static final double CONTACT_RANGE = 0.6;
    public static final double STANDARD_MULTIPLIER = 2.0;

    private ZombieBehaviorDefaults() {
    }

    public static int ticks(ZombieDefinition definition, String secondsProperty, double fallbackSeconds) {
        Double seconds = definition.getExtraAsDouble(secondsProperty);
        return Math.max(1, (int) Math.round(
                (seconds != null ? seconds : fallbackSeconds) * TICKS_PER_SECOND));
    }

    public static int integer(ZombieDefinition definition, String property, int fallback) {
        Double value = definition.getExtraAsDouble(property);
        return value == null ? fallback : value.intValue();
    }

    public static double number(ZombieDefinition definition, String property, double fallback) {
        Double value = definition.getExtraAsDouble(property);
        return value == null ? fallback : value;
    }
}
