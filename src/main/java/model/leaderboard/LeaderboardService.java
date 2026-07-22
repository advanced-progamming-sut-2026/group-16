package model.leaderboard;

import model.user.ChapterProgress;
import model.user.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LeaderboardService {
    private LeaderboardService() {
    }

    public static List<LeaderboardEntry> build(List<User> users) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (users == null) {
            return entries;
        }
        for (User user : users) {
            if (user != null) {
                entries.add(toEntry(user));
            }
        }
        return entries;
    }

    public static List<LeaderboardEntry> sort(
            List<LeaderboardEntry> entries,
            LeaderboardSortColumn column,
            boolean ascending) {
        List<LeaderboardEntry> sorted = new ArrayList<>(
                entries == null ? List.of() : entries);
        Comparator<LeaderboardEntry> comparator = comparatorFor(column);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        sorted.sort(comparator.thenComparing(LeaderboardEntry::username));
        return sorted;
    }

    private static LeaderboardEntry toEntry(User user) {
        user.ensureQuestTracker();
        var furthest = user.getChapterProgress().furthestCompleted();
        String progressLabel = furthest
                .map(ChapterProgress.CompletedLevel::displayLabel)
                .orElse("-");
        int progressSortKey = furthest
                .map(ChapterProgress.CompletedLevel::sortKey)
                .orElse(-1);
        return new LeaderboardEntry(
                user.getUsername(),
                progressLabel,
                progressSortKey,
                user.getMiniGameProgress().completedStageCount(),
                user.ensureQuestTracker().completedDailyCount(),
                user.ensureQuestTracker().completedNonDailyCount(),
                user.getBestMeioPoint());
    }

    private static Comparator<LeaderboardEntry> comparatorFor(LeaderboardSortColumn column) {
        LeaderboardSortColumn sortColumn =
                column == null ? LeaderboardSortColumn.USERNAME : column;
        return switch (sortColumn) {
            case USERNAME -> Comparator.comparing(
                    LeaderboardEntry::username, String.CASE_INSENSITIVE_ORDER);
            case PROGRESS -> Comparator.comparingInt(LeaderboardEntry::progressSortKey);
            case MINIGAMES -> Comparator.comparingInt(LeaderboardEntry::minigameCount);
            case DAILY_QUESTS -> Comparator.comparingLong(LeaderboardEntry::dailyQuestCount);
            case NON_DAILY_QUESTS -> Comparator.comparingLong(LeaderboardEntry::nonDailyQuestCount);
            case BEST_SCORE -> Comparator.comparingInt(LeaderboardEntry::bestScore);
        };
    }
}
