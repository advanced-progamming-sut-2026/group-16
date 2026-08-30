package io.github.finalwave.server.leaderboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.leaderboard.GetLeaderboardFailPayload;
import io.github.finalwave.network.leaderboard.GetLeaderboardOkPayload;
import io.github.finalwave.network.leaderboard.LeaderboardRow;
import io.github.finalwave.network.sync.SyncFailReason;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;
import io.github.finalwave.server.auth.RegisterService;
import io.github.finalwave.server.db.ServerDatabase;
import io.github.finalwave.server.session.SessionRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderboardHandlerTest {
    private static final Path DATABASE = Path.of("build", "leaderboard-handler-test.db");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ServerContext context;
    private ServerDatabase database;
    private RegisterService registerService;
    private ClientHandler handler;
    private LeaderboardHandler leaderboardHandler;

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
        handler = new ClientHandler(new Socket(), context);
        leaderboardHandler = new LeaderboardHandler(context, handler);
    }

    @AfterEach
    void tearDown() throws Exception {
        io.github.finalwave.model.user.UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void returnsAllUsersWithNullableMyPoint() throws Exception {
        registerUser("leader-alpha");
        registerUser("leader-beta");

        User alpha = database.getUser("leader-alpha");
        alpha.getChapterProgress().markLevelCompleted(ChapterId.ANCIENT_EGYPT, 2);
        alpha.getMiniGameProgress().markStageCompleted(MiniGameId.VASE_BREAKER, 1);
        alpha.getMiniGameProgress().markStageCompleted(MiniGameId.VASE_BREAKER, 2);
        alpha.updateBestMeowPoint(250);
        database.delegate().saveAdventureProgress(alpha);
        database.delegate().saveMiniGameProgress(alpha);
        database.delegate().saveBestMeowPoint(alpha);

        bindSession("leader-alpha");
        MessageEnvelope response = leaderboardHandler.handle(envelope(MessageTypes.GET_LEADERBOARD, null));

        assertEquals(MessageTypes.GET_LEADERBOARD_OK, response.getType());
        GetLeaderboardOkPayload payload = MAPPER.treeToValue(response.getPayload(), GetLeaderboardOkPayload.class);
        Map<String, LeaderboardRow> rows = payload.getEntries().stream()
                .collect(Collectors.toMap(LeaderboardRow::getUsername, Function.identity()));

        assertEquals(2, rows.size());
        LeaderboardRow alphaRow = rows.get("leader-alpha");
        LeaderboardRow betaRow = rows.get("leader-beta");
        assertEquals("Level 2 Chapter 1", alphaRow.getProgressLabel());
        assertEquals(2, alphaRow.getMinigameCount());
        assertEquals(250, alphaRow.getMyPoint());
        assertNull(betaRow.getMyPoint());
    }

    @Test
    void requiresAuthenticatedSession() throws Exception {
        registerUser("leader-guest");
        MessageEnvelope response = leaderboardHandler.handle(envelope(MessageTypes.GET_LEADERBOARD, null));

        assertEquals(MessageTypes.GET_LEADERBOARD_FAIL, response.getType());
        GetLeaderboardFailPayload payload = MAPPER.treeToValue(response.getPayload(), GetLeaderboardFailPayload.class);
        assertEquals(SyncFailReason.AUTH_REQUIRED, payload.getReason());
    }

    private MessageEnvelope envelope(String type, Object payload) {
        return new MessageEnvelope(type, type + "-req", MAPPER.valueToTree(payload));
    }

    private void registerUser(String username) {
        var result = registerService.register(new io.github.finalwave.network.auth.RegisterRequest(
                username,
                "Password1!",
                "Nick" + username,
                username + "@example.com",
                "MALE",
                1,
                "fluffy"
        ));
        assertTrue(result.isSuccess());
    }

    private void bindSession(String username) {
        assertTrue(context.sessionRegistry().tryBind(username, handler).isEmpty());
    }
}
