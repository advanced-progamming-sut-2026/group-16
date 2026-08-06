package io.github.finalwave.model.quest;

import io.github.finalwave.model.App;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.quest.event.GameEvent;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.finalwave.util.HashUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestMatchIntegrationTest {

    private static final Path DATABASE = Path.of("target", "quest-match-integration.db");

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
    }

    @AfterEach
    void tearDown() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void sessionSunCollectCompletesQuestAndAppliesCoinReward() {
        User user = new User("quest-player", HashUtil.hashSHA256("Passw0rd!"),
                "QP", "quest@example.com", Gender.MALE);
        user.setSecurityQuestionId(1);
        user.setSecurityAnswerHash(HashUtil.hashSHA256("a"));
        UserDatabase.getInstance().registerUser(user);

        int coinsBefore = user.getCoins();
        QuestTracker tracker = QuestService.createTrackerFor(user, null);
        Quest sunQuest = QuestFactory.createDailySunCollector(3000);
        tracker.setQuests(java.util.List.of(sunQuest));

        PlantRegistry registry = App.getInstance().getPlantRegistry();
        GameSession session = new GameSession(registry, 50);
        tracker.registerOn(session.getEventBus());
        tracker.beginSession(session);
        session.attachQuestTracker(tracker);
        session.start();

        session.getEventBus().publish(new GameEvent.SunCollected(3000));

        assertTrue(sunQuest.isCompleted());
        assertTrue(sunQuest.isRewardClaimed());
        assertEquals(coinsBefore + 30, user.getCoins());
    }

    @Test
    void zombieKillCarriesKillerPlantTypeAndWaveRelativeTime() {
        PlantRegistry registry = App.getInstance().getPlantRegistry();
        GameSession session = new GameSession(registry, 50);
        session.setChapterId("ancient-egypt");
        session.start();
        session.markWaveStarted();
        session.advanceTicks(20);

        AtomicReference<GameEvent.ZombieKilled> captured = new AtomicReference<>();
        session.getEventBus().subscribe(event -> {
            if (event instanceof GameEvent.ZombieKilled zk) {
                captured.set(zk);
            }
        });

        Zombie zombie = new Zombie.Builder("Basic")
                .maxHealth(10)
                .position(5.0, 0)
                .build();
        zombie.takeDirectDamage(999);
        session.handleZombieKilled(zombie, "Peashooter");

        assertNotNull(captured.get());
        assertEquals("Peashooter", captured.get().killerPlantType());
        assertEquals("ancient-egypt", captured.get().chapterId());
        assertEquals(2.0, captured.get().secondsSinceWaveStart(), 0.01);
    }

    @Test
    void gameFinishedReportsPlantsLostAndNightFlagOnStart() {
        PlantRegistry registry = App.getInstance().getPlantRegistry();
        GameSession session = new GameSession(registry, 50);
        session.setNightLevel(true);
        session.setLevelId("dark-ages-L1");

        AtomicReference<GameEvent.GameStarted> started = new AtomicReference<>();
        AtomicReference<GameEvent.GameFinished> finished = new AtomicReference<>();
        session.getEventBus().subscribe(event -> {
            if (event instanceof GameEvent.GameStarted gs) {
                started.set(gs);
            } else if (event instanceof GameEvent.GameFinished gf) {
                finished.set(gf);
            }
        });

        session.start();
        assertNotNull(started.get());
        assertTrue(started.get().isNightLevel());
        assertEquals("dark-ages-L1", started.get().levelId());

        session.loseMatch();
        assertNotNull(finished.get());
        assertEquals(0, finished.get().plantsLost());
    }
}
