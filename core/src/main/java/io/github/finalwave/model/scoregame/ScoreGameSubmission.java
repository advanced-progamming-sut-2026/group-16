package io.github.finalwave.model.scoregame;

import io.github.finalwave.model.user.User;

public final class ScoreGameSubmission {
    private ScoreGameSubmission() {
    }

    public static Result apply(User user, int score) {
        if (user == null) {
            return new Result(0, false, false);
        }
        int incoming = Math.max(0, score);
        if (!user.hasPlayed()) {
            user.setHasPlayed(true);
            user.setBestMeowPoint(incoming);
            return new Result(incoming, true, true);
        }
        if (incoming > user.getBestMeowPoint()) {
            user.setBestMeowPoint(incoming);
            return new Result(incoming, true, true);
        }
        return new Result(user.getBestMeowPoint(), false, true);
    }

    public record Result(int bestMeowPoint, boolean newBest, boolean hasPlayed) {
    }
}
