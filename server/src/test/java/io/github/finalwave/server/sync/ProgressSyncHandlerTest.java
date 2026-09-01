package io.github.finalwave.server.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.save.MatchSaveSnapshot;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.sync.UnlockContentPayload;
import io.github.finalwave.network.sync.UpdateAdventurePayload;
import io.github.finalwave.network.sync.UpdateGreenhousePotPayload;
import io.github.finalwave.network.sync.UpdateMatchSavePayload;
import io.github.finalwave.network.sync.UpdateNewsPayload;
import io.github.finalwave.network.sync.UpdatePlantPayload;
import io.github.finalwave.network.sync.UpdateQuestProgressPayload;
import io.github.finalwave.network.sync.UpdateWalletPayload;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressSyncHandlerTest {
    private static final Path DATABASE = Path.of("build", "progress-sync-handler-test.db");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ServerContext context;
    private ServerDatabase database;
    private RegisterService registerService;
    private ClientHandler handler;
    private ProgressSyncHandler syncHandler;

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
        registerUser("sync-user");
        bindSession("sync-user");
        syncHandler = new ProgressSyncHandler(context, handler);
    }

    @AfterEach
    void tearDown() throws Exception {
        io.github.finalwave.model.user.UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void appliesWalletWriteWithClamping() throws Exception {
        UpdateWalletPayload request = new UpdateWalletPayload();
        request.setCoins(-5);
        request.setDiamonds(12);
        request.setPlantFood(3);
        request.setGamesPlayed(2);

        MessageEnvelope response = syncHandler.handle(envelope(MessageTypes.UPDATE_WALLET, request));

        assertEquals(MessageTypes.UPDATE_WALLET_OK, response.getType());
        UpdateWalletPayload ok = MAPPER.treeToValue(response.getPayload(), UpdateWalletPayload.class);
        assertEquals(0, ok.getCoins());
        assertEquals(12, ok.getDiamonds());

        User user = database.getUser("sync-user");
        assertEquals(0, user.getCoins());
        assertEquals(12, user.getDiamonds());
    }

    @Test
    void rejectsWriteWithoutSession() {
        ClientHandler other = new ClientHandler(new Socket(), context);
        ProgressSyncHandler otherHandler = new ProgressSyncHandler(context, other);
        MessageEnvelope response = otherHandler.handle(envelope(
                MessageTypes.UPDATE_WALLET,
                new UpdateWalletPayload()
        ));
        assertEquals(MessageTypes.UPDATE_WALLET_FAIL, response.getType());
    }

    @Test
    void appliesPlantWrite() throws Exception {
        UpdatePlantPayload request = new UpdatePlantPayload("Cherry Bomb", 2, true, 5);
        MessageEnvelope response = syncHandler.handle(envelope(MessageTypes.UPDATE_PLANT, request));
        assertEquals(MessageTypes.UPDATE_PLANT_OK, response.getType());

        User user = database.getUser("sync-user");
        assertTrue(user.getPlantProgress().isOwned("Cherry Bomb"));
        assertEquals(2, user.getPlantProgress().getOwnedPlant("Cherry Bomb").orElseThrow().getLevel());
        assertEquals(5, user.getPlantProgress().getOwnedPlant("Cherry Bomb").orElseThrow().getSeedPackets());
    }

    @Test
    void appliesGreenhousePotWrite() throws Exception {
        UpdateGreenhousePotPayload request = new UpdateGreenhousePotPayload();
        request.setX(1);
        request.setY(1);
        request.setLocked(false);
        request.setPlantType("Marigold");
        request.setPlantedAtMillis(1000L);
        request.setMarigold(true);

        MessageEnvelope response = syncHandler.handle(envelope(MessageTypes.UPDATE_GREENHOUSE_POT, request));
        assertEquals(MessageTypes.UPDATE_GREENHOUSE_POT_OK, response.getType());

        User user = database.getUser("sync-user");
        var pot = user.getPotAt(1, 1);
        assertNotNull(pot);
        assertFalse(pot.isLocked());
        assertEquals("Marigold", pot.getPlantType());
    }

    @Test
    void appliesUnlockContent() throws Exception {
        UnlockContentPayload request = new UnlockContentPayload("ZOMBIES", "Conehead");
        MessageEnvelope response = syncHandler.handle(envelope(MessageTypes.UNLOCK_CONTENT, request));
        assertEquals(MessageTypes.UNLOCK_CONTENT_OK, response.getType());
        assertTrue(database.getUser("sync-user").getUnlockedZombies().contains("Conehead"));
    }

    @Test
    void appliesQuestProgress() throws Exception {
        UpdateQuestProgressPayload request = new UpdateQuestProgressPayload();
        UpdateQuestProgressPayload.QuestProgressRow row = new UpdateQuestProgressPayload.QuestProgressRow();
        row.setQuestId("daily_play");
        row.setCompleted(true);
        row.setClaimed(false);
        row.setProgressBlob("{}");
        request.setRows(List.of(row));

        MessageEnvelope response = syncHandler.handle(envelope(MessageTypes.UPDATE_QUEST_PROGRESS, request));
        assertEquals(MessageTypes.UPDATE_QUEST_PROGRESS_OK, response.getType());
    }

    @Test
    void appliesAdventureProgress() throws Exception {
        UpdateAdventurePayload request = new UpdateAdventurePayload();
        request.setUnlockedChapter(ChapterId.ANCIENT_EGYPT.getKey());
        request.setDifficultyLevel(1);
        request.setCompletedLevels(ChapterId.ANCIENT_EGYPT.getKey() + ":1");

        MessageEnvelope response = syncHandler.handle(envelope(MessageTypes.UPDATE_ADVENTURE, request));
        assertEquals(MessageTypes.UPDATE_ADVENTURE_OK, response.getType());

        User user = database.getUser("sync-user");
        assertEquals(ChapterId.ANCIENT_EGYPT, user.getChapterProgress().getUnlockedChapter());
        assertTrue(user.getChapterProgress().getCompletedLevels(ChapterId.ANCIENT_EGYPT).contains(1));
    }

    @Test
    void appliesMatchSaveAndClear() throws Exception {
        MatchSaveSnapshot snapshot = new MatchSaveSnapshot();
        snapshot.chapterKey = ChapterId.ANCIENT_EGYPT.getKey();
        snapshot.levelIndex = 1;
        snapshot.sun = 150;
        UpdateMatchSavePayload saveRequest = new UpdateMatchSavePayload();
        saveRequest.setSnapshot(MAPPER.valueToTree(snapshot));

        MessageEnvelope saveResponse = syncHandler.handle(envelope(MessageTypes.UPDATE_MATCH_SAVE, saveRequest));
        assertEquals(MessageTypes.UPDATE_MATCH_SAVE_OK, saveResponse.getType());
        assertNotNull(database.delegate().loadMatchSnapshot(database.getUser("sync-user")));

        MessageEnvelope clearResponse = syncHandler.handle(envelope(MessageTypes.CLEAR_MATCH_SAVE, null));
        assertEquals(MessageTypes.CLEAR_MATCH_SAVE_OK, clearResponse.getType());
        assertNull(database.delegate().loadMatchSnapshot(database.getUser("sync-user")));
    }

    @Test
    void appliesNewsWrite() throws Exception {
        UpdateNewsPayload request = new UpdateNewsPayload();
        UpdateNewsPayload.NewsRow row = new UpdateNewsPayload.NewsRow();
        row.setType("PLANT_UNLOCKED");
        row.setSubject("New plant");
        row.setMessage("Cherry Bomb unlocked");
        row.setCreatedAtMillis(42L);
        row.setRead(false);
        request.setRows(List.of(row));

        MessageEnvelope response = syncHandler.handle(envelope(MessageTypes.UPDATE_NEWS, request));
        assertEquals(MessageTypes.UPDATE_NEWS_OK, response.getType());
        assertFalse(database.getUser("sync-user").getNewsItems().isEmpty());
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
