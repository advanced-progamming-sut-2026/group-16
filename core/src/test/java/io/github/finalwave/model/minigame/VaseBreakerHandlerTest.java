package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.model.minigame.handler.VaseBreakerHandler;
import io.github.finalwave.model.minigame.mode.VaseBreakerMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VaseBreakerHandlerTest {

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
    void smashingAllEmptyVasesWinsImmediately() {
        MiniGameStageConfig stage = MiniGameStageConfig.vaseBreaker(1);
        VaseBreakerMode mode = new VaseBreakerMode(stage, plantRegistry, zombieRegistry, new Random(7));
        GameSession session = mode.createSession();
        session.start();

        List<Vase> snapshot = new ArrayList<>(session.getVases());
        for (Vase vase : snapshot) {
            if (vase.getContent() == Vase.Content.ZOMBIE
                    || vase.getContent() == Vase.Content.GARGANTUAR) {
                session.smashVase(vase.getCol(), vase.getRow());
            }
        }
        session.nukeAllZombies();
        for (Vase vase : new ArrayList<>(session.getVases())) {
            session.smashVase(vase.getCol(), vase.getRow());
        }
        assertTrue(session.areAllVasesSmashed());
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void livingZombiesBlockWinUntilCleared() {
        GameSession session = new GameSession(
                plantRegistry, new io.github.finalwave.model.game.board.GameBoard(), 0, zombieRegistry, 1, new Random(1));
        session.setActiveMiniGameHandler(new VaseBreakerHandler());
        session.start();
        session.addVase(new Vase("only", 2, 0, Vase.Content.EMPTY, null));
        session.spawnZombieOfType("ZombieDefault", 0, 5.0);
        session.smashVase(2, 0);
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
        session.nukeAllZombies();
        session.getActiveMiniGameHandler().onTick(session);
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void lawnMowerExhaustionCausesLoss() {
        GameSession session = new GameSession(
                plantRegistry, new io.github.finalwave.model.game.board.GameBoard(), 0, zombieRegistry, 1, new Random(2));
        session.setActiveMiniGameHandler(new VaseBreakerHandler());
        session.start();
        session.addVase(new Vase("v", 5, 0, Vase.Content.EMPTY, null));

        var zombie1 = session.spawnZombieOfType("ZombieDefault", 0, 0.1);
        session.handleZombieReachedHouse(zombie1);
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());

        var zombie2 = session.spawnZombieOfType("ZombieDefault", 0, 0.1);
        session.handleZombieReachedHouse(zombie2);
        assertEquals(MatchResult.LOST, session.getMatchResult());
    }
}
