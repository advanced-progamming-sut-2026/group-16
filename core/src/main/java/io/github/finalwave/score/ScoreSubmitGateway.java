package io.github.finalwave.score;

import io.github.finalwave.network.score.SubmitScoreOkPayload;

public interface ScoreSubmitGateway {
    void submit(int score, Callback callback);

    interface Callback {
        void onSuccess(SubmitScoreOkPayload payload);

        void onFailure(String reason);
    }
}
