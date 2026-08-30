package io.github.finalwave.model.user;

import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

public final class MiniGameProgressStore {
    private MiniGameProgressStore() {
    }

    public static void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_minigame_stage_progress (
                    userId INTEGER NOT NULL,
                    minigameId TEXT NOT NULL,
                    stageIndex INTEGER NOT NULL,
                    PRIMARY KEY (userId, minigameId, stageIndex),
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user_minigame_stage_progress table.", e);
        }
    }

    public static void load(Connection conn, User user) throws SQLException {
        String sql = """
                SELECT minigameId, stageIndex
                FROM user_minigame_stage_progress
                WHERE userId = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MiniGameId id = MiniGameId.fromName(rs.getString("minigameId"));
                    if (id != null) {
                        user.getMiniGameProgress().restoreCompletedStage(id, rs.getInt("stageIndex"));
                    }
                }
            }
        }
    }

    public static void save(Connection conn, User user) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM user_minigame_stage_progress WHERE userId = ?")) {
            delete.setLong(1, user.getId());
            delete.executeUpdate();
        }
        String insertSql = """
                INSERT INTO user_minigame_stage_progress (userId, minigameId, stageIndex)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            for (Map.Entry<MiniGameId, Set<Integer>> entry
                    : user.getMiniGameProgress().getAllCompletedStages().entrySet()) {
                for (Integer stage : entry.getValue()) {
                    insert.setLong(1, user.getId());
                    insert.setString(2, entry.getKey().getKey());
                    insert.setInt(3, stage);
                    insert.addBatch();
                }
            }
            insert.executeBatch();
        }
    }

    public static void saveStageRows(
            Connection conn,
            long userId,
            java.util.List<io.github.finalwave.network.sync.UpdateMinigameStagesPayload.MinigameStageRow> rows
    ) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM user_minigame_stage_progress WHERE userId = ?")) {
            delete.setLong(1, userId);
            delete.executeUpdate();
        }
        if (rows == null || rows.isEmpty()) {
            return;
        }
        String insertSql = """
                INSERT INTO user_minigame_stage_progress (userId, minigameId, stageIndex)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            for (io.github.finalwave.network.sync.UpdateMinigameStagesPayload.MinigameStageRow row : rows) {
                if (row == null || row.getMinigameId() == null || row.getMinigameId().isBlank()) {
                    continue;
                }
                if (row.getStageIndex() < 0) {
                    continue;
                }
                insert.setLong(1, userId);
                insert.setString(2, row.getMinigameId());
                insert.setInt(3, row.getStageIndex());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }
}
