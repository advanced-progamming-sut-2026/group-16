package io.github.finalwave.model.leaderboard;

import io.github.finalwave.model.user.ChapterProgress;
import io.github.finalwave.model.user.User;

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
        Comparator<LeaderboardEntry> comparator = comparatorFor(column, ascending);
        sorted.sort(comparator.thenComparing(LeaderboardEntry::username, String.CASE_INSENSITIVE_ORDER));
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
                user.hasPlayed() ? user.getBestMeowPoint() : null);
    }

    private static Comparator<LeaderboardEntry> comparatorFor(LeaderboardSortColumn column, boolean ascending) {
        LeaderboardSortColumn sortColumn =
                column == null ? LeaderboardSortColumn.USERNAME : column;
        return switch (sortColumn) {
            case USERNAME -> {
                Comparator<LeaderboardEntry> base = Comparator.comparing(
                        LeaderboardEntry::username, String.CASE_INSENSITIVE_ORDER);
                yield ascending ? base : base.reversed();
            }
            case PROGRESS -> {
                Comparator<LeaderboardEntry> base = Comparator.comparingInt(LeaderboardEntry::progressSortKey);
                yield ascending ? base : base.reversed();
            }
            case MINIGAMES -> {
                Comparator<LeaderboardEntry> base = Comparator.comparingInt(LeaderboardEntry::minigameCount);
                yield ascending ? base : base.reversed();
            }
            case DAILY_QUESTS -> {
                Comparator<LeaderboardEntry> base = Comparator.comparingLong(LeaderboardEntry::dailyQuestCount);
                yield ascending ? base : base.reversed();
            }
            case NON_DAILY_QUESTS -> {
                Comparator<LeaderboardEntry> base = Comparator.comparingLong(LeaderboardEntry::nonDailyQuestCount);
                yield ascending ? base : base.reversed();
            }
            case BEST_SCORE -> {
                Comparator<Integer> scores = ascending
                        ? Comparator.nullsLast(Comparator.naturalOrder())
                        : Comparator.nullsLast(Comparator.reverseOrder());
                yield Comparator.comparing(LeaderboardEntry::bestScore, scores);
            }
        };
    }
}
