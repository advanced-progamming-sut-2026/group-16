package io.github.finalwave.network.match;

public final class MatchUserEntry {
    private String username;
    private boolean online;
    private boolean busy;

    public MatchUserEntry() {
    }

    public MatchUserEntry(String username, boolean online, boolean busy) {
        this.username = username;
        this.online = online;
        this.busy = busy;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }
}
