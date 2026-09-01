package io.github.finalwave.model.user;

import io.github.finalwave.util.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public final class UserProgressStore {
    private static final String[] PROGRESS_TABLE_DDLS = {
            """
                    CREATE TABLE IF NOT EXISTS user_wallet (
                        userId INTEGER PRIMARY KEY,
                        coins INTEGER NOT NULL DEFAULT 0,
                        diamonds INTEGER NOT NULL DEFAULT 0,
                        plantFood INTEGER NOT NULL DEFAULT 0,
                        dailyOfferPlant TEXT,
                        dailyOfferDate TEXT,
                        dailyOfferPurchased INTEGER NOT NULL DEFAULT 0,
                        gamesPlayed INTEGER NOT NULL DEFAULT 0,
                        questDay TEXT,
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """,
            """
                    CREATE TABLE IF NOT EXISTS greenhouse_pots (
                        userId INTEGER NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        locked INTEGER NOT NULL CHECK(locked IN (0, 1)),
                        plantType TEXT,
                        plantedAtMillis INTEGER NOT NULL DEFAULT 0,
                        isMarigold INTEGER NOT NULL DEFAULT 0 CHECK(isMarigold IN (0, 1)),
                        PRIMARY KEY (userId, x, y),
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """,
            """
                    CREATE TABLE IF NOT EXISTS stored_boosts (
                        userId INTEGER NOT NULL,
                        plantType TEXT NOT NULL,
                        PRIMARY KEY (userId, plantType),
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """,
            """
                    CREATE TABLE IF NOT EXISTS user_news (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        userId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        subject TEXT,
                        message TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        isRead INTEGER NOT NULL CHECK(isRead IN (0, 1)),
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """,
            """
                    CREATE TABLE IF NOT EXISTS user_unlocked_zombies (
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        PRIMARY KEY (userId, name),
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """,
            """
                    CREATE TABLE IF NOT EXISTS user_unlocked_levels (
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        PRIMARY KEY (userId, name),
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """,
            """
                    CREATE TABLE IF NOT EXISTS user_unlocked_minigames (
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        PRIMARY KEY (userId, name),
                        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """
    };

    private UserProgressStore() {
    }

    public static void createTables() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String ddl : PROGRESS_TABLE_DDLS) {
                stmt.execute(ddl);
            }
            migrateGamesPlayedColumn(stmt);
            migrateQuestDayColumn(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user progress tables.", e);
        }
    }

    private static void migrateGamesPlayedColumn(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE user_wallet ADD COLUMN gamesPlayed INTEGER NOT NULL DEFAULT 0");
        } catch (SQLException ignored) {
        }
    }

    private static void migrateQuestDayColumn(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE user_wallet ADD COLUMN questDay TEXT");
        } catch (SQLException ignored) {
        }
    }

    public static void initializeUserProgress(Connection conn, User user) throws SQLException {
        UserProgressInitializer.initializeUserProgress(user);
        saveUserProgress(conn, user);
    }

    public static void loadUserProgress(Connection conn, User user) throws SQLException {
        loadWallet(conn, user);
        loadGreenhousePots(conn, user);
        loadStoredBoosts(conn, user);
        loadNews(conn, user);
        loadUnlockSet(conn, user, "user_unlocked_zombies", user.getUnlockedZombies());
        loadUnlockSet(conn, user, "user_unlocked_levels", user.getUnlockedLevels());
        loadUnlockSet(conn, user, "user_unlocked_minigames", user.getUnlockedMinigames());
        if (user.getGreenhousePots().isEmpty()) {
            UserProgressInitializer.ensureGreenhousePots(user);
        }
    }

    public static void saveUserProgress(Connection conn, User user) throws SQLException {
        saveWalletRow(conn, user);
        saveGreenhousePots(conn, user);
        saveStoredBoostsRow(conn, user);
        saveNewsRows(conn, user);
        saveUnlockSet(conn, user, "user_unlocked_zombies", user.getUnlockedZombies());
        saveUnlockSet(conn, user, "user_unlocked_levels", user.getUnlockedLevels());
        saveUnlockSet(conn, user, "user_unlocked_minigames", user.getUnlockedMinigames());
    }

    private static void loadWallet(Connection conn, User user) throws SQLException {
        String sql = """
                SELECT coins, diamonds, plantFood, dailyOfferPlant, dailyOfferDate,
                       dailyOfferPurchased, gamesPlayed, questDay
                FROM user_wallet
                WHERE userId = ?
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                user.setCoins(rs.getInt("coins"));
                user.setDiamonds(rs.getInt("diamonds"));
                user.setPlantFood(rs.getInt("plantFood"));
                user.setDailyOfferPlant(rs.getString("dailyOfferPlant"));
                String dailyOfferDate = rs.getString("dailyOfferDate");
                user.setDailyOfferDate(dailyOfferDate == null ? null : LocalDate.parse(dailyOfferDate));
                user.setDailyOfferPurchased(rs.getInt("dailyOfferPurchased") == 1);
                user.setGamesPlayed(rs.getInt("gamesPlayed"));
                String questDay = rs.getString("questDay");
                user.setQuestDay(questDay == null || questDay.isBlank() ? null : LocalDate.parse(questDay));
            }
        }
    }

    private static void loadGreenhousePots(Connection conn, User user) throws SQLException {
        String sql = """
                SELECT x, y, locked, plantType, plantedAtMillis, isMarigold
                FROM greenhouse_pots
                WHERE userId = ?
                ORDER BY y, x
                """;
        user.getGreenhousePots().clear();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GreenhousePot pot = new GreenhousePot(
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("locked") == 1
                    );
                    String plantType = rs.getString("plantType");
                    if (plantType != null) {
                        pot.plant(plantType, rs.getInt("isMarigold") == 1, rs.getLong("plantedAtMillis"));
                    }
                    user.getGreenhousePots().add(pot);
                }
            }
        }
    }

    private static void loadStoredBoosts(Connection conn, User user) throws SQLException {
        String sql = "SELECT plantType FROM stored_boosts WHERE userId = ? ORDER BY plantType";
        user.getStoredBoosts().clear();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    user.getStoredBoosts().add(rs.getString("plantType"));
                }
            }
        }
    }

    public static void saveWalletRow(Connection conn, User user) throws SQLException {
        String sql = """
                INSERT INTO user_wallet (userId, coins, diamonds, plantFood, dailyOfferPlant,
                                         dailyOfferDate, dailyOfferPurchased, gamesPlayed, questDay)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(userId) DO UPDATE SET
                    coins = excluded.coins,
                    diamonds = excluded.diamonds,
                    plantFood = excluded.plantFood,
                    dailyOfferPlant = excluded.dailyOfferPlant,
                    dailyOfferDate = excluded.dailyOfferDate,
                    dailyOfferPurchased = excluded.dailyOfferPurchased,
                    gamesPlayed = excluded.gamesPlayed,
                    questDay = excluded.questDay
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setInt(2, user.getCoins());
            pstmt.setInt(3, user.getDiamonds());
            pstmt.setInt(4, user.getPlantFood());
            pstmt.setString(5, user.getDailyOfferPlant());
            pstmt.setString(6, user.getDailyOfferDate() == null ? null : user.getDailyOfferDate().toString());
            pstmt.setInt(7, user.isDailyOfferPurchased() ? 1 : 0);
            pstmt.setInt(8, user.getGamesPlayed());
            pstmt.setString(9, user.getQuestDay() == null ? null : user.getQuestDay().toString());
            pstmt.executeUpdate();
        }
    }

    public static void saveGreenhousePots(Connection conn, User user) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM greenhouse_pots WHERE userId = ?")) {
            delete.setLong(1, user.getId());
            delete.executeUpdate();
        }
        String sql = """
                INSERT INTO greenhouse_pots (userId, x, y, locked, plantType, plantedAtMillis, isMarigold)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement insert = conn.prepareStatement(sql)) {
            for (GreenhousePot pot : user.getGreenhousePots()) {
                bindPot(insert, user.getId(), pot);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    public static void saveSinglePot(Connection conn, long userId, GreenhousePot pot) throws SQLException {
        String sql = """
                INSERT INTO greenhouse_pots (userId, x, y, locked, plantType, plantedAtMillis, isMarigold)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(userId, x, y) DO UPDATE SET
                    locked = excluded.locked,
                    plantType = excluded.plantType,
                    plantedAtMillis = excluded.plantedAtMillis,
                    isMarigold = excluded.isMarigold
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            bindPot(pstmt, userId, pot);
            pstmt.executeUpdate();
        }
    }

    private static void bindPot(PreparedStatement pstmt, long userId, GreenhousePot pot) throws SQLException {
        pstmt.setLong(1, userId);
        pstmt.setInt(2, pot.getX());
        pstmt.setInt(3, pot.getY());
        pstmt.setInt(4, pot.isLocked() ? 1 : 0);
        pstmt.setString(5, pot.getPlantType());
        pstmt.setLong(6, pot.getPlantedAtMillis());
        pstmt.setInt(7, pot.isMarigold() ? 1 : 0);
    }

    public static void saveStoredBoostsRow(Connection conn, User user) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM stored_boosts WHERE userId = ?")) {
            delete.setLong(1, user.getId());
            delete.executeUpdate();
        }
        String sql = "INSERT INTO stored_boosts (userId, plantType) VALUES (?, ?)";
        try (PreparedStatement insert = conn.prepareStatement(sql)) {
            for (String plantType : user.getStoredBoosts()) {
                insert.setLong(1, user.getId());
                insert.setString(2, plantType);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void loadNews(Connection conn, User user) throws SQLException {
        String sql = """
                SELECT id, type, subject, message, createdAtMillis, isRead
                FROM user_news
                WHERE userId = ?
                ORDER BY createdAtMillis ASC, id ASC
                """;
        user.getNewsItems().clear();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NewsItem item = new NewsItem(
                            NewsType.valueOf(rs.getString("type")),
                            rs.getString("subject"),
                            rs.getString("message"),
                            rs.getLong("createdAtMillis"),
                            rs.getInt("isRead") == 1
                    );
                    item.setId(rs.getLong("id"));
                    user.getNewsItems().add(item);
                }
            }
        }
        NewsManager.syncNextIdFrom(user.getNewsItems());
    }

    public static void saveNewsRows(Connection conn, User user) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM user_news WHERE userId = ?")) {
            delete.setLong(1, user.getId());
            delete.executeUpdate();
        }
        String sql = """
                INSERT INTO user_news (id, userId, type, subject, message, createdAtMillis, isRead)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement insert = conn.prepareStatement(sql)) {
            for (NewsItem item : user.getNewsItems()) {
                insert.setLong(1, item.getId());
                insert.setLong(2, user.getId());
                insert.setString(3, item.getType().name());
                insert.setString(4, item.getSubject());
                insert.setString(5, item.getMessage());
                insert.setLong(6, item.getCreatedAtMillis());
                insert.setInt(7, item.isRead() ? 1 : 0);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void loadUnlockSet(Connection conn, User user, String tableName, java.util.Set<String> target)
            throws SQLException {
        target.clear();
        String sql = "SELECT name FROM " + tableName + " WHERE userId = ? ORDER BY name";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    target.add(rs.getString("name"));
                }
            }
        }
    }

    private static void saveUnlockSet(Connection conn, User user, String tableName, java.util.Set<String> values)
            throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM " + tableName + " WHERE userId = ?")) {
            delete.setLong(1, user.getId());
            delete.executeUpdate();
        }
        String sql = "INSERT INTO " + tableName + " (userId, name) VALUES (?, ?)";
        try (PreparedStatement insert = conn.prepareStatement(sql)) {
            for (String name : values) {
                insert.setLong(1, user.getId());
                insert.setString(2, name);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    public static void saveSingleUnlock(Connection conn, long userId, String kind, String name) throws SQLException {
        String tableName = unlockTableName(kind);
        String sql = "INSERT OR IGNORE INTO " + tableName + " (userId, name) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        }
    }

    public static String unlockTableName(String kind) {
        if (UnlockKind.ZOMBIES.name().equals(kind)) {
            return "user_unlocked_zombies";
        }
        if (UnlockKind.LEVELS.name().equals(kind)) {
            return "user_unlocked_levels";
        }
        if (UnlockKind.MINIGAMES.name().equals(kind)) {
            return "user_unlocked_minigames";
        }
        throw new IllegalArgumentException("Unknown unlock kind: " + kind);
    }
}
