package io.github.finalwave.network.match;

public final class MatchInputPayload {
    private String matchId;
    private MatchInputAction action;
    private String alias;
    private Integer col;
    private Integer row;

    public MatchInputPayload() {
    }

    public MatchInputPayload(String matchId, MatchInputAction action, String alias, int col, int row) {
        this.matchId = matchId;
        this.action = action;
        this.alias = alias;
        this.col = col;
        this.row = row;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public MatchInputAction getAction() {
        return action;
    }

    public void setAction(MatchInputAction action) {
        this.action = action;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getCol() {
        return col;
    }

    public void setCol(Integer col) {
        this.col = col;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }
}
