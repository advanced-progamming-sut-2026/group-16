package io.github.finalwave.network.leaderboard;

public final class LeaderboardRow {
    private String username;
    private String progressLabel;
    private int progressSortKey;
    private int minigameCount;
    private long dailyQuestCount;
    private long nonDailyQuestCount;
    private Integer myPoint;

    public LeaderboardRow() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProgressLabel() {
        return progressLabel;
    }

    public void setProgressLabel(String progressLabel) {
        this.progressLabel = progressLabel;
    }

    public int getProgressSortKey() {
        return progressSortKey;
    }

    public void setProgressSortKey(int progressSortKey) {
        this.progressSortKey = progressSortKey;
    }

    public int getMinigameCount() {
        return minigameCount;
    }

    public void setMinigameCount(int minigameCount) {
        this.minigameCount = minigameCount;
    }

    public long getDailyQuestCount() {
        return dailyQuestCount;
    }

    public void setDailyQuestCount(long dailyQuestCount) {
        this.dailyQuestCount = dailyQuestCount;
    }

    public long getNonDailyQuestCount() {
        return nonDailyQuestCount;
    }

    public void setNonDailyQuestCount(long nonDailyQuestCount) {
        this.nonDailyQuestCount = nonDailyQuestCount;
    }

    public Integer getMyPoint() {
        return myPoint;
    }

    public void setMyPoint(Integer myPoint) {
        this.myPoint = myPoint;
    }
}
