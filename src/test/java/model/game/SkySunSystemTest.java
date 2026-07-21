package model.game;

import model.item.Sun;
import model.item.SunType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SkySunSystemTest {

    @Test
    void intervalFormulaMatchesDoc() {
        assertEquals(12.0, SkySunSystem.intervalSeconds(0), 0.001);
        assertEquals(12.0, SkySunSystem.intervalSeconds(100), 0.001);
        assertEquals(12.0, SkySunSystem.intervalSeconds(120), 0.001);
        assertTrue(SkySunSystem.intervalSeconds(200) >= 12.0);
    }

    @Test
    void typeDistributionApproximateWithSeed() {
        SkySunSystem system = new SkySunSystem(new Random(42));
        Map<SunType, Integer> counts = new EnumMap<>(SunType.class);
        for (int i = 0; i < 1000; i++) {
            counts.merge(system.rollSunType(), 1, Integer::sum);
        }
        assertTrue(counts.getOrDefault(SunType.NORMAL, 0) > 700);
        assertTrue(counts.getOrDefault(SunType.SPECIAL, 0) > 50);
        assertTrue(counts.getOrDefault(SunType.RADIOACTIVE, 0) > 10);
    }

    @Test
    void disabledSystemProducesNoSun() {
        SkySunSystem system = new SkySunSystem(new Random(1));
        system.setEnabled(false);
        assertNull(system.tick(100, 10, 9, 5));
    }

    @Test
    void fallingSunLandsAfterFiftyTicks() {
        Sun sun = new Sun(1, 1, 25, SunType.NORMAL, false);
        assertTrue(sun.isFalling());
        boolean landed = false;
        for (int i = 0; i < 50; i++) {
            landed = sun.tick() || landed;
        }
        assertTrue(landed);
        assertFalse(sun.isFalling());
        assertTrue(sun.hasReachedGround());
    }
}
