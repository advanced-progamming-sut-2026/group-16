package io.github.finalwave.model.user;

import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class UserSettingsStore {
    private UserSettingsStore() {
    }

    public static void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_settings (
                    userId INTEGER PRIMARY KEY,
                    gameSpeed INTEGER NOT NULL DEFAULT 1,
                    showLawnGrid INTEGER NOT NULL DEFAULT 0 CHECK(showLawnGrid IN (0, 1)),
                    debugMode INTEGER NOT NULL DEFAULT 0 CHECK(debugMode IN (0, 1)),
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user_settings table.", e);
        }
    }

    public static void load(Connection conn, User user) throws SQLException {
        if (user == null) {
            return;
        }
        String sql = """
                SELECT gameSpeed, showLawnGrid, debugMode
                FROM user_settings
                WHERE userId = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user.setGameSpeed(rs.getInt("gameSpeed"));
                    user.setShowLawnGrid(rs.getInt("showLawnGrid") == 1);
                    user.setDebugMode(rs.getInt("debugMode") == 1);
                }
            }
        }
    }

    public static void save(Connection conn, User user) throws SQLException {
        if (user == null) {
            return;
        }
        String sql = """
                INSERT INTO user_settings (userId, gameSpeed, showLawnGrid, debugMode)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(userId) DO UPDATE SET
                    gameSpeed = excluded.gameSpeed,
                    showLawnGrid = excluded.showLawnGrid,
                    debugMode = excluded.debugMode
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setInt(2, user.getGameSpeed());
            pstmt.setInt(3, user.isShowLawnGrid() ? 1 : 0);
            pstmt.setInt(4, user.isDebugMode() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }
}
