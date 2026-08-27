package io.github.finalwave.model.game;

public record PendingGraveLanding(
        long id,
        int col,
        int row,
        int ticksRemaining,
        int ticksTotal,
        int holdTicks,
        double fromX,
        double fromY) {

    public PendingGraveLanding tickDown() {
        return new PendingGraveLanding(id, col, row, ticksRemaining - 1, ticksTotal,
                holdTicks, fromX, fromY);
    }

    public boolean inFlight() {
        return ticksRemaining > 0 && elapsed() >= holdTicks;
    }

    public double flightX() {
        return fromX + ((col + 0.5) - fromX) * progress();
    }

    public double flightY() {
        double base = fromY + (row - fromY) * linearProgress();
        return base - Math.sin(Math.PI * linearProgress()) * 1.35;
    }

    public double progress() {
        double t = linearProgress();
        return 0.5 - 0.5 * Math.cos(Math.PI * t);
    }

    private double linearProgress() {
        int fly = Math.max(1, ticksTotal - holdTicks);
        int flown = elapsed() - holdTicks;
        if (flown <= 0) {
            return 0.0;
        }
        return Math.min(1.0, flown / (double) fly);
    }

    private int elapsed() {
        return ticksTotal - ticksRemaining;
    }
}
