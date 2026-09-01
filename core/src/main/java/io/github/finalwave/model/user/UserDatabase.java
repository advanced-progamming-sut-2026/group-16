package io.github.finalwave.model.user;

import io.github.finalwave.model.collection.OwnedPlant;
import io.github.finalwave.model.collection.PlayerPlantProgress;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase {
    private static UserDatabase instance;

    private final java.util.List<UserWriteListener> writeListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private volatile boolean writeEventsSuppressed;

    private UserDatabase() {
        createDatabase();
    }

    public static synchronized UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    public void addWriteListener(UserWriteListener listener) {
        if (listener != null) {
            writeListeners.add(listener);
        }
    }

    public void removeWriteListener(UserWriteListener listener) {
        writeListeners.remove(listener);
    }

    public void setWriteEventsSuppressed(boolean suppressed) {
        this.writeEventsSuppressed = suppressed;
    }

    private void notifyWrite(java.util.function.Consumer<UserWriteListener> action) {
        if (writeEventsSuppressed) {
            return;
        }
        for (UserWriteListener listener : writeListeners) {
            try {
                action.accept(listener);
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static synchronized void resetInstanceForTests() {
        instance = null;
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
            QuestProgressStore.createTables();
            AdventureProgressStore.createTables();
            MiniGameProgressStore.createTables();
            ScoreGameStore.createTables();
            MatchSaveStore.createTables();
            UserSettingsStore.createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Could not create the database.", e);
        }
    }


    public void replaceLocalProfileFromServer(User user, String passwordHash) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("user with valid id is required");
        }
        boolean previousSuppressed = writeEventsSuppressed;
        writeEventsSuppressed = true;
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement pragma = conn.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }
            int securityQuestionId = user.getSecurityQuestionId();
            String securityAnswerHash = user.getSecurityAnswerHash();
            if (securityQuestionId <= 0) {
                User existing = getUser(user.getUsername());
                if (existing != null && existing.getSecurityQuestionId() > 0) {
                    securityQuestionId = existing.getSecurityQuestionId();
                    securityAnswerHash = existing.getSecurityAnswerHash();
                    user.setSecurityQuestionId(securityQuestionId);
                    user.setSecurityAnswerHash(securityAnswerHash);
                }
            }
            try (PreparedStatement delete = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                delete.setString(1, user.getUsername());
                delete.executeUpdate();
            }
            String insertSql = """
                    INSERT INTO users (id, username, passwordHash, nickname, email, gender,
                    securityQuestionNumber, securityAnswerHash)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setLong(1, user.getId());
                insert.setString(2, user.getUsername());
                insert.setString(3, passwordHash == null ? "" : passwordHash);
                insert.setString(4, user.getNickname());
                insert.setString(5, user.getEmail());
                insert.setString(6, user.getGender().name());
                if (securityQuestionId > 0) {
                    insert.setInt(7, securityQuestionId);
                } else {
                    insert.setNull(7, Types.INTEGER);
                }
                insert.setString(8, securityAnswerHash);
                insert.executeUpdate();
            }
            replacePlantProgress(conn, user);
            UserProgressStore.saveUserProgress(conn, user);
            AdventureProgressStore.save(conn, user);
            MiniGameProgressStore.save(conn, user);
            ScoreGameStore.save(conn, user);
            UserSettingsStore.save(conn, user);
            if (user.getQuestTracker() != null) {
                QuestProgressStore.saveQuestProgress(conn, user, user.getQuestTracker());
            }
            MatchSaveSnapshot snapshot = user.getMatchSaveSnapshot();
            if (snapshot != null) {
                MatchSaveStore.save(conn, user, snapshot);
            } else {
                MatchSaveStore.clear(conn, user);
            }
            conn.commit();
        } catch (SQLException exception) {
            throw new RuntimeException("Could not cache server profile locally.", exception);
        } finally {
            writeEventsSuppressed = previousSuppressed;
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
            AdventureProgressStore.save(conn, user);
            UserSettingsStore.save(conn, user);
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

    public void updateSecurityQuestion(String username, int securityQuestionId, String securityAnswerHash) {
        String sql = """
                UPDATE users
                SET securityQuestionNumber = ?, securityAnswerHash = COALESCE(?, securityAnswerHash)
                WHERE username = ?
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (securityQuestionId > 0) {
                pstmt.setInt(1, securityQuestionId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, securityAnswerHash);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update security question.", e);
        }
    }

    public void updateProfile(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("user with valid id is required");
        }
        String sql = """
                UPDATE users
                SET username = ?, nickname = ?, email = ?, passwordHash = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getNickname());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setLong(5, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update profile.", e);
        }
    }

    public void saveGamesPlayed(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            UserProgressStore.saveWalletRow(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save games played.", e);
        }
        notifyWrite(listener -> listener.onWalletChanged(user));
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
        AdventureProgressStore.load(conn, user);
        MiniGameProgressStore.load(conn, user);
        ScoreGameStore.load(conn, user);
        UserSettingsStore.load(conn, user);
        return user;
    }

    public void saveBestMeowPoint(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            ScoreGameStore.save(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save best meowpoint.", e);
        }
        notifyWrite(listener -> listener.onScoreGameChanged(user));
    }

    public void saveMatchSnapshot(User user, MatchSaveSnapshot snapshot) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            MatchSaveStore.save(conn, user, snapshot);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save match.", e);
        }
        notifyWrite(listener -> listener.onMatchSaved(user, snapshot));
    }

    public MatchSaveSnapshot loadMatchSnapshot(User user) {
        if (user == null) {
            return null;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            return MatchSaveStore.load(conn, user);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load match save.", e);
        }
    }

    public void clearMatchSave(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            MatchSaveStore.clear(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not clear match save.", e);
        }
        notifyWrite(listener -> listener.onMatchCleared(user));
    }

    public void saveUserSettings(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            UserSettingsStore.save(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save user settings.", e);
        }
        notifyWrite(listener -> listener.onSettingsChanged(user));
    }

    public void saveAdventureProgress(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            AdventureProgressStore.save(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save adventure progress.", e);
        }
        notifyWrite(listener -> listener.onAdventureChanged(user));
    }

    public void saveMiniGameProgress(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            MiniGameProgressStore.save(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save minigame progress.", e);
        }
        notifyWrite(listener -> listener.onMiniGameStagesChanged(user));
    }

    public void loadQuestProgress(User user, io.github.finalwave.model.quest.QuestTracker tracker) {
        if (user == null || tracker == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            QuestProgressStore.loadQuestProgress(conn, user, tracker);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load quest progress.", e);
        }
    }

    public void saveQuestProgress(User user) {
        if (user == null || user.getQuestTracker() == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            QuestProgressStore.saveQuestProgress(conn, user, user.getQuestTracker());
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save quest progress.", e);
        }
        notifyWrite(listener -> listener.onQuestProgressChanged(user));
    }

    public void savePlantProgress(User user) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            replacePlantProgress(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save plant progress.", e);
        }
        notifyWrite(listener -> listener.onPlantsChanged(user, user.getPlantProgress().getOwnedPlants().keySet()));
    }

    public void savePlant(User user, String plantName) {
        if (user == null || plantName == null || plantName.isBlank()) {
            return;
        }
        OwnedPlant plant = user.getPlantProgress().getOwnedPlant(plantName).orElse(null);
        if (plant == null) {
            return;
        }
        savePlantEntry(user, plant);
    }

    public void savePlantEntry(User user, OwnedPlant plant) {
        if (user == null || plant == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            upsertPlantRow(conn, user.getId(), plant);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save plant progress.", e);
        }
        notifyWrite(listener -> listener.onPlantsChanged(user, java.util.Set.of(plant.getPlantName())));
    }

    private void upsertPlantRow(Connection conn, long userId, OwnedPlant plant) throws SQLException {
        String sql = """
                INSERT INTO user_plants (userId, plantName, level, unlocked, seedPackets)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(userId, plantName) DO UPDATE SET
                    level = excluded.level,
                    unlocked = excluded.unlocked,
                    seedPackets = excluded.seedPackets
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, plant.getPlantName());
            pstmt.setInt(3, plant.getLevel());
            pstmt.setInt(4, plant.isUnlocked() ? 1 : 0);
            pstmt.setInt(5, plant.getSeedPackets());
            pstmt.executeUpdate();
        }
    }

    public void saveUserWallet(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            UserProgressStore.saveWalletRow(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save user wallet progress.", e);
        }
        notifyWrite(listener -> listener.onWalletChanged(user));
    }

    public void saveGreenhousePot(User user, GreenhousePot pot) {
        if (user == null || pot == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            UserProgressStore.saveSinglePot(conn, user.getId(), pot);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save greenhouse pot.", e);
        }
        notifyWrite(listener -> listener.onGreenhousePotChanged(user, pot));
    }

    public void saveStoredBoosts(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            UserProgressStore.saveStoredBoostsRow(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save stored boosts.", e);
        }
        notifyWrite(listener -> listener.onStoredBoostsChanged(user));
    }

    public void saveUnlock(User user, UnlockKind kind, String name) {
        if (user == null || kind == null || name == null || name.isBlank()) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            UserProgressStore.saveSingleUnlock(conn, user.getId(), kind.name(), name);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save unlock.", e);
        }
        notifyWrite(listener -> listener.onUnlocked(user, kind.name(), name));
    }

    public void saveUserNews(User user) {
        if (user == null) {
            return;
        }
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            UserProgressStore.saveNewsRows(conn, user);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save user news.", e);
        }
        notifyWrite(listener -> listener.onNewsChanged(user));
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
