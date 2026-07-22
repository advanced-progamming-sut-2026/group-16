package controller;

import model.minigame.MiniGameId;
import model.user.User;
import model.user.UserDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import util.StayLoggedInStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CliVaseBreakerSmokeTest {

    private static final Path DATABASE = Path.of("target", "cli-vasebreaker-smoke.db");
    private static final String USERNAME = "vase-smoke-user";
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
    void travelLogOpensMiniGameHubAndStartsVasebreaker() {
        CommandParser parser = new CommandParser();
        assertInstanceOf(RegistrationController.class, parser.getCurrentController());

        List<String> setup = List.of(
                "register -u " + USERNAME + " -p " + PASSWORD + " " + PASSWORD
                        + " -n VasePlayer -e vase@example.com -g male",
                "pick question -q 1 -a fluffy -c fluffy",
                "login -u " + USERNAME + " -p " + PASSWORD,
                "menu enter game",
                "menu travel-log",
                "travel log page minigames"
        );
        assertDoesNotThrow(() -> setup.forEach(parser::parseAndExecute));
        assertInstanceOf(MiniGameHubController.class, parser.getCurrentController());

        parser.parseAndExecute("enter game -n vase-breaker");
        parser.parseAndExecute("show stages");
        parser.parseAndExecute("start stage -n 1");
        assertInstanceOf(VaseBreakerController.class, parser.getCurrentController());

        VaseBreakerController gameplay = (VaseBreakerController) parser.getCurrentController();
        List<int[]> vaseCells = new ArrayList<>();
        for (var vase : gameplay.getSession().getVases()) {
            vaseCells.add(new int[]{vase.getCol(), vase.getRow()});
        }

        assertDoesNotThrow(() -> {
            parser.parseAndExecute("show map");
            parser.parseAndExecute("zombies info");
            for (int[] cell : vaseCells) {
                parser.parseAndExecute("smash vase -l (" + cell[0] + ", " + cell[1] + ")");
            }
            parser.parseAndExecute("release the nuke");
        });

        assertInstanceOf(MiniGameHubController.class, parser.getCurrentController());
        User user = UserDatabase.getInstance().getUser(USERNAME);
        assertTrue(user.getMiniGameProgress().isStageCompleted(MiniGameId.VASE_BREAKER, 1));
    }

    @Test
    @Order(2)
    void stubMiniGamesShowComingSoon() {
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu travel-log");
        parser.parseAndExecute("travel log page minigames");
        parser.parseAndExecute("enter game -n walnut-bowling");
        assertInstanceOf(WalnutBowlingController.class, parser.getCurrentController());
        parser.parseAndExecute("menu exit");
        assertInstanceOf(MiniGameHubController.class, parser.getCurrentController());
    }
}
