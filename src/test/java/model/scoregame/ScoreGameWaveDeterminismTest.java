package model.scoregame;

import model.App;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.WaveManager;
import model.game.entity.zombie.Zombie;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ScoreGameWaveDeterminismTest {

    @Test
    void sameDailySeedProducesIdenticalFirstWaveSpawns() throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-07-23T08:00:00Z"), ZoneOffset.UTC);
        PlantRegistry plants = App.getInstance().getPlantRegistry();
        ZombieRegistry zombies = loadZombies();

        List<String> first = spawnSignature(plants, zombies, clock);
        List<String> second = spawnSignature(plants, zombies, clock);

        assertFalse(first.isEmpty());
        assertEquals(first, second);
    }

    private static List<String> spawnSignature(PlantRegistry plants, ZombieRegistry zombies, Clock clock) {
        var match = ScoreGameSessionFactory.create(plants, zombies, clock);
        GameSession session = match.session();
        WaveManager waves = session.getWaveManager();
        waves.startWaves(session);
        List<String> signature = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            signature.add(zombie.getType() + "@" + zombie.getRow() + ":" + zombie.getX());
        }
        return signature;
    }

    private static ZombieRegistry loadZombies() throws Exception {
        ZombieRegistry registry = new ZombieRegistry();
        try (InputStream z = ScoreGameWaveDeterminismTest.class.getClassLoader()
                .getResourceAsStream("zombies.json");
             InputStream a = ScoreGameWaveDeterminismTest.class.getClassLoader()
                     .getResourceAsStream("ArmorTypeData.json")) {
            registry.loadFromJson(z);
            registry.loadArmorFromJson(a);
        }
        return registry;
    }
}
