package model.minigame.beghouled;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeghouledGridTest {

    private static final List<String> POOL = List.of("A", "B", "C", "D", "E");

    @Test
    void fillRandomlyFillsAllNonCraterCells() {
        BeghouledGrid grid = new BeghouledGrid(5, 9);
        grid.fillRandomly(POOL, new Random(1));
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                assertTrue(POOL.contains(grid.getPlant(col, row)));
            }
        }
    }

    @Test
    void swapExchangesAdjacentPlants() {
        BeghouledGrid grid = new BeghouledGrid(3, 3);
        grid.setPlant(0, 0, "A");
        grid.setPlant(1, 0, "B");
        grid.swap(0, 0, 1, 0);
        assertEquals("B", grid.getPlant(0, 0));
        assertEquals("A", grid.getPlant(1, 0));
    }

    @Test
    void findHorizontalMatchOfThree() {
        BeghouledGrid grid = new BeghouledGrid(3, 3);
        grid.setPlant(0, 1, "A");
        grid.setPlant(1, 1, "A");
        grid.setPlant(2, 1, "A");
        grid.setPlant(0, 0, "B");
        grid.setPlant(1, 0, "C");
        grid.setPlant(2, 0, "D");
        List<BeghouledMatch> matches = grid.findMatches();
        assertEquals(1, matches.size());
        assertEquals(3, matches.getFirst().size());
        assertEquals("A", matches.getFirst().plantName());
    }

    @Test
    void findVerticalMatchOfFour() {
        BeghouledGrid grid = new BeghouledGrid(4, 2);
        for (int row = 0; row < 4; row++) {
            grid.setPlant(0, row, "X");
            grid.setPlant(1, row, "Y" + row);
        }
        List<BeghouledMatch> matches = grid.findMatches();
        assertEquals(1, matches.size());
        assertEquals(4, matches.getFirst().size());
    }

    @Test
    void clearRemovesMatchedCells() {
        BeghouledGrid grid = new BeghouledGrid(1, 3);
        grid.setPlant(0, 0, "A");
        grid.setPlant(1, 0, "A");
        grid.setPlant(2, 0, "A");
        grid.clear(grid.findMatches());
        assertEquals(null, grid.getPlant(0, 0));
        assertEquals(null, grid.getPlant(1, 0));
        assertEquals(null, grid.getPlant(2, 0));
    }

    @Test
    void gravityFallsAroundCrater() {
        BeghouledGrid grid = new BeghouledGrid(5, 1);
        grid.setPlant(0, 0, "A");
        grid.markCrater(0, 2);
        grid.setPlant(0, 3, "B");
        grid.applyGravity();
        assertEquals(null, grid.getPlant(0, 0));
        assertEquals("A", grid.getPlant(0, 1));
        assertTrue(grid.isCrater(0, 2));
        assertEquals(null, grid.getPlant(0, 3));
        assertEquals("B", grid.getPlant(0, 4));
    }

    @Test
    void refillFillsOnlyEmptyNonCraterCells() {
        BeghouledGrid grid = new BeghouledGrid(2, 2);
        grid.markCrater(0, 0);
        grid.setPlant(1, 0, "A");
        grid.refill(POOL, new Random(2));
        assertTrue(grid.isCrater(0, 0));
        assertEquals(null, grid.getPlant(0, 0));
        assertEquals("A", grid.getPlant(1, 0));
        assertTrue(POOL.contains(grid.getPlant(0, 1)));
        assertTrue(POOL.contains(grid.getPlant(1, 1)));
    }

    @Test
    void hasAnyValidMoveDetectsSwap() {
        BeghouledGrid grid = new BeghouledGrid(3, 3);
        grid.setPlant(0, 0, "A");
        grid.setPlant(1, 0, "B");
        grid.setPlant(2, 0, "A");
        grid.setPlant(0, 1, "A");
        grid.setPlant(1, 1, "C");
        grid.setPlant(2, 1, "D");
        grid.setPlant(0, 2, "E");
        grid.setPlant(1, 2, "E");
        grid.setPlant(2, 2, "E");
        assertTrue(grid.hasAnyValidMove());
    }

    @Test
    void hasAnyValidMoveFalseWhenNoMatchesPossible() {
        BeghouledGrid grid = new BeghouledGrid(2, 2);
        grid.setPlant(0, 0, "A");
        grid.setPlant(1, 0, "B");
        grid.setPlant(0, 1, "C");
        grid.setPlant(1, 1, "D");
        assertFalse(grid.hasAnyValidMove());
    }

    @Test
    void areAdjacentOnlyForNeighbors() {
        BeghouledGrid grid = new BeghouledGrid(5, 5);
        assertTrue(grid.areAdjacent(1, 1, 2, 1));
        assertTrue(grid.areAdjacent(1, 1, 1, 2));
        assertFalse(grid.areAdjacent(1, 1, 2, 2));
        assertFalse(grid.areAdjacent(1, 1, 3, 1));
    }

    @Test
    void copyPreservesState() {
        BeghouledGrid grid = new BeghouledGrid(2, 2);
        grid.setPlant(0, 0, "A");
        grid.markCrater(1, 1);
        BeghouledGrid copy = grid.copy();
        assertEquals("A", copy.getPlant(0, 0));
        assertTrue(copy.isCrater(1, 1));
        copy.setPlant(0, 0, "B");
        assertEquals("A", grid.getPlant(0, 0));
    }
}
