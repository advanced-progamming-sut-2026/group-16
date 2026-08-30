package io.github.finalwave.network.score;

public final class SubmitScoreOkPayload {
    private int bestMeowPoint;
    private boolean hasPlayed;
    private boolean newBest;

    public SubmitScoreOkPayload() {
    }

    public int getBestMeowPoint() {
        return bestMeowPoint;
    }

    public void setBestMeowPoint(int bestMeowPoint) {
        this.bestMeowPoint = bestMeowPoint;
    }

    public boolean isHasPlayed() {
        return hasPlayed;
    }

    public void setHasPlayed(boolean hasPlayed) {
        this.hasPlayed = hasPlayed;
    }

    public boolean isNewBest() {
        return newBest;
    }

    public void setNewBest(boolean newBest) {
        this.newBest = newBest;
    }
}
