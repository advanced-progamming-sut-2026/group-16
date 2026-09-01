package io.github.finalwave.server.auth;

import io.github.finalwave.network.auth.LoginFailReason;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.auth.LoginRequest;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;
import io.github.finalwave.server.db.ServerDatabase;
import io.github.finalwave.server.session.SessionRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginServiceTest {
    private static final Path DATABASE = Path.of("build", "login-service-test.db");

    private ServerDatabase database;
    private ServerContext context;
    private RegisterService registerService;
    private LoginService loginService;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        io.github.finalwave.model.user.UserDatabase.resetInstanceForTests();
        database = new ServerDatabase();
        database.initializeSchema();
        context = new ServerContext(database, new SessionRegistry());
        registerService = new RegisterService(database);
        loginService = new LoginService(context);
    }

    @AfterEach
    void tearDown() throws Exception {
        io.github.finalwave.model.user.UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void validCredentialsReturnProfile() throws Exception {
        registerUser("login-user");
        ClientHandler handler = new ClientHandler(new Socket(), context);

        LoginService.LoginResult result = loginService.login(
                new LoginRequest("login-user", "Password1!"),
                handler
        );

        assertTrue(result.isSuccess());
        LoginOkPayload payload = result.successPayload();
        assertNotNull(payload);
        assertEquals("login-user", payload.getUsername());
        assertEquals(0, payload.getCoins());
        assertNotNull(payload.getPlants());
        assertFalse(payload.getPlants().isEmpty());
    }

    @Test
    void wrongPasswordReturnsBadCredentials() throws Exception {
        registerUser("bad-pass-user");
        ClientHandler handler = new ClientHandler(new Socket(), context);

        LoginService.LoginResult result = loginService.login(
                new LoginRequest("bad-pass-user", "WrongPass1!"),
                handler
        );

        assertFalse(result.isSuccess());
        assertEquals(LoginFailReason.BAD_CREDENTIALS, result.failurePayload().getReason());
    }

    @Test
    void duplicateLoginReturnsAlreadyLoggedIn() throws Exception {
        registerUser("dup-login-user");
        ClientHandler first = new ClientHandler(new Socket(), context);
        ClientHandler second = new ClientHandler(new Socket(), context);

        LoginService.LoginResult firstResult = loginService.login(
                new LoginRequest("dup-login-user", "Password1!"),
                first
        );
        assertTrue(firstResult.isSuccess());

        LoginService.LoginResult secondResult = loginService.login(
                new LoginRequest("dup-login-user", "Password1!"),
                second
        );

        assertFalse(secondResult.isSuccess());
        assertEquals(LoginFailReason.ALREADY_LOGGED_IN, secondResult.failurePayload().getReason());
    }

    @Test
    void unbindAllowsRelogin() throws Exception {
        registerUser("relogin-user");
        ClientHandler first = new ClientHandler(new Socket(), context);
        ClientHandler second = new ClientHandler(new Socket(), context);

        assertTrue(loginService.login(new LoginRequest("relogin-user", "Password1!"), first).isSuccess());
        context.sessionRegistry().unbind(first);

        LoginService.LoginResult secondResult = loginService.login(
                new LoginRequest("relogin-user", "Password1!"),
                second
        );
        assertTrue(secondResult.isSuccess());
    }

    private void registerUser(String username) {
        io.github.finalwave.network.auth.RegisterRequest request = new io.github.finalwave.network.auth.RegisterRequest(
                username,
                "Password1!",
                "Nick" + username.replace('-', 'x'),
                username + "@example.com",
                "MALE",
                1,
                "fluffy"
        );
        RegisterService.RegisterResult result = registerService.register(request);
        assertTrue(result.isSuccess());
    }
}
