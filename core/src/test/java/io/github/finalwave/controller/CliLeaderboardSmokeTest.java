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
class CliLeaderboardSmokeTest {

    private static final Path DATABASE = Path.of("target", "cli-leaderboard-smoke.db");
    private static final String USERNAME = "cli-leaderboard-user";
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
    void leaderboardFromMainMenuSupportsSortAndRefresh() {
        CommandParser parser = new CommandParser();
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            assertDoesNotThrow(() -> {
                for (String line : registerLoginAndLeaderboardCommands()) {
                    parser.parseAndExecute(line);
                }
            });
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString();
        assertTrue(output.contains("Leaderboard"), output);
        assertTrue(output.contains(USERNAME), output);
        assertTrue(output.contains("BestScore"), output);
        assertTrue(output.contains("Sorted by score"), output);
        assertInstanceOf(MainMenuController.class, parser.getCurrentController());
    }

    @Test
    @Order(2)
    void gameMenuLeaderboardAndScoreGameStub() {
        CommandParser parser = new CommandParser();
        loginExistingUser(parser);

        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu leaderboard");
        assertInstanceOf(LeaderboardController.class, parser.getCurrentController());
        parser.parseAndExecute("menu exit");
        assertInstanceOf(GameController.class, parser.getCurrentController());
        parser.parseAndExecute("menu exit");

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            parser.parseAndExecute("menu enter score-game");
            assertInstanceOf(ScoreGameController.class, parser.getCurrentController());
            parser.parseAndExecute("menu exit");
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString();
        assertTrue(output.contains("Score game"), output);
        assertTrue(output.contains("Best meowpoint:"), output);
        assertInstanceOf(MainMenuController.class, parser.getCurrentController());
    }

    private static List<String> registerLoginAndLeaderboardCommands() {
        return List.of(
                "register -u " + USERNAME + " -p " + PASSWORD + " " + PASSWORD
                        + " -n BoardPlayer -e board@example.com -g male",
                "pick question -q 1 -a fluffy -c fluffy",
                "login -u " + USERNAME + " -p " + PASSWORD,
                "menu enter leaderboard",
                "sort -c score",
                "refresh",
                "menu exit");
    }

    private static void loginExistingUser(CommandParser parser) {
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
    }
}
