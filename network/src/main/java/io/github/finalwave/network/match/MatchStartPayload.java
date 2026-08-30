package io.github.finalwave.network.match;

public final class MatchStartPayload {
    private String matchId;
    private String opponentUsername;
    private MatchRole yourRole;
    private int stageIndex;

    public MatchStartPayload() {
    }

    public MatchStartPayload(String matchId, String opponentUsername, MatchRole yourRole, int stageIndex) {
        this.matchId = matchId;
        this.opponentUsername = opponentUsername;
        this.yourRole = yourRole;
        this.stageIndex = stageIndex;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getOpponentUsername() {
        return opponentUsername;
    }

    public void setOpponentUsername(String opponentUsername) {
        this.opponentUsername = opponentUsername;
    }

    public MatchRole getYourRole() {
        return yourRole;
    }

    public void setYourRole(MatchRole yourRole) {
        this.yourRole = yourRole;
    }

    public int getStageIndex() {
        return stageIndex;
    }

    public void setStageIndex(int stageIndex) {
        this.stageIndex = stageIndex;
    }
}
