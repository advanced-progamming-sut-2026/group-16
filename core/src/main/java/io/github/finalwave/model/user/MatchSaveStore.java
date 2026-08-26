package io.github.finalwave.model.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class MatchSaveStore {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MatchSaveStore() {
    }

    public static void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_match_save (
                    userId INTEGER PRIMARY KEY,
                    snapshot TEXT NOT NULL,
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user_match_save table.", e);
        }
    }

    public static void save(Connection conn, User user, MatchSaveSnapshot snapshot) throws SQLException {
        if (user == null) {
            return;
        }
        if (snapshot == null) {
            clear(conn, user);
            return;
        }
        String json;
        try {
            json = MAPPER.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new SQLException("Could not serialize match save.", e);
        }
        String sql = """
                INSERT INTO user_match_save (userId, snapshot)
                VALUES (?, ?)
                ON CONFLICT(userId) DO UPDATE SET snapshot = excluded.snapshot
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setString(2, json);
            pstmt.executeUpdate();
        }
    }

    public static MatchSaveSnapshot load(Connection conn, User user) throws SQLException {
        if (user == null) {
            return null;
        }
        String sql = """
                SELECT snapshot
                FROM user_match_save
                WHERE userId = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String json = rs.getString("snapshot");
                if (json == null || json.isBlank()) {
                    return null;
                }
                try {
                    return MAPPER.readValue(json, MatchSaveSnapshot.class);
                } catch (Exception e) {
                    return null;
                }
            }
        }
    }

    public static void clear(Connection conn, User user) throws SQLException {
        if (user == null) {
            return;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM user_match_save WHERE userId = ?")) {
            pstmt.setLong(1, user.getId());
            pstmt.executeUpdate();
        }
    }
}
