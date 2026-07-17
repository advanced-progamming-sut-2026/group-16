package controller;

import model.quest.QuestTracker;
import model.user.User;
import model.user.UserDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.StayLoggedInStorage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliTravelLogTest {

    private static final Path DATABASE = Path.of("target", "cli-travel-log.db");
    private static final String USERNAME = "travel-log-user";
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
    void travelLogMenuListsDailyMainAndEpicQuests() {
        CommandParser parser = new CommandParser();
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            for (String line : List.of(
                    "register -u " + USERNAME + " -p " + PASSWORD + " " + PASSWORD
                            + " -n TravelPlayer -e travel@example.com -g male",
                    "pick question -q 1 -a fluffy -c fluffy",
                    "login -u " + USERNAME + " -p " + PASSWORD,
                    "menu enter game",
                    "menu travel-log",
                    "travel log page daily",
                    "travel log page main",
                    "travel log page epic",
                    "menu exit"
            )) {
                parser.parseAndExecute(line);
            }
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString();
        assertTrue(output.contains("Travel Log"), output);
        assertTrue(output.contains("daily_sun_3000") || output.contains("آفتاب"), output);
        assertTrue(output.contains("chapter_hunter") || output.contains("شکارچی"), output);
        assertTrue(parser.getCurrentController() instanceof GameController);

        User user = model.App.getInstance().getCurrentUser();
        QuestTracker tracker = user.ensureQuestTracker();
        assertTrue(tracker.getDailyQuests().size() >= 1);
        assertTrue(tracker.getMainQuests().size() >= 1);
        assertTrue(tracker.getEpicQuests().size() >= 1);
    }
}
