package io.github.finalwave.model.minigame;

import io.github.finalwave.network.match.MatchEndPayload;
import io.github.finalwave.network.match.MatchEndReason;
import io.github.finalwave.network.match.MatchWinner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NetworkedMatchEndLogicTest {

    @Test
    void nullWinnerMustNotAssignZombieWinByRoleFallback() {
        MatchEndPayload payload = new MatchEndPayload("match-1", null, MatchEndReason.NORMAL);
        assertNull(payload.getWinner());
    }

    @Test
    void zombieWinnerMeansPlantLoses() {
        MatchEndPayload payload = new MatchEndPayload(
                "match-1", MatchWinner.ZOMBIE, MatchEndReason.BRAINS_EATEN);
        assertEquals(MatchWinner.ZOMBIE, payload.getWinner());
    }

    @Test
    void forfeitUsesOpponentDisconnectedReason() {
        MatchEndPayload payload = new MatchEndPayload(
                "match-1", MatchWinner.PLANT, MatchEndReason.OPPONENT_DISCONNECTED);
        assertEquals(MatchEndReason.OPPONENT_DISCONNECTED, payload.getReason());
    }
}
