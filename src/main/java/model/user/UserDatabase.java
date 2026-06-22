package model.user;

import util.database.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase {
    private static UserDatabase instance;

    private UserDatabase() {
        createDatabase();
    }

    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    private void createDatabase() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    passwordHash TEXT NOT NULL,
                    nickname TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    gender TEXT NOT NULL CHECK(gender IN ('MALE', 'FEMALE')),
                    securityQuestionNumber INTEGER,
                    securityAnswerHash TEXT
                );
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create the database.");
        }
    }


    public void registerUser(User user) {
        String sql = """
                INSERT INTO users (username, passwordHash, nickname, email, gender,
                securityQuestionNumber, securityAnswerHash)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getNickname());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getGender().name());
            pstmt.setInt(6, user.getSecurityQuestionId());
            pstmt.setString(7, user.getSecurityAnswerHash());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not register user.", e);
        }
    }

    public User getUser(String username) {
        String sql = """
                SELECT id, username, passwordHash, nickname, email, gender,
                securityQuestionNumber, securityAnswerHash
                FROM users WHERE username = ?
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            // TODO: handle database error
        }
        return null;
    }

    public boolean isUsernameTaken(String username) {
        return getUser(username) != null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public void updatePassword(String username, String passwordHash) {
        String sql = "UPDATE users SET passwordHash = ? WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, passwordHash);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update password.", e);
        }
    }

    public List<User> getAllUsers() {
        String sql = """
                SELECT id, username, passwordHash, nickname, email, gender,
                securityQuestionNumber, securityAnswerHash
                FROM users
                """;
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            // TODO: handle database error
        }

        return users;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("passwordHash"));
        user.setNickname(rs.getString("nickname"));
        user.setEmail(rs.getString("email"));
        user.setGender(Gender.fromString(rs.getString("gender")));
        user.setSecurityQuestionId(rs.getInt("securityQuestionNumber"));
        user.setSecurityAnswerHash(rs.getString("securityAnswerHash"));
        return user;
    }
}
