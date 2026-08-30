package io.github.finalwave.server.score;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.score.SubmitScoreFailPayload;
import io.github.finalwave.network.score.SubmitScoreOkPayload;
import io.github.finalwave.network.score.SubmitScoreRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitScoreHandlerTest {
    private static final Path DATABASE = Path.of("build", "submit-score-handler-test.db");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ServerContext context;
    private ServerDatabase database;
    private RegisterService registerService;
    private ClientHandler handler;
    private SubmitScoreHandler submitScoreHandler;

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
        submitScoreHandler = new SubmitScoreHandler(context, handler);
    }

    @AfterEach
    void tearDown() throws Exception {
        io.github.finalwave.model.user.UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void requiresAuthenticatedSession() throws Exception {
        registerUser("submit-guest");
        MessageEnvelope response = submitScoreHandler.handle(envelope(42));

        assertEquals(MessageTypes.SUBMIT_SCORE_FAIL, response.getType());
        SubmitScoreFailPayload payload = MAPPER.treeToValue(response.getPayload(), SubmitScoreFailPayload.class);
        assertEquals(SyncFailReason.AUTH_REQUIRED, payload.getReason());
    }

    @Test
    void firstSubmitSetsHasPlayedAndPersists() throws Exception {
        registerUser("submit-first");
        bindSession("submit-first");

        MessageEnvelope response = submitScoreHandler.handle(envelope(42));

        assertEquals(MessageTypes.SUBMIT_SCORE_OK, response.getType());
        SubmitScoreOkPayload payload = MAPPER.treeToValue(response.getPayload(), SubmitScoreOkPayload.class);
        assertTrue(payload.isHasPlayed());
        assertTrue(payload.isNewBest());
        assertEquals(42, payload.getBestMeowPoint());

        User reloaded = database.getUser("submit-first");
        assertTrue(reloaded.hasPlayed());
        assertEquals(42, reloaded.getBestMeowPoint());
    }

    @Test
    void lowerRepeatScoreLeavesBestUnchanged() throws Exception {
        registerUser("submit-repeat");
        bindSession("submit-repeat");
        submitScoreHandler.handle(envelope(50));

        MessageEnvelope response = submitScoreHandler.handle(envelope(30));

        assertEquals(MessageTypes.SUBMIT_SCORE_OK, response.getType());
        SubmitScoreOkPayload payload = MAPPER.treeToValue(response.getPayload(), SubmitScoreOkPayload.class);
        assertTrue(payload.isHasPlayed());
        assertFalse(payload.isNewBest());
        assertEquals(50, payload.getBestMeowPoint());

        User reloaded = database.getUser("submit-repeat");
        assertEquals(50, reloaded.getBestMeowPoint());
    }

    @Test
    void higherScoreUpdatesBestInDatabase() throws Exception {
        registerUser("submit-beat");
        bindSession("submit-beat");
        submitScoreHandler.handle(envelope(50));

        MessageEnvelope response = submitScoreHandler.handle(envelope(80));

        assertEquals(MessageTypes.SUBMIT_SCORE_OK, response.getType());
        SubmitScoreOkPayload payload = MAPPER.treeToValue(response.getPayload(), SubmitScoreOkPayload.class);
        assertTrue(payload.isNewBest());
        assertEquals(80, payload.getBestMeowPoint());

        User reloaded = database.getUser("submit-beat");
        assertEquals(80, reloaded.getBestMeowPoint());
    }

    @Test
    void rejectsNegativeScore() throws Exception {
        registerUser("submit-invalid");
        bindSession("submit-invalid");

        MessageEnvelope response = submitScoreHandler.handle(envelope(-1));

        assertEquals(MessageTypes.SUBMIT_SCORE_FAIL, response.getType());
        SubmitScoreFailPayload payload = MAPPER.treeToValue(response.getPayload(), SubmitScoreFailPayload.class);
        assertEquals(SyncFailReason.VALIDATION, payload.getReason());
    }

    private MessageEnvelope envelope(int score) {
        SubmitScoreRequest request = new SubmitScoreRequest(score);
        return new MessageEnvelope(
                MessageTypes.SUBMIT_SCORE,
                MessageTypes.SUBMIT_SCORE + "-req",
                MAPPER.valueToTree(request));
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
