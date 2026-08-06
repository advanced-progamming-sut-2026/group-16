package io.github.finalwave.controller;

import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.github.finalwave.util.StayLoggedInStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CliMiniGameSmokeTest {

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
        unlockRemainingMinigamesForSmoke(user);
    }

    @Test
    @Order(2)
    void walnutBowlingStartsAndPlantsNut() {
        unlockRemainingMinigamesForSmoke(UserDatabase.getInstance().getUser(USERNAME));
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu travel-log");
        parser.parseAndExecute("travel log page minigames");
        parser.parseAndExecute("enter game -n walnut-bowling");
        parser.parseAndExecute("show stages");
        parser.parseAndExecute("start stage -n 1");
        assertInstanceOf(WalnutBowlingController.class, parser.getCurrentController());

        WalnutBowlingController gameplay = (WalnutBowlingController) parser.getCurrentController();
        String plant = gameplay.getSession().getConveyorBeltPlants().getFirst();
        assertDoesNotThrow(() -> {
            parser.parseAndExecute("plant plant -t " + plant + " -l (2, 0)");
            parser.parseAndExecute("show map");
            for (int i = 0; i < 500 && gameplay.getSession().getMatchResult()
                    == io.github.finalwave.model.game.MatchResult.IN_PROGRESS; i++) {
                if (!gameplay.getSession().getZombies().isEmpty()) {
                    gameplay.getSession().nukeAllZombies();
                }
                parser.parseAndExecute("advance time -t 5 ticks");
            }
        });

        assertInstanceOf(MiniGameHubController.class, parser.getCurrentController());
        User user = UserDatabase.getInstance().getUser(USERNAME);
        assertTrue(user.getMiniGameProgress().isStageCompleted(MiniGameId.WALNUT_BOWLING, 1));
    }

    @Test
    @Order(3)
    void iZombieStartsAndPlacesZombie() {
        unlockRemainingMinigamesForSmoke(UserDatabase.getInstance().getUser(USERNAME));
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu travel-log");
        parser.parseAndExecute("travel log page minigames");
        parser.parseAndExecute("enter game -n i-zombie");
        parser.parseAndExecute("show stages");
        parser.parseAndExecute("start stage -n 1");
        assertInstanceOf(IZombieController.class, parser.getCurrentController());

        IZombieController gameplay = (IZombieController) parser.getCurrentController();
        assertDoesNotThrow(() -> {
            parser.parseAndExecute("show zombies roster");
            parser.parseAndExecute("place zombie -t ZombieImp -l (5, 0)");
            parser.parseAndExecute("show map");
            for (int row = 0; row < 5; row++) {
                var zombie = gameplay.getSession().spawnZombieOfType("ZombieDefault", row, 0.1);
                gameplay.getSession().handleZombieReachedHouse(zombie);
            }
            parser.parseAndExecute("advance time -t 1 ticks");
        });

        assertInstanceOf(MiniGameHubController.class, parser.getCurrentController());
        User user = UserDatabase.getInstance().getUser(USERNAME);
        assertTrue(user.getMiniGameProgress().isStageCompleted(MiniGameId.I_ZOMBIE, 1));
    }

    @Test
    @Order(4)
    void zombotanyStartsPlantsAndCompletesStage() {
        unlockRemainingMinigamesForSmoke(UserDatabase.getInstance().getUser(USERNAME));
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu travel-log");
        parser.parseAndExecute("travel log page minigames");
        parser.parseAndExecute("enter game -n zombotany");
        parser.parseAndExecute("show stages");
        parser.parseAndExecute("start stage -n 1");
        assertInstanceOf(ZombotanyController.class, parser.getCurrentController());

        ZombotanyController gameplay = (ZombotanyController) parser.getCurrentController();
        assertDoesNotThrow(() -> {
            parser.parseAndExecute("show sun amount");
            parser.parseAndExecute("plant plant -t Sunflower -l (1, 0)");
            parser.parseAndExecute("show map");
            for (int i = 0; i < 500 && gameplay.getSession().getMatchResult()
                    == io.github.finalwave.model.game.MatchResult.IN_PROGRESS; i++) {
                if (!gameplay.getSession().getZombies().isEmpty()) {
                    gameplay.getSession().nukeAllZombies();
                }
                parser.parseAndExecute("advance time -t 5 ticks");
            }
        });

        assertInstanceOf(MiniGameHubController.class, parser.getCurrentController());
        User user = UserDatabase.getInstance().getUser(USERNAME);
        assertTrue(user.getMiniGameProgress().isStageCompleted(MiniGameId.ZOMBOTANY, 1));
    }

    @Test
    @Order(5)
    void beghouledStartsSwapsAndCompletesStage() {
        unlockRemainingMinigamesForSmoke(UserDatabase.getInstance().getUser(USERNAME));
        CommandParser parser = new CommandParser();
        parser.parseAndExecute("menu enter login");
        parser.parseAndExecute("login -u " + USERNAME + " -p " + PASSWORD);
        parser.parseAndExecute("menu enter game");
        parser.parseAndExecute("menu travel-log");
        parser.parseAndExecute("travel log page minigames");
        parser.parseAndExecute("enter game -n beghouled");
        parser.parseAndExecute("show stages");
        parser.parseAndExecute("start stage -n 1");
        assertInstanceOf(BeghouledController.class, parser.getCurrentController());

        BeghouledController gameplay = (BeghouledController) parser.getCurrentController();
        assertDoesNotThrow(() -> {
            parser.parseAndExecute("show upgrades");
            parser.parseAndExecute("show map");
            var swap = gameplay.getSession().getBeghouledBoard().findAnyValidSwap();
            assertTrue(swap.isPresent());
            int[] cells = swap.get();
            parser.parseAndExecute("swap plants -a (" + cells[0] + ", " + cells[1]
                    + ") -b (" + cells[2] + ", " + cells[3] + ")");
            gameplay.getSession().getBeghouledBoard()
                    .setMatchesMade(gameplay.getSession().getBeghouledMatchTarget());
            parser.parseAndExecute("advance time -t 1 ticks");
        });

        assertInstanceOf(MiniGameHubController.class, parser.getCurrentController());
        User user = UserDatabase.getInstance().getUser(USERNAME);
        assertTrue(user.getMiniGameProgress().isStageCompleted(MiniGameId.BEGHOULED, 1));
    }

    private static void unlockRemainingMinigamesForSmoke(User user) {
        if (user == null) {
            return;
        }
        UnlockService unlockService = new UnlockService();
        boolean changed = false;
        for (MiniGameId id : MiniGameId.values()) {
            if (unlockService.unlockMinigame(user, id.getKey())) {
                changed = true;
            }
        }
        if (changed) {
            UserDatabase.getInstance().saveUserWallet(user);
        }
    }
}
