package io.github.finalwave.model.scoregame;

import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.Gender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreGameSubmissionTest {

    @Test
    void firstSubmissionSetsHasPlayedAndScore() {
        User user = freshUser();

        ScoreGameSubmission.Result result = ScoreGameSubmission.apply(user, 42);

        assertTrue(result.hasPlayed());
        assertTrue(result.newBest());
        assertEquals(42, result.bestMeowPoint());
        assertTrue(user.hasPlayed());
        assertEquals(42, user.getBestMeowPoint());
    }

    @Test
    void firstSubmissionWithZeroStillSetsHasPlayed() {
        User user = freshUser();

        ScoreGameSubmission.Result result = ScoreGameSubmission.apply(user, 0);

        assertTrue(result.hasPlayed());
        assertTrue(result.newBest());
        assertEquals(0, result.bestMeowPoint());
        assertTrue(user.hasPlayed());
        assertEquals(0, user.getBestMeowPoint());
    }

    @Test
    void lowerRepeatScoreLeavesBestUnchanged() {
        User user = freshUser();
        ScoreGameSubmission.apply(user, 50);

        ScoreGameSubmission.Result result = ScoreGameSubmission.apply(user, 30);

        assertTrue(result.hasPlayed());
        assertFalse(result.newBest());
        assertEquals(50, result.bestMeowPoint());
        assertEquals(50, user.getBestMeowPoint());
    }

    @Test
    void higherScoreUpdatesBest() {
        User user = freshUser();
        ScoreGameSubmission.apply(user, 50);

        ScoreGameSubmission.Result result = ScoreGameSubmission.apply(user, 80);

        assertTrue(result.hasPlayed());
        assertTrue(result.newBest());
        assertEquals(80, result.bestMeowPoint());
        assertEquals(80, user.getBestMeowPoint());
    }

    @Test
    void negativeScoreClampsToZeroOnFirstPlay() {
        User user = freshUser();

        ScoreGameSubmission.Result result = ScoreGameSubmission.apply(user, -5);

        assertEquals(0, result.bestMeowPoint());
        assertEquals(0, user.getBestMeowPoint());
    }

    private static User freshUser() {
        return new User("score-user", "hash", "Nick", "score@example.com", Gender.MALE);
    }
}
