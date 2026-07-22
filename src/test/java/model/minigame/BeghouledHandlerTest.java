package model.minigame;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.MatchResult;
import model.game.entity.plant.Plant;
import model.minigame.beghouled.BeghouledSwapOutcome;
import model.minigame.beghouled.BeghouledSwapResult;
import model.minigame.mode.BeghouledMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeghouledHandlerTest {

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

    private GameSession createStage1() {
        MiniGameStageConfig stage = MiniGameStageConfig.beghouled(1);
        BeghouledMode mode = new BeghouledMode(stage, plantRegistry, zombieRegistry, new Random(1));
        GameSession session = mode.createSession();
        session.start();
        return session;
    }

    @Test
    void reachingMatchTargetWins() {
        GameSession session = createStage1();
        session.getBeghouledBoard().setMatchesMade(session.getBeghouledMatchTarget());
        session.getActiveMiniGameHandler().onTick(session);
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void eatingPlantCreatesCrater() {
        GameSession session = createStage1();
        Plant plant = session.getBoard().getPlantAt(0, 0);
        assertTrue(plant != null);
        int col = plant.getCol();
        int row = plant.getRow();
        session.removePlantFromBoard(plant, true);
        assertTrue(session.getBoard().getTile(col, row).isCrater());
        assertTrue(session.getBeghouledBoard().getGrid().isCrater(col, row));
    }

    @Test
    void validSwapIncreasesSunAndMatches() {
        GameSession session = createStage1();
        Optional<int[]> swap = session.getBeghouledBoard().findAnyValidSwap();
        assertTrue(swap.isPresent(), "expected a valid opening swap");
        int[] cells = swap.get();
        int sunBefore = session.getSunBalance();
        int matchesBefore = session.getBeghouledBoard().getMatchesMade();
        BeghouledSwapResult result = session.trySwapBeghouledPlants(
                cells[0], cells[1], cells[2], cells[3]);
        assertEquals(BeghouledSwapOutcome.SUCCESS, result.outcome());
        assertTrue(result.matchesCleared() >= 1);
        assertTrue(session.getSunBalance() > sunBefore);
        assertTrue(session.getBeghouledBoard().getMatchesMade() > matchesBefore);
    }

    @Test
    void mapShowsMatchesHeader() {
        GameSession session = createStage1();
        String map = session.renderMap();
        assertTrue(map.contains("Matches: 0/8"), map);
    }

    @Test
    void mapShowsCraterTile() {
        GameSession session = createStage1();
        Plant plant = session.getBoard().getPlantAt(2, 2);
        session.removePlantFromBoard(plant, true);
        String map = session.renderMap();
        assertTrue(map.contains("X"), map);
    }
}
