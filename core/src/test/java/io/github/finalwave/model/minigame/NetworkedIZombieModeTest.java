package io.github.finalwave.model.minigame;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.minigame.mode.NetworkedIZombieMode;
import io.github.finalwave.network.match.MatchSnapshotApplier;
import io.github.finalwave.network.match.MatchSnapshotBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkedIZombieModeTest {

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
    void hostCanPlantAndBuildSnapshotForGuest() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombie(1);
        NetworkedIZombieMode mode = new NetworkedIZombieMode(stage, plantRegistry, zombieRegistry, new Random(7L));

        GameSession host = mode.createHostSession();
        host.start();
        host.advanceTicks(3);

        PlantPlacementResult planted = host.tryPlant("Peashooter", 2, 2, 1);
        assertEquals(PlantPlacementResult.SUCCESS, planted);
        assertTrue(host.getBoard().getAllPlants().size() >= 1);

        var payload = MatchSnapshotBuilder.build(host, "match-test");
        GameSession guest = mode.createGuestSession();
        guest.start();
        MatchSnapshotApplier.apply(guest, payload);

        assertEquals(host.getBoard().getAllPlants().size(), guest.getBoard().getAllPlants().size());
        assertEquals(MatchResult.IN_PROGRESS, guest.getMatchResult());
    }

    @Test
    void guestSunTracksSnapshotWithoutOverwritingPlantSun() {
        MiniGameStageConfig stage = MiniGameStageConfig.iZombie(1);
        NetworkedIZombieMode mode = new NetworkedIZombieMode(
                stage, plantRegistry, zombieRegistry, new Random(3L));
        GameSession host = mode.createHostSession();
        host.start();
        host.addIZombieSunBalance(25);

        var payload = MatchSnapshotBuilder.build(host, "match-sun");
        GameSession guest = mode.createGuestSession();
        guest.setSunBalance(999);
        MatchSnapshotApplier.apply(guest, payload);

        assertEquals(host.getIZombieSunBalance(), guest.getIZombieSunBalance());
        assertEquals(999, guest.getSunBalance());
    }
}
