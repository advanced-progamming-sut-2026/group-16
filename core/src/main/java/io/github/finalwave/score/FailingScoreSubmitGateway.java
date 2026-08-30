package io.github.finalwave.score;

public final class FailingScoreSubmitGateway implements ScoreSubmitGateway {
    private final String reason;

    public FailingScoreSubmitGateway(String reason) {
        this.reason = reason;
    }

    @Override
    public void submit(int score, Callback callback) {
        if (callback != null) {
            callback.onFailure(reason);
        }
    }
}
