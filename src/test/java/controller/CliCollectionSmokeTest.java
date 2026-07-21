package controller;

import model.App;
import model.user.User;
import model.user.UserDatabase;
import org.junit.jupiter.api.*;
import util.StayLoggedInStorage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CliCollectionSmokeTest {

    private static final Path DATABASE = Path.of("target", "cli-collection-smoke.db");
    private static final String USERNAME = "cli-collection-user";
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
    void collectionMenuFlowCoversDocCommands() {
        CommandParser parser = new CommandParser();
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            assertDoesNotThrow(() -> {
                for (String line : collectionFlowCommands()) {
                    parser.parseAndExecute(line);
                }
            });
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString();
        assertTrue(output.contains("collection menu"), output);
        assertTrue(output.contains("Owned plants:"), output);
        assertTrue(output.contains("All plants:"), output);
        assertTrue(output.contains("Seen zombies:"), output);
        assertTrue(output.contains("All zombies:"), output);
        assertTrue(output.contains("Plant: Peashooter"), output);
        assertTrue(output.contains("Successfully purchased Cherry Bomb"), output);
        assertTrue(output.contains("don't have enough seed packets"), output);

        User user = App.getInstance().getCurrentUser();
        assertNotNull(user);
        assertTrue(user.getPlantProgress().isOwned("Cherry Bomb"));
        assertEquals(3000, user.getCoins());
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());
        assertNotNull(UserDatabase.getInstance().getUser(USERNAME));
    }

    @Test
    @Order(2)
    void collectionMenuExitReturnsToGameMenu() {
        CommandParser parser = new CommandParser();
        loginExistingUser(parser);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu enter collection");
        assertInstanceOf(CollectionController.class, parser.getCurrentController());
        parser.parseAndExecute("menu exit");
        assertInstanceOf(GameController.class, parser.getCurrentController());
    }

    @Test
    @Order(3)
    void showZombieRejectsUnseenZombie() {
        CommandParser parser = new CommandParser();
        loginExistingUser(parser);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            parser.parseAndExecute("menu enter game");
            parser.parseAndExecute("menu enter collection");
            parser.parseAndExecute("menu collection show-zombie -z ZombieDefault");
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(captured.toString().contains("has not been seen yet"));
        assertFalse(App.getInstance().getCurrentUser().getUnlockedZombies().contains("ZombieDefault"));
    }

    @Test
    @Order(4)
    void purchaseFailsWithoutEnoughCoins() {
        CommandParser parser = new CommandParser();
        registerFreshUser(parser, "cli-collection-poor", "poor@example.com");

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            parser.parseAndExecute("menu enter game");
            parser.parseAndExecute("menu enter collection");
            parser.parseAndExecute("menu collection purchase-plant -p Cherry Bomb");
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(captured.toString().contains("don't have enough coins to purchase"));
        assertFalse(App.getInstance().getCurrentUser().getPlantProgress().isOwned("Cherry Bomb"));
    }

    private static List<String> collectionFlowCommands() {
        return List.of(
                "register -u " + USERNAME + " -p " + PASSWORD + " " + PASSWORD
                        + " -n CollectionPlayer -e collection@example.com -g male",
                "pick question -q 1 -a fluffy -c fluffy",
                "login -u " + USERNAME + " -p " + PASSWORD,
                "menu enter game",
                "menu cheat add 5000 coin",
                "menu enter collection",
                "menu collection show-plants",
                "menu collection show-all-plants",
                "menu collection show-zombies",
                "menu collection show-all-zombies",
                "menu collection show-plant -p Peashooter",
                "menu collection purchase-plant -p Cherry Bomb",
                "menu collection upgrade-plant -p Peashooter",
                "menu exit",
                "menu exit",
                "menu logout"
        );
    }

    private static void loginExistingUser(CommandParser parser) {
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
    }

    private static void registerFreshUser(CommandParser parser, String username, String email) {
        parser.parseAndExecute("register -u " + username + " -p " + PASSWORD + " " + PASSWORD
                + " -n FreshUser -e " + email + " -g male");
        parser.parseAndExecute("pick question -q 1 -a fluffy -c fluffy");
        parser.parseAndExecute("login -u " + username + " -p " + PASSWORD);
    }
}
