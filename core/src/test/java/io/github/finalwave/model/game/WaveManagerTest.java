package io.github.finalwave.model.game;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class WaveManagerTest {

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
    void waveCostsScaleAndFlagWaveDoubles() {
        WaveManager manager = new WaveManager(3, 100, List.of("ZombieDefault"), new Random(1));
        List<Wave> waves = manager.getWaves();
        assertEquals(3, waves.size());
        assertEquals(100, waves.get(0).getTargetCost());
        assertEquals(125, waves.get(1).getTargetCost());
        assertEquals(250, waves.get(2).getTargetCost());
        assertTrue(waves.get(2).isFlagWave());
    }

    @Test
    void difficultyScaleLowersWaveBudget() {
        WaveManager manager = new WaveManager(2, 300, List.of("ZombieDefault"), new Random(2));
        manager.setWaveCostDifficultyScale(WaveManager.waveCostScale(5));
        assertEquals(Math.max(1, (int) Math.round(300 / (5.0 / 3.0))),
                manager.getWaves().get(0).getTargetCost());
    }

    @Test
    void destroyedHealthRatioGate() {
        Wave wave = new Wave(1, 100, false);
        assertEquals(1.0, wave.getDestroyedHealthRatio(), 0.001);
        assertFalse(wave.isCleared());
    }

    @Test
    void hypnotizedZombiesDoNotBlockWaveProgressOrClear() {
        Wave wave = new Wave(1, 100, false);
        Zombie enemy = new Zombie.Builder("ZombieDefault").maxHealth(100).position(5, 1).build();
        Zombie ally = new Zombie.Builder("ZombieDefault").maxHealth(100).position(4, 1).build();
        wave.markStarted();
        wave.registerSpawn(enemy);
        wave.registerSpawn(ally);

        assertEquals(0.0, wave.getDestroyedHealthRatio(), 0.001);
        assertFalse(wave.isCleared());

        ally.hypnotize(1.0, 1.0);
        assertEquals(0.5, wave.getDestroyedHealthRatio(), 0.001);
        assertFalse(wave.isCleared());

        enemy.takeDirectDamage(enemy.getHealth() + 99999);
        assertEquals(1.0, wave.getDestroyedHealthRatio(), 0.001);
        assertTrue(wave.isCleared());
        assertTrue(ally.isAlive());
        assertTrue(ally.isHypnotized());
        assertFalse(ally.countsAsEnemy());
    }

    @Test
    void firstWaveAlwaysIncludesConeArmor() {
        List<String> pool = List.of("ZombieDefault", "ZombieArmor1");
        int[] seeds = {1, 2, 7, 9, 42, 99, 1234};
        for (int seed : seeds) {
            GameSession session = new GameSession(plantRegistry, zombieRegistry, 1);
            session.setWavesAutoStart(false);
            WaveManager waves = new WaveManager(1, 300, pool, new Random(seed));
            session.setWaveManager(waves);
            waves.startWaves(session);
            assertTrue(containsAlias(session.getZombies(), "ZombieArmor1"),
                    "seed " + seed + " first wave missing ZombieArmor1");
        }
    }

    private static boolean containsAlias(List<Zombie> zombies, String alias) {
        for (Zombie zombie : zombies) {
            if (alias.equals(zombie.getType())) {
                return true;
            }
        }
        return false;
    }
}
