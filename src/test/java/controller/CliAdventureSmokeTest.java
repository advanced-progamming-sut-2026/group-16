package controller;

import model.App;
import model.adventure.ChapterId;
import model.user.User;
import model.user.UserDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import util.StayLoggedInStorage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CliAdventureSmokeTest {

    private static final Path DATABASE = Path.of("target", "cli-adventure-smoke.db");
    private static final String USERNAME = "cli-smoke-user";
    private static final String PASSWORD = "Passw0rd!";

    @BeforeAll
    static void setUpDatabase() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        StayLoggedInStorage.clear();
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
    }

    @AfterAll
    static void tearDown() throws Exception {
        StayLoggedInStorage.clear();
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    @Order(1)
    void adventureNormalLevelFlowRunsWithoutCrashing() {
        CommandParser parser = new CommandParser();
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());

        List<String> inputs = List.of(
                "register -u " + USERNAME + " -p " + PASSWORD + " " + PASSWORD
                        + " -n SmokePlayer -e smoke@example.com -g male",
                "pick question -q 1 -a fluffy -c fluffy",
                "login -u " + USERNAME + " -p " + PASSWORD,
                "menu enter game",
                "menu enter chapter -c Ancient Egypt",
                "show levels",
                "start level -n 1",
                "show available plants",
                "add plant -t Sunflower",
                "add plant -t Peashooter",
                "start game",
                "show map",
                "show sun amount",
                "cheat add -n 500 suns",
                "plant plant -t Sunflower -l (0, 0)",
                "plant plant -t Peashooter -l (1, 0)",
                "show plants status",
                "advance time -t 50 ticks",
                "show map",
                "zombies info",
                "release the nuke",
                "advance time -t 5 ticks",
                "show map",
                "menu exit",
                "menu exit",
                "menu exit",
                "menu logout"
        );

        assertDoesNotThrow(() -> {
            for (String line : inputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(RegistrationController.class, parser.getCurrentController());
        assertNotNull(UserDatabase.getInstance().getUser(USERNAME));
    }

    @Test
    @Order(2)
    void conveyorBeltLevelSkipsPlantSelectionAndEntersGameplayDirectly() {
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu enter chapter -c Ancient Egypt");
        parser.parseAndExecute("start level -n 2");

        assertInstanceOf(ConveyBeltLevelController.class, parser.getCurrentController());

        List<String> inputs = List.of(
                "show map",
                "show sun amount",
                "cheat add -n 500 suns",
                "plant plant -t Peashooter -l (0, 0)",
                "advance time -t 130 ticks",
                "show map",
                "menu exit"
        );

        assertDoesNotThrow(() -> {
            for (String line : inputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(AdventureController.class, parser.getCurrentController());
    }

    @Test
    @Order(3)
    void lockedPlantsLevelKeepsSelectionWithRestrictionsAndEntersGameplay() {
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu cheat add 10000 coins");
        parser.parseAndExecute("menu enter collection");
        parser.parseAndExecute("menu collection purchase-plant -p Twin Sunflower");
        parser.parseAndExecute("menu collection purchase-plant -p Repeater");
        parser.parseAndExecute("menu exit");
        parser.parseAndExecute("menu enter chapter -c Ancient Egypt");
        parser.parseAndExecute("start level -n 3");

        assertInstanceOf(LockedPlantsSelectionController.class, parser.getCurrentController());

        List<String> selectionInputs = List.of(
                "show available plants",
                "add plant -t Peashooter",
                "add plant -t Repeater",
                "add plant -t Wall-nut"
        );

        assertDoesNotThrow(() -> {
            for (String line : selectionInputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(LockedPlantsSelectionController.class, parser.getCurrentController());
        parser.parseAndExecute("start game");
        assertInstanceOf(LockedPlantsLevelController.class, parser.getCurrentController());

        List<String> gameplayInputs = List.of(
                "show map",
                "cheat add -n 500 suns",
                "plant plant -t Wall-nut -l (0, 0)",
                "menu exit"
        );

        assertDoesNotThrow(() -> {
            for (String line : gameplayInputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(AdventureController.class, parser.getCurrentController());
    }

    @Test
    @Order(4)
    void saveOurSeedsLevelPlacesProtectedSeedsAndEntersGameplay() {
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);

        User user = App.getInstance().getCurrentUser();
        assertNotNull(user);
        user.getChapterProgress().setUnlockedChapter(ChapterId.FROSTBITE_CAVES);
        UserDatabase.getInstance().saveAdventureProgress(user);

        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu enter chapter -c Frostbite Caves");
        parser.parseAndExecute("start level -n 2");
        assertInstanceOf(PlantSelectionController.class, parser.getCurrentController());

        List<String> selectionInputs = List.of(
                "show available plants",
                "add plant -t Sunflower",
                "add plant -t Peashooter",
                "start game"
        );
        assertDoesNotThrow(() -> {
            for (String line : selectionInputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(SaveOurSeedsLevelController.class, parser.getCurrentController());

        List<String> gameplayInputs = List.of(
                "show map",
                "cheat add -n 500 suns",
                "plant plant -t Peashooter -l (0, 0)",
                "menu exit"
        );
        assertDoesNotThrow(() -> {
            for (String line : gameplayInputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(AdventureController.class, parser.getCurrentController());
    }

    @Test
    @Order(5)
    void timedWarLevelEntersGameplayWithTimerStatus() {
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);

        User user = App.getInstance().getCurrentUser();
        assertNotNull(user);
        user.getChapterProgress().setUnlockedChapter(ChapterId.FROSTBITE_CAVES);
        UserDatabase.getInstance().saveAdventureProgress(user);

        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu enter chapter -c Frostbite Caves");
        parser.parseAndExecute("start level -n 3");
        assertInstanceOf(PlantSelectionController.class, parser.getCurrentController());

        List<String> selectionInputs = List.of(
                "show available plants",
                "add plant -t Sunflower",
                "add plant -t Peashooter",
                "start game"
        );
        assertDoesNotThrow(() -> {
            for (String line : selectionInputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(TimedWarLevelController.class, parser.getCurrentController());

        List<String> gameplayInputs = List.of(
                "show map",
                "cheat add -n 500 suns",
                "plant plant -t Peashooter -l (0, 0)",
                "menu exit"
        );
        assertDoesNotThrow(() -> {
            for (String line : gameplayInputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(AdventureController.class, parser.getCurrentController());
    }

    @Test
    @Order(6)
    void nightOpsLevelUsesDedicatedControllerAndRunsWithoutCrashing() {
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        App.getInstance().getCurrentUser().getChapterProgress()
                .setUnlockedChapter(ChapterId.BIG_WAVE_BEACH);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu enter chapter -c Big Wave Beach");
        parser.parseAndExecute("start level -n 2");

        assertInstanceOf(PlantSelectionController.class, parser.getCurrentController());

        List<String> inputs = List.of(
                "show available plants",
                "add plant -t Sunflower",
                "add plant -t Peashooter",
                "start game"
        );

        assertDoesNotThrow(() -> {
            for (String line : inputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(NightOpsLevelController.class, parser.getCurrentController());

        List<String> gameplayInputs = List.of(
                "show map",
                "show sun amount",
                "plant plant -t Sunflower -l (0, 0)",
                "advance time -t 240 ticks",
                "show sun amount",
                "menu exit"
        );

        assertDoesNotThrow(() -> {
            for (String line : gameplayInputs) {
                parser.parseAndExecute(line);
            }
        });

        assertInstanceOf(AdventureController.class, parser.getCurrentController());
    }

    @Test
    @Order(7)
    void deadLineLevelLosesWhenZombieCrossesLine() {
        CommandParser parser = new CommandParser();
        loginAndUnlockBigWaveBeach(parser);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu enter chapter -c Big Wave Beach");
        parser.parseAndExecute("start level -n 3");
        parser.parseAndExecute("add plant -t Sunflower");
        parser.parseAndExecute("add plant -t Peashooter");
        parser.parseAndExecute("start game");

        assertInstanceOf(DeadLineLevelController.class, parser.getCurrentController());

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            parser.parseAndExecute("show map");
            parser.parseAndExecute("cheat spawn-zombie -t ZombieDefault -l 2, 0");
            parser.parseAndExecute("advance time -t 1 ticks");
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString();
        assertTrue(output.contains("Dead line"), output);
        assertTrue(output.contains("crossed the dead line"), output);
        assertInstanceOf(AdventureController.class, parser.getCurrentController());
    }

    @Test
    @Order(8)
    void loveYourPlantsLevelShowsRuleAndTracksPlantLoss() {
        CommandParser parser = new CommandParser();
        loginAndUnlockChapter(parser, ChapterId.DARK_AGES);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu enter chapter -c Dark Ages");
        parser.parseAndExecute("start level -n 2");
        parser.parseAndExecute("add plant -t Sunflower");
        parser.parseAndExecute("add plant -t Peashooter");
        parser.parseAndExecute("start game");

        assertInstanceOf(LoveYourPlantsLevelController.class, parser.getCurrentController());

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            parser.parseAndExecute("show map");
            parser.parseAndExecute("cheat add -n 500 suns");
            parser.parseAndExecute("plant plant -t Sunflower -l (4, 2)");
            parser.parseAndExecute("cheat spawn-zombie -t ZombieDefault -l 5, 2");
            parser.parseAndExecute("advance time -t 400 ticks");
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString();
        assertTrue(output.contains("Plants lost: 0/5"), output);
        assertTrue(output.contains("Plants lost: 1/5") || output.contains("destroyed"),
                output);
    }

    private static void loginAndUnlockBigWaveBeach(CommandParser parser) {
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        if (!(parser.getCurrentController() instanceof MainMenuController)) {
            parser.parseAndExecute("register -u " + USERNAME + " -p " + PASSWORD + " " + PASSWORD
                    + " -n SmokePlayer -e smoke@example.com -g male");
            parser.parseAndExecute("pick question -q 1 -a fluffy -c fluffy");
            parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        }
        User user = App.getInstance().getCurrentUser();
        user.getChapterProgress().setUnlockedChapter(ChapterId.BIG_WAVE_BEACH);
        UserDatabase.getInstance().saveAdventureProgress(user);
    }

    private static void loginAndUnlockChapter(CommandParser parser, ChapterId chapterId) {
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        if (!(parser.getCurrentController() instanceof MainMenuController)) {
            parser.parseAndExecute("register -u " + USERNAME + " -p " + PASSWORD + " " + PASSWORD
                    + " -n SmokePlayer -e smoke@example.com -g male");
            parser.parseAndExecute("pick question -q 1 -a fluffy -c fluffy");
            parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        }
        User user = App.getInstance().getCurrentUser();
        assertNotNull(user);
        user.getChapterProgress().setUnlockedChapter(chapterId);
        UserDatabase.getInstance().saveAdventureProgress(user);
    }
}
