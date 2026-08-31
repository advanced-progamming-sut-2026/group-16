package io.github.finalwave.network.match;

import java.util.ArrayList;
import java.util.List;

public final class MatchInputPayload {
    private String matchId;
    private MatchInputAction action;
    private String alias;
    private Integer col;
    private Integer row;
    private List<String> picks;

    public MatchInputPayload() {
    }

    public MatchInputPayload(String matchId, MatchInputAction action, String alias, int col, int row) {
        this.matchId = matchId;
        this.action = action;
        this.alias = alias;
        this.col = col;
        this.row = row;
    }

    public static MatchInputPayload guestReady(String matchId) {
        MatchInputPayload payload = new MatchInputPayload();
        payload.matchId = matchId;
        payload.action = MatchInputAction.GUEST_READY;
        return payload;
    }

    public static MatchInputPayload submitPicks(String matchId, List<String> picks) {
        MatchInputPayload payload = new MatchInputPayload();
        payload.matchId = matchId;
        payload.action = MatchInputAction.SUBMIT_PICKS;
        payload.picks = picks == null ? List.of() : new ArrayList<>(picks);
        return payload;
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

    public List<String> getPicks() {
        return picks;
    }

    public void setPicks(List<String> picks) {
        this.picks = picks;
    }
}
