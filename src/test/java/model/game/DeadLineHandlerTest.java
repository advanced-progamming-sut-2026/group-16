package model.game;

import model.adventure.AdventureRegistry;
import model.adventure.ChapterConfig;
import model.adventure.ChapterId;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeadLineHandlerTest {

    private PlantRegistry plantRegistry;
    private ZombieRegistry zombieRegistry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");
    }

    private GameSession newDeadLineSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.BIG_WAVE_BEACH);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(3), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setWavesAutoStart(false);
        session.addSunBalance(1000);
        return session;
    }

    @Test
    void onLevelStartActivatesDeadLineOnSession() {
        GameSession session = newDeadLineSession(new Random(1));
        DeadLineHandler handler = new DeadLineHandler(3);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);

        assertTrue(session.isDeadLineActive());
        assertEquals(3, session.getDeadLineColumn());
    }

    @Test
    void zombiePastLineTriggersLoss() {
        GameSession session = newDeadLineSession(new Random(2));
        DeadLineHandler handler = new DeadLineHandler(3);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        session.spawnZombieOfType("ZombieDefault", 0, 2.0);
        handler.onTick(session);

        assertEquals(MatchResult.LOST, session.getMatchResult());
    }

    @Test
    void zombieBeforeLineDoesNotLose() {
        GameSession session = newDeadLineSession(new Random(3));
        DeadLineHandler handler = new DeadLineHandler(3);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        session.spawnZombieOfType("ZombieDefault", 0, 7.0);
        handler.onTick(session);

        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
    }

    @Test
    void breachNotifiesMatchListener() {
        GameSession session = newDeadLineSession(new Random(4));
        AtomicBoolean notified = new AtomicBoolean(false);
        AtomicInteger column = new AtomicInteger(-1);
        AtomicReference<String> zombieType = new AtomicReference<>();
        session.setMatchListener(new MatchListener() {
            @Override
            public void onDeadLineBreached(int breachedColumn, String type) {
                notified.set(true);
                column.set(breachedColumn);
                zombieType.set(type);
            }
        });

        DeadLineHandler handler = new DeadLineHandler(3);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();
        session.spawnZombieOfType("ZombieDefault", 0, 2.0);
        handler.onTick(session);

        assertTrue(notified.get());
        assertEquals(3, column.get());
        assertEquals("ZombieDefault", zombieType.get());
        assertEquals(MatchResult.LOST, session.getMatchResult());
    }
}
