package io.github.finalwave.model.minigame.beghouled;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BeghouledGrid {

    private final int rows;
    private final int cols;
    private final String[][] plants;
    private final boolean[][] craters;

    public BeghouledGrid(int rows, int cols) {
        this.rows = Math.max(1, rows);
        this.cols = Math.max(1, cols);
        this.plants = new String[this.rows][this.cols];
        this.craters = new boolean[this.rows][this.cols];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public String getPlant(int col, int row) {
        return inBounds(col, row) ? plants[row][col] : null;
    }

    public boolean isCrater(int col, int row) {
        return inBounds(col, row) && craters[row][col];
    }

    public boolean inBounds(int col, int row) {
        return col >= 0 && col < cols && row >= 0 && row < rows;
    }

    public void setPlant(int col, int row, String plantName) {
        if (inBounds(col, row) && !craters[row][col]) {
            plants[row][col] = plantName;
        }
    }

    public void markCrater(int col, int row) {
        if (!inBounds(col, row)) {
            return;
        }
        craters[row][col] = true;
        plants[row][col] = null;
    }

    public void fillRandomly(List<String> pool, Random random) {
        if (pool == null || pool.isEmpty()) {
            throw new IllegalArgumentException("plant pool must not be empty");
        }
        Random rng = random == null ? new Random() : random;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!craters[row][col]) {
                    plants[row][col] = pool.get(rng.nextInt(pool.size()));
                }
            }
        }
    }

    public void swap(int colA, int rowA, int colB, int rowB) {
        if (!inBounds(colA, rowA) || !inBounds(colB, rowB)) {
            return;
        }
        if (craters[rowA][colA] || craters[rowB][colB]) {
            return;
        }
        String temp = plants[rowA][colA];
        plants[rowA][colA] = plants[rowB][colB];
        plants[rowB][colB] = temp;
    }

    public boolean areAdjacent(int colA, int rowA, int colB, int rowB) {
        int dc = Math.abs(colA - colB);
        int dr = Math.abs(rowA - rowB);
        return (dc + dr) == 1;
    }

    public List<BeghouledMatch> findMatches() {
        List<BeghouledMatch> matches = new ArrayList<>();
        boolean[][] claimed = new boolean[rows][cols];
        collectHorizontalMatches(matches, claimed);
        collectVerticalMatches(matches, claimed);
        return matches;
    }

    private void collectHorizontalMatches(List<BeghouledMatch> matches, boolean[][] claimed) {
        for (int row = 0; row < rows; row++) {
            int col = 0;
            while (col < cols) {
                String name = plants[row][col];
                if (name == null || craters[row][col]) {
                    col++;
                    continue;
                }
                int end = col + 1;
                while (end < cols && name.equals(plants[row][end]) && !craters[row][end]) {
                    end++;
                }
                if (end - col >= 3) {
                    addMatch(matches, claimed, name, col, row, end - col, true);
                }
                col = end;
            }
        }
    }

    private void collectVerticalMatches(List<BeghouledMatch> matches, boolean[][] claimed) {
        for (int col = 0; col < cols; col++) {
            int row = 0;
            while (row < rows) {
                String name = plants[row][col];
                if (name == null || craters[row][col]) {
                    row++;
                    continue;
                }
                int end = row + 1;
                while (end < rows && name.equals(plants[end][col]) && !craters[end][col]) {
                    end++;
                }
                if (end - row >= 3) {
                    addMatch(matches, claimed, name, col, row, end - row, false);
                }
                row = end;
            }
        }
    }

    private void addMatch(List<BeghouledMatch> matches,
                          boolean[][] claimed,
                          String name,
                          int startCol,
                          int startRow,
                          int length,
                          boolean horizontal) {
        List<BeghouledCell> cells = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            int col = horizontal ? startCol + i : startCol;
            int row = horizontal ? startRow : startRow + i;
            if (!claimed[row][col]) {
                claimed[row][col] = true;
                cells.add(new BeghouledCell(col, row));
            }
        }
        if (!cells.isEmpty()) {
            matches.add(new BeghouledMatch(name, cells));
        }
    }

    public void clear(List<BeghouledMatch> matches) {
        if (matches == null) {
            return;
        }
        for (BeghouledMatch match : matches) {
            for (BeghouledCell cell : match.cells()) {
                if (inBounds(cell.col(), cell.row()) && !craters[cell.row()][cell.col()]) {
                    plants[cell.row()][cell.col()] = null;
                }
            }
        }
    }

    public void applyGravity() {
        for (int col = 0; col < cols; col++) {
            applyGravityToColumn(col);
        }
    }

    private void applyGravityToColumn(int col) {
        int writeRow = rows - 1;
        for (int row = rows - 1; row >= 0; row--) {
            if (craters[row][col]) {
                writeRow = row - 1;
                continue;
            }
            if (plants[row][col] == null) {
                continue;
            }
            if (writeRow != row) {
                plants[writeRow][col] = plants[row][col];
                plants[row][col] = null;
            }
            writeRow--;
        }
    }

    public void refill(List<String> pool, Random random) {
        if (pool == null || pool.isEmpty()) {
            return;
        }
        Random rng = random == null ? new Random() : random;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!craters[row][col] && plants[row][col] == null) {
                    plants[row][col] = pool.get(rng.nextInt(pool.size()));
                }
            }
        }
    }

    public boolean hasAnyValidMove() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (hasValidSwapFrom(col, row)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasValidSwapFrom(int col, int row) {
        if (craters[row][col] || plants[row][col] == null) {
            return false;
        }
        int[][] deltas = {{1, 0}, {0, 1}};
        for (int[] delta : deltas) {
            int ncol = col + delta[0];
            int nrow = row + delta[1];
            if (!inBounds(ncol, nrow) || craters[nrow][ncol] || plants[nrow][ncol] == null) {
                continue;
            }
            swap(col, row, ncol, nrow);
            boolean matched = !findMatches().isEmpty();
            swap(col, row, ncol, nrow);
            if (matched) {
                return true;
            }
        }
        return false;
    }

    public BeghouledGrid copy() {
        BeghouledGrid copy = new BeghouledGrid(rows, cols);
        for (int row = 0; row < rows; row++) {
            System.arraycopy(plants[row], 0, copy.plants[row], 0, cols);
            System.arraycopy(craters[row], 0, copy.craters[row], 0, cols);
        }
        return copy;
    }
}
