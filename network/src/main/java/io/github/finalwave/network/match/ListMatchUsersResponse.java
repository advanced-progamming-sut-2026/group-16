package io.github.finalwave.network.match;

import java.util.ArrayList;
import java.util.List;

public final class ListMatchUsersResponse {
    private String selfUsername;
    private boolean selfOnline;
    private boolean selfBusy;
    private List<MatchUserEntry> users = new ArrayList<>();

    public ListMatchUsersResponse() {
    }

    public ListMatchUsersResponse(
            String selfUsername,
            boolean selfOnline,
            boolean selfBusy,
            List<MatchUserEntry> users) {
        this.selfUsername = selfUsername;
        this.selfOnline = selfOnline;
        this.selfBusy = selfBusy;
        this.users = users == null ? new ArrayList<>() : users;
    }

    public String getSelfUsername() {
        return selfUsername;
    }

    public void setSelfUsername(String selfUsername) {
        this.selfUsername = selfUsername;
    }

    public boolean isSelfOnline() {
        return selfOnline;
    }

    public void setSelfOnline(boolean selfOnline) {
        this.selfOnline = selfOnline;
    }

    public boolean isSelfBusy() {
        return selfBusy;
    }

    public void setSelfBusy(boolean selfBusy) {
        this.selfBusy = selfBusy;
    }

    public List<MatchUserEntry> getUsers() {
        return users;
    }

    public void setUsers(List<MatchUserEntry> users) {
        this.users = users == null ? new ArrayList<>() : users;
    }
}
