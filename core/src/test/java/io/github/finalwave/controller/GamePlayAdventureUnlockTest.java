package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.NewsType;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.github.finalwave.view.cli.GamePlayViewCli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamePlayAdventureUnlockTest {
    private static final Path DATABASE = Path.of("target", "adventure-unlock-news-test.db");

    @BeforeAll
    static void configureDatabase() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
    }

    @AfterAll
    static void cleanUpDatabase() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void winningEgyptLevel1PublishesLevelAndMinigameNewsOnce() throws IOException {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "adv-unlock",
                "password-hash",
                "Adv Unlock",
                "adv-unlock@example.com",
                Gender.MALE);
        database.registerUser(user);

        assertTrue(user.getUnlockedMinigames().contains(MiniGameId.VASE_BREAKER.getKey()));
        assertTrue(user.getUnlockedLevels().contains("1-1"));
        assertTrue(user.getNewsItems().isEmpty());

        TestableGamePlayController controller = createController(user, database, ChapterId.ANCIENT_EGYPT);
        controller.finish(MatchResult.WON);

        assertTrue(user.getUnlockedLevels().contains("2-1"));
        assertTrue(user.getUnlockedMinigames().contains(MiniGameId.WALNUT_BOWLING.getKey()));
        assertEquals(2, user.getNewsItems().size());
        assertTrue(user.getNewsItems().stream()
                .anyMatch(n -> n.getType() == NewsType.LEVEL_UNLOCKED && "2-1".equals(n.getSubject())));
        assertTrue(user.getNewsItems().stream()
                .anyMatch(n -> n.getType() == NewsType.MINIGAME_UNLOCKED
                        && MiniGameId.WALNUT_BOWLING.getKey().equals(n.getSubject())));

        int newsCount = user.getNewsItems().size();
        controller.finish(MatchResult.WON);
        assertEquals(newsCount, user.getNewsItems().size());
    }

    @Test
    void winningLastChapterLevel1UnlocksFinalMinigame() throws IOException {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "adv-final",
                "password-hash",
                "Adv Final",
                "adv-final@example.com",
                Gender.FEMALE);
        database.registerUser(user);
        user.getChapterProgress().setUnlockedChapter(ChapterId.DARK_AGES);

        TestableGamePlayController controller = createController(user, database, ChapterId.DARK_AGES);
        controller.finish(MatchResult.WON);

        assertTrue(user.getUnlockedMinigames().contains(MiniGameId.ZOMBOTANY.getKey()));
        assertTrue(user.getNewsItems().stream()
                .anyMatch(n -> n.getType() == NewsType.MINIGAME_UNLOCKED
                        && MiniGameId.ZOMBOTANY.getKey().equals(n.getSubject())));
        assertFalse(user.getNewsItems().stream()
                .anyMatch(n -> n.getType() == NewsType.LEVEL_UNLOCKED));
    }

    @Test
    void levelIdUsesChapterOrdinalPlusOne() {
        assertEquals("1-1", GamePlayController.levelId(ChapterId.ANCIENT_EGYPT));
        assertEquals("2-1", GamePlayController.levelId(ChapterId.FROSTBITE_CAVES));
        assertEquals("4-1", GamePlayController.levelId(ChapterId.DARK_AGES));
    }

    private static TestableGamePlayController createController(User user,
                                                               UserDatabase database,
                                                               ChapterId chapterId)
            throws IOException {
        PlantRegistry plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        ZombieRegistry zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");

        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(chapterId);
        LevelConfig level = chapter.getLevel(1);
        AdventureMode mode = new AdventureMode(
                chapter, level, plantRegistry, zombieRegistry, 3, new Random(1));
        GameSession session = mode.createSession();
        session.setSelectedLoadout(Set.of("Peashooter", "Sunflower"));
        session.setWavesAutoStart(false);

        TestableGamePlayController controller = new TestableGamePlayController(
                user, database, mode, session, chapter, level);
        CommandParser parser = new CommandParser();
        controller.setParser(parser);
        controller.setView(new GamePlayViewCli());
        return controller;
    }

    private static final class TestableGamePlayController extends GamePlayController {
        TestableGamePlayController(User user,
                                   UserDatabase userDatabase,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level) {
            super(user, userDatabase, null, adventureMode, session, chapter, level, Set.of(), true);
        }

        void finish(MatchResult result) {
            onMatchFinished(result);
        }
    }
}
