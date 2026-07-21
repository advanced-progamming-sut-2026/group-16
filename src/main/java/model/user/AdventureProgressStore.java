package model.user;

import model.adventure.ChapterId;
import util.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public final class AdventureProgressStore {
    private AdventureProgressStore() {
    }

    public static void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_adventure (
                    userId INTEGER PRIMARY KEY,
                    unlockedChapter TEXT NOT NULL,
                    difficultyLevel INTEGER NOT NULL DEFAULT 3,
                    completedLevels TEXT,
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user_adventure table.", e);
        }
    }

    public static void load(Connection conn, User user) throws SQLException {
        String sql = """
                SELECT unlockedChapter, difficultyLevel, completedLevels
                FROM user_adventure WHERE userId = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                ChapterId unlocked = ChapterId.fromName(rs.getString("unlockedChapter"));
                if (unlocked != null) {
                    user.getChapterProgress().setUnlockedChapter(unlocked);
                }
                user.setDifficultyLevel(rs.getInt("difficultyLevel"));
                parseCompleted(rs.getString("completedLevels"), user.getChapterProgress());
            }
        }
    }

    public static void save(Connection conn, User user) throws SQLException {
        String sql = """
                INSERT INTO user_adventure (userId, unlockedChapter, difficultyLevel, completedLevels)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(userId) DO UPDATE SET
                    unlockedChapter = excluded.unlockedChapter,
                    difficultyLevel = excluded.difficultyLevel,
                    completedLevels = excluded.completedLevels
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setString(2, user.getChapterProgress().getUnlockedChapter().getKey());
            pstmt.setInt(3, user.getDifficultyLevel());
            pstmt.setString(4, serializeCompleted(user.getChapterProgress()));
            pstmt.executeUpdate();
        }
    }

    private static String serializeCompleted(ChapterProgress progress) {
        StringJoiner joiner = new StringJoiner(";");
        for (Map.Entry<ChapterId, Set<Integer>> entry : progress.getAllCompletedLevels().entrySet()) {
            StringJoiner levels = new StringJoiner(",");
            for (Integer level : entry.getValue()) {
                levels.add(Integer.toString(level));
            }
            joiner.add(entry.getKey().getKey() + ":" + levels);
        }
        return joiner.toString();
    }

    private static void parseCompleted(String blob, ChapterProgress progress) {
        if (blob == null || blob.isBlank()) {
            return;
        }
        for (String part : blob.split(";")) {
            if (part.isBlank()) {
                continue;
            }
            int colon = part.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            ChapterId chapter = ChapterId.fromName(part.substring(0, colon));
            if (chapter == null) {
                continue;
            }
            String levels = part.substring(colon + 1);
            if (levels.isBlank()) {
                continue;
            }
            for (String level : levels.split(",")) {
                try {
                    progress.restoreCompletedLevel(chapter, Integer.parseInt(level.trim()));
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }
    }
}
