package io.github.finalwave.model.user;

import io.github.finalwave.model.quest.Quest;
import io.github.finalwave.model.quest.QuestTracker;
import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

public final class QuestProgressStore {
    private QuestProgressStore() {
    }

    public static void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS quest_progress (
                    userId INTEGER NOT NULL,
                    questId TEXT NOT NULL,
                    completed INTEGER NOT NULL DEFAULT 0 CHECK(completed IN (0, 1)),
                    claimed INTEGER NOT NULL DEFAULT 0 CHECK(claimed IN (0, 1)),
                    progressBlob TEXT,
                    updatedAt TEXT NOT NULL,
                    PRIMARY KEY (userId, questId),
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create quest_progress table.", e);
        }
    }

    public static void loadQuestProgress(Connection conn, User user, QuestTracker tracker)
            throws SQLException {
        if (user == null || tracker == null) {
            return;
        }
        String sql = """
                SELECT questId, completed, claimed, progressBlob
                FROM quest_progress
                WHERE userId = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String questId = rs.getString("questId");
                    boolean completed = rs.getInt("completed") == 1;
                    boolean claimed = rs.getInt("claimed") == 1;
                    String blob = rs.getString("progressBlob");
                    for (Quest quest : tracker.getQuests()) {
                        if (quest.getId().equals(questId)) {
                            quest.restoreState(completed, claimed, blob);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void saveQuestProgress(Connection conn, User user, QuestTracker tracker)
            throws SQLException {
        if (user == null || tracker == null) {
            return;
        }
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM quest_progress WHERE userId = ?")) {
            delete.setLong(1, user.getId());
            delete.executeUpdate();
        }
        String insert = """
                INSERT INTO quest_progress
                (userId, questId, completed, claimed, progressBlob, updatedAt)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String now = Instant.now().toString();
        try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
            for (Quest quest : tracker.getQuests()) {
                insertStmt.setLong(1, user.getId());
                insertStmt.setString(2, quest.getId());
                insertStmt.setInt(3, quest.isCompleted() ? 1 : 0);
                insertStmt.setInt(4, quest.isRewardClaimed() ? 1 : 0);
                insertStmt.setString(5, quest.exportProgressBlob());
                insertStmt.setString(6, now);
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }
}
