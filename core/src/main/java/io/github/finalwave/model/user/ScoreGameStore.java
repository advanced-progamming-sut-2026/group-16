package io.github.finalwave.model.user;

import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ScoreGameStore {
    private ScoreGameStore() {
    }

    public static void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_score_game (
                    userId INTEGER PRIMARY KEY,
                    bestMeowPoint INTEGER NOT NULL DEFAULT 0,
                    hasPlayed INTEGER NOT NULL DEFAULT 0 CHECK(hasPlayed IN (0, 1)),
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            migrateLegacyColumn(stmt);
            migrateHasPlayedColumn(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user_score_game table.", e);
        }
    }

    private static void migrateLegacyColumn(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE user_score_game RENAME COLUMN bestMeioPoint TO bestMeowPoint");
        } catch (SQLException ignored) {
        }
    }

    private static void migrateHasPlayedColumn(Statement stmt) throws SQLException {
        try {
            stmt.execute("""
                    ALTER TABLE user_score_game
                    ADD COLUMN hasPlayed INTEGER NOT NULL DEFAULT 0 CHECK(hasPlayed IN (0, 1))
                    """);
        } catch (SQLException ignored) {
        }
    }

    public static void load(Connection conn, User user) throws SQLException {
        if (user == null) {
            return;
        }
        String sql = """
                SELECT bestMeowPoint, hasPlayed
                FROM user_score_game
                WHERE userId = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean hasPlayed = rs.getInt("hasPlayed") == 1;
                    user.setHasPlayed(hasPlayed);
                    if (hasPlayed) {
                        user.setBestMeowPoint(rs.getInt("bestMeowPoint"));
                    }
                }
            }
        }
    }

    public static void save(Connection conn, User user) throws SQLException {
        if (user == null) {
            return;
        }
        String sql = """
                INSERT INTO user_score_game (userId, bestMeowPoint, hasPlayed)
                VALUES (?, ?, ?)
                ON CONFLICT(userId) DO UPDATE SET
                    bestMeowPoint = excluded.bestMeowPoint,
                    hasPlayed = excluded.hasPlayed
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setInt(2, user.getBestMeowPoint());
            pstmt.setInt(3, user.hasPlayed() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }
}
