package io.github.finalwave.model.greenhouse;

import java.util.concurrent.TimeUnit;

public record GreenhouseSlotState(
        int x,
        int y,
        boolean locked,
        boolean empty,
        String plantType,
        long plantedAtMillis,
        long growthDurationMillis
) {
    public boolean isReady(long nowMillis) {
        return !locked && !empty && nowMillis >= plantedAtMillis + growthDurationMillis;
    }

    public long remainingMillis(long nowMillis) {
        if (locked || empty || isReady(nowMillis)) {
            return 0L;
        }
        return Math.max(0L, plantedAtMillis + growthDurationMillis - nowMillis);
    }

    public int accelerateCost(long nowMillis) {
        double remainingHours = remainingMillis(nowMillis) / (double) TimeUnit.HOURS.toMillis(1);
        return (int) Math.ceil(remainingHours);
    }

    public String remainingLabel(long nowMillis) {
        long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis(nowMillis));
        long hours = remainingMinutes / 60;
        long minutes = remainingMinutes % 60;
        return hours + "h " + minutes + "m";
    }
}
