package io.github.finalwave.server.auth;

import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.network.auth.RegisterFailReason;
import io.github.finalwave.network.auth.RegisterOkPayload;
import io.github.finalwave.network.auth.RegisterRequest;
import io.github.finalwave.server.db.ServerDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterServiceTest {
    private static final Path DATABASE = Path.of("build", "register-service-test.db");

    private ServerDatabase database;
    private RegisterService registerService;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
        database = new ServerDatabase();
        database.initializeSchema();
        registerService = new RegisterService(database);
    }

    @AfterEach
    void tearDown() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void registersUserWithDefaultProgressRows() throws Exception {
        RegisterRequest request = validRequest("network-user", "network@example.com");
        RegisterService.RegisterResult result = registerService.register(request);

        assertTrue(result.isSuccess());
        RegisterOkPayload payload = result.successPayload();
        assertNotNull(payload);
        assertTrue(payload.getUserId() > 0);
        assertEquals("network-user", payload.getUsername());
        assertEquals(0, payload.getCoins());
        assertEquals(0, payload.getDiamonds());

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE.toAbsolutePath());
             Statement stmt = conn.createStatement()) {
            try (ResultSet wallet = stmt.executeQuery(
                    "SELECT coins, diamonds, plantFood FROM user_wallet WHERE userId = "
                            + payload.getUserId())) {
                assertTrue(wallet.next());
                assertEquals(0, wallet.getInt("coins"));
                assertEquals(0, wallet.getInt("diamonds"));
                assertEquals(0, wallet.getInt("plantFood"));
            }
            try (ResultSet plants = stmt.executeQuery(
                    "SELECT COUNT(*) AS total FROM user_plants WHERE userId = " + payload.getUserId())) {
                assertTrue(plants.next());
                assertEquals(3, plants.getInt("total"));
            }
            try (ResultSet levels = stmt.executeQuery(
                    "SELECT name FROM user_unlocked_levels WHERE userId = " + payload.getUserId())) {
                assertTrue(levels.next());
                assertEquals("1-1", levels.getString("name"));
                assertFalse(levels.next());
            }
            try (ResultSet minigames = stmt.executeQuery(
                    "SELECT name FROM user_unlocked_minigames WHERE userId = " + payload.getUserId())) {
                assertTrue(minigames.next());
                assertEquals("vase-breaker", minigames.getString("name"));
                assertFalse(minigames.next());
            }
        }
    }

    @Test
    void rejectsDuplicateUsername() {
        RegisterService.RegisterResult first = registerService.register(validRequest("dup-user", "one@example.com"));
        assertTrue(first.isSuccess());

        RegisterRequest duplicate = validRequest("dup-user", "two@example.com");
        RegisterService.RegisterResult second = registerService.register(duplicate);

        assertFalse(second.isSuccess());
        assertEquals(RegisterFailReason.USERNAME_TAKEN, second.failurePayload().getReason());
    }

    @Test
    void rejectsDuplicateEmail() {
        RegisterService.RegisterResult first = registerService.register(validRequest("user-one", "same@example.com"));
        assertTrue(first.isSuccess());

        RegisterRequest duplicate = validRequest("user-two", "same@example.com");
        RegisterService.RegisterResult second = registerService.register(duplicate);

        assertFalse(second.isSuccess());
        assertEquals(RegisterFailReason.EMAIL_TAKEN, second.failurePayload().getReason());
    }

    @Test
    void rejectsInvalidEmail() {
        RegisterRequest request = validRequest("bad-email-user", "not-an-email");
        RegisterService.RegisterResult result = registerService.register(request);

        assertFalse(result.isSuccess());
        assertEquals(RegisterFailReason.INVALID_EMAIL, result.failurePayload().getReason());
    }

    @Test
    void rejectsInvalidGender() {
        RegisterRequest request = validRequest("bad-gender-user", "gender@example.com");
        request.setGender("OTHER");
        RegisterService.RegisterResult result = registerService.register(request);

        assertFalse(result.isSuccess());
        assertEquals(RegisterFailReason.INVALID_GENDER, result.failurePayload().getReason());
    }

    private static RegisterRequest validRequest(String username, String email) {
        return new RegisterRequest(
                username,
                "Password1!",
                "Nick" + username.replace('-', 'x'),
                email,
                "MALE",
                1,
                "fluffy"
        );
    }
}
