package io.github.finalwave.network.score;

public final class SubmitScoreRequest {
    private int score;

    public SubmitScoreRequest() {
    }

    public SubmitScoreRequest(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
