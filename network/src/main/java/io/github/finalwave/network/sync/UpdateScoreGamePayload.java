package io.github.finalwave.network.sync;

public final class UpdateScoreGamePayload {
    private boolean hasPlayed;
    private int bestMeowPoint;

    public UpdateScoreGamePayload() {
    }

    public boolean isHasPlayed() {
        return hasPlayed;
    }

    public void setHasPlayed(boolean hasPlayed) {
        this.hasPlayed = hasPlayed;
    }

    public int getBestMeowPoint() {
        return bestMeowPoint;
    }

    public void setBestMeowPoint(int bestMeowPoint) {
        this.bestMeowPoint = bestMeowPoint;
    }
}
