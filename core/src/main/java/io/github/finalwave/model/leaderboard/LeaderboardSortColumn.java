package io.github.finalwave.model.leaderboard;

import java.util.Locale;

public enum LeaderboardSortColumn {
    USERNAME("username"),
    PROGRESS("progress"),
    MINIGAMES("minigames"),
    DAILY_QUESTS("daily"),
    NON_DAILY_QUESTS("nondaily"),
    BEST_SCORE("score");

    private final String key;

    LeaderboardSortColumn(String key) {
        this.key = key;
    }

    public static LeaderboardSortColumn fromKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        for (LeaderboardSortColumn column : values()) {
            if (column.key.equals(normalized)
                    || column.name().equalsIgnoreCase(raw.trim().replace('-', '_'))) {
                return column;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }
}
