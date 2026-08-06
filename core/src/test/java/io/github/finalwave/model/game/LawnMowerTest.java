package io.github.finalwave.model.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LawnMowerTest {

    @Test
    void firstTriggerSucceedsSecondFails() {
        LawnMower mower = new LawnMower(2);
        assertFalse(mower.isUsed());
        assertTrue(mower.trigger());
        assertTrue(mower.isUsed());
        assertFalse(mower.trigger());
    }

    @Test
    void rejectsNegativeRow() {
        assertThrows(IllegalArgumentException.class, () -> new LawnMower(-1));
    }
}
