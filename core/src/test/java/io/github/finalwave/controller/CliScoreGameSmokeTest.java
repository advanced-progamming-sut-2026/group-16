package io.github.finalwave.controller;

import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.github.finalwave.util.StayLoggedInStorage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CliScoreGameSmokeTest {

    private static final Path DATABASE = Path.of("target", "cli-score-game-smoke.db");
    private static final String USERNAME = "cli-score-user";
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
    void scoreGameFlowAwardsMeowpointAndUpdatesBest() {
        CommandParser parser = new CommandParser();
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            assertDoesNotThrow(() -> {
                for (String line : scoreGameFlowCommands()) {
                    parser.parseAndExecute(line);
                }
            });
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString();
        assertTrue(output.contains("Score game"), output);
        assertTrue(output.contains("Best meowpoint:"), output);
        assertTrue(output.contains("The Game Started!") || output.contains("Game Started"), output);
        assertTrue(output.contains("Meowpoint:"), output);
        assertTrue(output.contains("Could not submit score"), output);
        assertInstanceOf(ScoreGameController.class, parser.getCurrentController());
    }

    @Test
    @Order(2)
    void exitReturnsToMainMenu() {
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        parser.parseAndExecute("menu enter score-game");
        assertInstanceOf(ScoreGameController.class, parser.getCurrentController());
        parser.parseAndExecute("menu exit");
        assertInstanceOf(MainMenuController.class, parser.getCurrentController());
    }

    private static List<String> scoreGameFlowCommands() {
        return List.of(
                "register -u " + USERNAME + " -p " + PASSWORD + " " + PASSWORD
                        + " -n ScorePlayer -e scorecli@example.com -g male",
                "pick question -q 1 -a fluffy -c fluffy",
                "login -u " + USERNAME + " -p " + PASSWORD,
                "menu enter score-game",
                "start",
                "show available plants",
                "add plant -t Sunflower",
                "add plant -t Peashooter",
                "start game",
                "cheat add -n 500 suns",
                "plant plant -t Sunflower -l (0, 0)",
                "plant plant -t Peashooter -l (1, 0)",
                "release the nuke",
                "advance time -t 5 ticks",
                "release the nuke",
                "advance time -t 5 ticks",
                "release the nuke",
                "advance time -t 20 ticks"
        );
    }
}
