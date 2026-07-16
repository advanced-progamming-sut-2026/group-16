package model.user;

import model.collection.OwnedPlant;
import model.collection.PlayerPlantProgress;
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
        String plantSql = """
                CREATE TABLE IF NOT EXISTS user_plants (
                    userId INTEGER NOT NULL,
                    plantName TEXT NOT NULL,
                    level INTEGER NOT NULL CHECK(level BETWEEN 1 AND 4),
                    unlocked INTEGER NOT NULL CHECK(unlocked IN (0, 1)),
                    seedPackets INTEGER NOT NULL DEFAULT 0 CHECK(seedPackets >= 0),
                    PRIMARY KEY (userId, plantName),
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(sql);
            stmt.execute(plantSql);
            UserProgressStore.createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Could not create the database.", e);
        }
    }


    public void registerUser(User user) {
        String sql = """
                INSERT INTO users (username, passwordHash, nickname, email, gender,
                securityQuestionNumber, securityAnswerHash)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
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
            }
            replacePlantProgress(conn, user);
            UserProgressStore.initializeUserProgress(conn, user);
            conn.commit();
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
                    return mapUser(rs, conn);
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
                users.add(mapUser(rs, conn));
            }
        } catch (SQLException e) {
            // TODO: handle database error
        }

        return users;
    }

    private User mapUser(ResultSet rs, Connection conn) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("passwordHash"));
        user.setNickname(rs.getString("nickname"));
        user.setEmail(rs.getString("email"));
        user.setGender(Gender.fromString(rs.getString("gender")));
        user.setSecurityQuestionId(rs.getInt("securityQuestionNumber"));
        user.setSecurityAnswerHash(rs.getString("securityAnswerHash"));
        user.setPlantProgress(loadPlantProgress(conn, user.getId()));
        UserProgressStore.loadUserProgress(conn, user);
        return user;
    }

    public void savePlantProgress(User user) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            replacePlantProgress(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save plant progress.", e);
        }
    }

    public void saveUserWallet(User user) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            UserProgressStore.saveUserProgress(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save user wallet progress.", e);
        }
    }

    private void replacePlantProgress(Connection conn, User user) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM user_plants WHERE userId = ?")) {
            delete.setLong(1, user.getId());
            delete.executeUpdate();
        }

        String insertSql = """
                INSERT INTO user_plants
                (userId, plantName, level, unlocked, seedPackets)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
            for (OwnedPlant plant : user.getPlantProgress().getOwnedPlants().values()) {
                insert.setLong(1, user.getId());
                insert.setString(2, plant.getPlantName());
                insert.setInt(3, plant.getLevel());
                insert.setInt(4, plant.isUnlocked() ? 1 : 0);
                insert.setInt(5, plant.getSeedPackets());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private PlayerPlantProgress loadPlantProgress(Connection conn, long userId)
            throws SQLException {
        String sql = """
                SELECT plantName, level, unlocked, seedPackets
                FROM user_plants
                WHERE userId = ?
                ORDER BY plantName
                """;
        List<OwnedPlant> plants = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    plants.add(new OwnedPlant(
                            rs.getString("plantName"),
                            rs.getInt("level"),
                            rs.getInt("unlocked") != 0,
                            rs.getInt("seedPackets")));
                }
            }
        }
        return PlayerPlantProgress.fromOwnedPlants(plants);
    }
}
