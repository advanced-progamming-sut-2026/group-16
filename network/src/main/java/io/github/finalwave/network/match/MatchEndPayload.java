package io.github.finalwave.network.match;

public final class MatchEndPayload {
    private String matchId;
    private MatchWinner winner;
    private MatchEndReason reason;

    public MatchEndPayload() {
    }

    public MatchEndPayload(String matchId, MatchWinner winner, MatchEndReason reason) {
        this.matchId = matchId;
        this.winner = winner;
        this.reason = reason;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public MatchWinner getWinner() {
        return winner;
    }

    public void setWinner(MatchWinner winner) {
        this.winner = winner;
    }

    public MatchEndReason getReason() {
        return reason;
    }

    public void setReason(MatchEndReason reason) {
        this.reason = reason;
    }
}
