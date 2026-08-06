package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.ChapterProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionMatchIntegrationTest {

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

    @Test
    void plantAndCheatSpawnAreConsistent() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.ANCIENT_EGYPT);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(1), plantRegistry, zombieRegistry, 3, new Random(7));
        GameSession session = mode.createSession();
        session.setSelectedLoadout(Set.of("Peashooter", "Sunflower"));
        session.setWavesAutoStart(false);
        session.addSunBalance(500);
        session.start();

        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Sunflower", 0, 0, 1));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Peashooter", 1, 0, 1));

        session.spawnZombieOfType("ZombieDefault", 0, 8.5);
        session.nukeAllZombies();
        session.advanceTicks(5);

        assertNotNull(session.renderMap());
        assertTrue(session.getSunBalance() >= 0);
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
    }

    @Test
    void lawnMowerFirstHitClearsRowSecondLoses() {
        GameSession session = new GameSession(plantRegistry, zombieRegistry, 1);
        session.setWavesAutoStart(false);
        session.setWaveManager(new WaveManager(1, 100, List.of("ZombieDefault"), new Random(1)));
        session.start();

        var zombie = session.spawnZombieOfType("ZombieDefault", 0, 0.0);
        session.handleZombieReachedHouse(zombie);
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
        assertTrue(session.getLawnMowers().get(0).isUsed());

        var second = session.spawnZombieOfType("ZombieDefault", 0, 0.0);
        session.handleZombieReachedHouse(second);
        assertEquals(MatchResult.LOST, session.getMatchResult());
    }

    @Test
    void chapterProgressUnlocksNextChapter() {
        ChapterProgress progress = new ChapterProgress();
        assertEquals(ChapterId.ANCIENT_EGYPT, progress.getUnlockedChapter());
        progress.markLevelCompleted(ChapterId.ANCIENT_EGYPT, 1);
        assertEquals(ChapterId.FROSTBITE_CAVES, progress.getUnlockedChapter());
        assertTrue(progress.isLevelCompleted(ChapterId.ANCIENT_EGYPT, 1));
    }

    @Test
    void winWhenAllWavesCleared() {
        GameSession session = new GameSession(plantRegistry, zombieRegistry, 1);
        WaveManager waves = new WaveManager(1, 50, List.of("ZombieDefault"), new Random(9));
        session.setWaveManager(waves);
        session.setWavesAutoStart(false);
        session.start();
        waves.startWaves(session);
        for (int i = 0; i < 30 && session.getMatchResult() == MatchResult.IN_PROGRESS; i++) {
            session.nukeAllZombies();
            session.advanceTicks(1);
        }
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void lawnMowerDuringAdvanceTicksDoesNotThrow() {
        GameSession session = new GameSession(plantRegistry, zombieRegistry, 1);
        session.setWavesAutoStart(false);
        session.setWaveManager(new WaveManager(1, 100, List.of("ZombieDefault"), new Random(1)));
        session.start();

        session.spawnZombieOfType("ZombieDefault", 0, 0.05);
        assertDoesNotThrow(() -> session.advanceTicks(20));
    }
}
