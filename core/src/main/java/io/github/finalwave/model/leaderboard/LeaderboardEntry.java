package io.github.finalwave.model.leaderboard;

public record LeaderboardEntry(
        String username,
        String progressLabel,
        int progressSortKey,
        int minigameCount,
        long dailyQuestCount,
        long nonDailyQuestCount,
        Integer bestScore)
{}
