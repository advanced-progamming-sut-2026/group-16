package model.user;

import util.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public final class UserProgressStore {
    private UserProgressStore() {
    }

    public static void createTables() {
        String walletSql = """
                CREATE TABLE IF NOT EXISTS user_wallet (
                    userId INTEGER PRIMARY KEY,
                    coins INTEGER NOT NULL DEFAULT 0,
                    diamonds INTEGER NOT NULL DEFAULT 0,
                    plantFood INTEGER NOT NULL DEFAULT 0,
                    dailyOfferPlant TEXT,
                    dailyOfferDate TEXT,
                    dailyOfferPurchased INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;
        String potSql = """
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
                """;
        String boostSql = """
                CREATE TABLE IF NOT EXISTS stored_boosts (
                    userId INTEGER NOT NULL,
                    plantType TEXT NOT NULL,
                    PRIMARY KEY (userId, plantType),
                    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
                );
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(walletSql);
            stmt.execute(potSql);
            stmt.execute(boostSql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user progress tables.", e);
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
        if (user.getGreenhousePots().isEmpty()) {
            UserProgressInitializer.initializeUserProgress(user);
        }
    }

    public static void saveUserProgress(Connection conn, User user) throws SQLException {
        saveWallet(conn, user);
        saveGreenhousePots(conn, user);
        saveStoredBoosts(conn, user);
    }

    private static void loadWallet(Connection conn, User user) throws SQLException {
        String sql = """
                SELECT coins, diamonds, plantFood, dailyOfferPlant, dailyOfferDate, dailyOfferPurchased
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

    private static void saveWallet(Connection conn, User user) throws SQLException {
        String sql = """
                INSERT INTO user_wallet (userId, coins, diamonds, plantFood, dailyOfferPlant, dailyOfferDate, dailyOfferPurchased)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(userId) DO UPDATE SET
                    coins = excluded.coins,
                    diamonds = excluded.diamonds,
                    plantFood = excluded.plantFood,
                    dailyOfferPlant = excluded.dailyOfferPlant,
                    dailyOfferDate = excluded.dailyOfferDate,
                    dailyOfferPurchased = excluded.dailyOfferPurchased
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setInt(2, user.getCoins());
            pstmt.setInt(3, user.getDiamonds());
            pstmt.setInt(4, user.getPlantFood());
            pstmt.setString(5, user.getDailyOfferPlant());
            pstmt.setString(6, user.getDailyOfferDate() == null ? null : user.getDailyOfferDate().toString());
            pstmt.setInt(7, user.isDailyOfferPurchased() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }

    private static void saveGreenhousePots(Connection conn, User user) throws SQLException {
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
                insert.setLong(1, user.getId());
                insert.setInt(2, pot.getX());
                insert.setInt(3, pot.getY());
                insert.setInt(4, pot.isLocked() ? 1 : 0);
                insert.setString(5, pot.getPlantType());
                insert.setLong(6, pot.getPlantedAtMillis());
                insert.setInt(7, pot.isMarigold() ? 1 : 0);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void saveStoredBoosts(Connection conn, User user) throws SQLException {
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
}
