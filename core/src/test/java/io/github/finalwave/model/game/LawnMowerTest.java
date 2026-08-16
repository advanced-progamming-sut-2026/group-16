package io.github.finalwave.model.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LawnMowerTest {

    @Test
    void firstTriggerActivatesSecondFailsUntilFinished() {
        LawnMower mower = new LawnMower(2);
        assertTrue(mower.isReady());
        assertFalse(mower.isUsed());
        assertFalse(mower.isActive());
        assertEquals(LawnMower.START_X, mower.getX(), 0.0001);

        assertTrue(mower.trigger());
        assertTrue(mower.isActive());
        assertFalse(mower.isUsed());
        assertFalse(mower.isReady());
        assertFalse(mower.trigger());

        int cols = 9;
        while (!mower.isUsed()) {
            mower.tick(cols);
        }
        assertTrue(mower.isActive());
        assertTrue(mower.isUsed());
        assertTrue(mower.getX() >= cols);
        assertTrue(mower.getX() < cols + LawnMower.EXIT_COLUMNS);
        assertFalse(mower.trigger());

        while (mower.isActive()) {
            mower.tick(cols);
        }
        assertFalse(mower.isActive());
        assertTrue(mower.isUsed());
        assertTrue(mower.getX() >= cols + LawnMower.EXIT_COLUMNS);
        assertFalse(mower.trigger());
    }

    @Test
    void rejectsNegativeRow() {
        assertThrows(IllegalArgumentException.class, () -> new LawnMower(-1));
    }
}
