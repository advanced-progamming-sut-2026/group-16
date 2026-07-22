package model.minigame.izombie;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.board.GameBoard;
import model.game.entity.zombie.Zombie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SunProducerSystemTest {

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

    private GameSession newSession(int startingSun) {
        GameSession session = new GameSession(
                plantRegistry, new GameBoard(5, 9), startingSun, zombieRegistry, 1);
        session.start();
        return session;
    }

    @Test
    void productionStartsAtBaseRateAfterOneSecond() {
        GameSession session = newSession(0);
        SunProducerSystem system = new SunProducerSystem();
        Zombie producer = session.spawnZombieOfType("ZombieArmor2", 0, 8);
        producer.setStationary(true);
        system.register(producer, 0);

        system.tick(session);
        assertEquals(0, session.getSunBalance());

        for (int i = 0; i < GameSession.TICKS_PER_SECOND; i++) {
            system.tick(session);
        }
        assertEquals(SunProducerSystem.BASE_SUN_PER_SECOND, session.getSunBalance());
    }

    @Test
    void productionGrowsOverTime() {
        GameSession session = newSession(0);
        SunProducerSystem system = new SunProducerSystem();
        Zombie producer = session.spawnZombieOfType("ZombieArmor2", 0, 8);
        producer.setStationary(true);
        system.register(producer, 0);

        int ticksForGrowth = (SunProducerSystem.GROWTH_INTERVAL_SECONDS + 1)
                * GameSession.TICKS_PER_SECOND;
        for (int i = 0; i < ticksForGrowth; i++) {
            system.tick(session);
        }

        int expectedMin = SunProducerSystem.BASE_SUN_PER_SECOND
                * SunProducerSystem.GROWTH_INTERVAL_SECONDS
                + (SunProducerSystem.BASE_SUN_PER_SECOND + 1);
        assertTrue(session.getSunBalance() >= expectedMin,
                "sun balance should grow after growth interval, got " + session.getSunBalance());
    }

    @Test
    void deadProducerStopsProducingAndIsRemoved() {
        GameSession session = newSession(0);
        SunProducerSystem system = new SunProducerSystem();
        Zombie producer = session.spawnZombieOfType("ZombieArmor2", 0, 8);
        producer.setStationary(true);
        system.register(producer, 0);

        for (int i = 0; i < GameSession.TICKS_PER_SECOND; i++) {
            system.tick(session);
        }
        int sunAfterFirstSecond = session.getSunBalance();
        assertEquals(SunProducerSystem.BASE_SUN_PER_SECOND, sunAfterFirstSecond);

        producer.takeDirectDamage(producer.getHealth() + 99999);
        assertTrue(producer.isDead());

        for (int i = 0; i < GameSession.TICKS_PER_SECOND * 5; i++) {
            system.tick(session);
        }
        assertEquals(sunAfterFirstSecond, session.getSunBalance());
        assertEquals(0, system.getProducerCount());
    }
}
