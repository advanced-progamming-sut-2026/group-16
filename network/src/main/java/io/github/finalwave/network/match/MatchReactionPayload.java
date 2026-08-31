package io.github.finalwave.network.match;

public final class MatchReactionPayload {
    private String matchId;
    private String kind;
    private int index;
    private String fromUsername;

    public MatchReactionPayload() {
    }

    public MatchReactionPayload(String matchId, String kind, int index, String fromUsername) {
        this.matchId = matchId;
        this.kind = kind;
        this.index = index;
        this.fromUsername = fromUsername;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }
}
