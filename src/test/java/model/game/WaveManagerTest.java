package model.game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class WaveManagerTest {

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
}
