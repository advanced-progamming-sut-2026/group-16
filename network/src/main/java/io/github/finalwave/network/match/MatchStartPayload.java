package io.github.finalwave.network.match;

import java.util.ArrayList;
import java.util.List;

public final class MatchStartPayload {
    private String matchId;
    private String opponentUsername;
    private MatchRole yourRole;
    private int stageIndex;
    private String phase;
    private List<String> pool;
    private int slots;
    private int pickSeconds;
    private int roundSeconds;

    public MatchStartPayload() {
    }

    public MatchStartPayload(String matchId, String opponentUsername, MatchRole yourRole, int stageIndex) {
        this.matchId = matchId;
        this.opponentUsername = opponentUsername;
        this.yourRole = yourRole;
        this.stageIndex = stageIndex;
        this.phase = "picking";
        this.pickSeconds = 45;
        this.roundSeconds = 120;
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

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public List<String> getPool() {
        return pool;
    }

    public void setPool(List<String> pool) {
        this.pool = pool == null ? null : new ArrayList<>(pool);
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getPickSeconds() {
        return pickSeconds;
    }

    public void setPickSeconds(int pickSeconds) {
        this.pickSeconds = pickSeconds;
    }

    public int getRoundSeconds() {
        return roundSeconds;
    }

    public void setRoundSeconds(int roundSeconds) {
        this.roundSeconds = roundSeconds;
    }
}
