package model.user;

import util.database.DatabaseUtil;

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
                    bestMeioPoint INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user_score_game table.", e);
        }
    }

    public static void load(Connection conn, User user) throws SQLException {
        if (user == null) {
            return;
        }
        String sql = """
                SELECT bestMeioPoint
                FROM user_score_game
                WHERE userId = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user.setBestMeioPoint(rs.getInt("bestMeioPoint"));
                }
            }
        }
    }

    public static void save(Connection conn, User user) throws SQLException {
        if (user == null) {
            return;
        }
        String sql = """
                INSERT INTO user_score_game (userId, bestMeioPoint)
                VALUES (?, ?)
                ON CONFLICT(userId) DO UPDATE SET bestMeioPoint = excluded.bestMeioPoint
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setInt(2, user.getBestMeioPoint());
            pstmt.executeUpdate();
        }
    }
}
