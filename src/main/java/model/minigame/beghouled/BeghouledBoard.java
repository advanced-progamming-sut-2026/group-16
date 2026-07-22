package model.minigame.beghouled;

import model.game.GameSession;
import model.game.board.tile.CraterTile;
import model.game.board.tile.NormalTile;
import model.game.entity.plant.Plant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class BeghouledBoard {

    private static final int SUN_PER_MATCH_UNIT = 50;

    private BeghouledGrid grid;
    private List<String> plantPool = List.of();
    private BeghouledUpgradeCatalog upgradeCatalog = new BeghouledUpgradeCatalog(List.of());
    private Random random = new Random();
    private int matchesMade;

    public BeghouledGrid getGrid() {
        return grid;
    }

    public int getMatchesMade() {
        return matchesMade;
    }

    public void setMatchesMade(int matchesMade) {
        this.matchesMade = Math.max(0, matchesMade);
    }

    public BeghouledUpgradeCatalog getUpgradeCatalog() {
        return upgradeCatalog;
    }

    public List<String> getPlantPool() {
        return plantPool;
    }

    public void configure(List<String> pool, BeghouledUpgradeCatalog catalog, Random random) {
        this.plantPool = pool == null ? List.of() : List.copyOf(pool);
        this.upgradeCatalog = catalog == null ? new BeghouledUpgradeCatalog(List.of()) : catalog;
        this.random = random == null ? new Random() : random;
        this.matchesMade = 0;
    }

    public void fillRandomly(GameSession session) {
        int rows = session.getBoard().getRows();
        int cols = session.getBoard().getCols();
        grid = new BeghouledGrid(rows, cols);
        clearExistingPlants(session);
        resetNonCraterTiles(session);
        grid.fillRandomly(plantPool, random);
        resolveOpeningMatches();
        syncSessionFromGrid(session);
    }

    private void resolveOpeningMatches() {
        for (int attempt = 0; attempt < 20; attempt++) {
            int guard = 0;
            while (!grid.findMatches().isEmpty() && guard < 50) {
                grid.clear(grid.findMatches());
                grid.applyGravity();
                grid.refill(plantPool, random);
                guard++;
            }
            if (grid.hasAnyValidMove()) {
                return;
            }
            grid.fillRandomly(plantPool, random);
        }
    }

    public BeghouledSwapResult trySwap(GameSession session,
                                       int colA, int rowA,
                                       int colB, int rowB) {
        if (grid == null) {
            return BeghouledSwapResult.failure(BeghouledSwapOutcome.OUT_OF_BOUNDS);
        }
        BeghouledSwapOutcome validation = validateSwap(colA, rowA, colB, rowB);
        if (validation != BeghouledSwapOutcome.SUCCESS) {
            return BeghouledSwapResult.failure(validation);
        }
        BeghouledGrid trial = grid.copy();
        trial.swap(colA, rowA, colB, rowB);
        if (trial.findMatches().isEmpty()) {
            return BeghouledSwapResult.failure(BeghouledSwapOutcome.NO_MATCH_FORMED);
        }
        grid.swap(colA, rowA, colB, rowB);
        CascadeResult cascade = resolveCascades(true);
        boolean reset = false;
        if (!grid.hasAnyValidMove()) {
            resetBoardKeepingCraters();
            reset = true;
        }
        syncSessionFromGrid(session);
        session.addSunBalance(cascade.sunAwarded());
        return new BeghouledSwapResult(
                BeghouledSwapOutcome.SUCCESS,
                cascade.matchesCleared(),
                cascade.sunAwarded(),
                reset);
    }

    private BeghouledSwapOutcome validateSwap(int colA, int rowA, int colB, int rowB) {
        if (!grid.inBounds(colA, rowA) || !grid.inBounds(colB, rowB)) {
            return BeghouledSwapOutcome.OUT_OF_BOUNDS;
        }
        if (!grid.areAdjacent(colA, rowA, colB, rowB)) {
            return BeghouledSwapOutcome.NOT_ADJACENT;
        }
        if (grid.isCrater(colA, rowA) || grid.isCrater(colB, rowB)) {
            return BeghouledSwapOutcome.CRATER_BLOCKED;
        }
        if (grid.getPlant(colA, rowA) == null || grid.getPlant(colB, rowB) == null) {
            return BeghouledSwapOutcome.MISSING_PLANT;
        }
        return BeghouledSwapOutcome.SUCCESS;
    }

    private CascadeResult resolveCascades(boolean firstWaveIsPlayerMove) {
        int matchesCleared = 0;
        int sunAwarded = 0;
        boolean firstWave = true;
        int guard = 0;
        while (guard < 50) {
            List<BeghouledMatch> matches = grid.findMatches();
            if (matches.isEmpty()) {
                break;
            }
            for (BeghouledMatch match : matches) {
                matchesCleared++;
                matchesMade++;
                int units = Math.max(1, match.size() - 2);
                sunAwarded += units * SUN_PER_MATCH_UNIT;
                if (!firstWave || !firstWaveIsPlayerMove) {
                    sunAwarded += SUN_PER_MATCH_UNIT;
                }
            }
            grid.clear(matches);
            grid.applyGravity();
            grid.refill(plantPool, random);
            firstWave = false;
            guard++;
        }
        return new CascadeResult(matchesCleared, sunAwarded);
    }

    public BeghouledUpgradeResult applyUpgrade(GameSession session, String fromPlant) {
        Optional<BeghouledUpgradeRule> ruleOpt = upgradeCatalog.findRule(fromPlant);
        if (ruleOpt.isEmpty()) {
            return BeghouledUpgradeResult.failure(BeghouledUpgradeOutcome.UNKNOWN_UPGRADE);
        }
        BeghouledUpgradeRule rule = ruleOpt.get();
        if (session.getSunBalance() < rule.sunCost()) {
            return BeghouledUpgradeResult.failure(BeghouledUpgradeOutcome.INSUFFICIENT_SUN);
        }
        int converted = convertPlantsOnGrid(rule.fromPlant(), rule.toPlant());
        if (converted == 0) {
            return BeghouledUpgradeResult.failure(BeghouledUpgradeOutcome.NO_PLANTS_OF_TYPE);
        }
        session.setSunBalance(session.getSunBalance() - rule.sunCost());
        syncSessionFromGrid(session);
        return new BeghouledUpgradeResult(BeghouledUpgradeOutcome.SUCCESS, converted, rule.sunCost());
    }

    private int convertPlantsOnGrid(String from, String to) {
        int converted = 0;
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                if (from.equals(grid.getPlant(col, row))) {
                    grid.setPlant(col, row, to);
                    converted++;
                }
            }
        }
        return converted;
    }

    public void markCrater(GameSession session, int col, int row) {
        if (grid == null || !grid.inBounds(col, row)) {
            return;
        }
        grid.markCrater(col, row);
        session.getBoard().setTile(col, row, new CraterTile());
    }

    public Optional<int[]> findAnyValidSwap() {
        if (grid == null) {
            return Optional.empty();
        }
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                Optional<int[]> swap = findValidSwapFrom(col, row);
                if (swap.isPresent()) {
                    return swap;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<int[]> findValidSwapFrom(int col, int row) {
        int[][] deltas = {{1, 0}, {0, 1}};
        for (int[] delta : deltas) {
            int ncol = col + delta[0];
            int nrow = row + delta[1];
            if (!grid.inBounds(ncol, nrow)) {
                continue;
            }
            BeghouledGrid trial = grid.copy();
            if (trial.isCrater(col, row) || trial.isCrater(ncol, nrow)) {
                continue;
            }
            if (trial.getPlant(col, row) == null || trial.getPlant(ncol, nrow) == null) {
                continue;
            }
            trial.swap(col, row, ncol, nrow);
            if (!trial.findMatches().isEmpty()) {
                return Optional.of(new int[]{col, row, ncol, nrow});
            }
        }
        return Optional.empty();
    }

    private void resetBoardKeepingCraters() {
        grid.fillRandomly(plantPool, random);
        resolveOpeningMatches();
    }

    private void clearExistingPlants(GameSession session) {
        List<Plant> plants = new ArrayList<>(session.getBoard().getAllPlants());
        for (Plant plant : plants) {
            session.removePlantFromBoard(plant, false);
        }
    }

    private void resetNonCraterTiles(GameSession session) {
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            for (int col = 0; col < session.getBoard().getCols(); col++) {
                if (!session.getBoard().getTile(col, row).isCrater()) {
                    session.getBoard().setTile(col, row, new NormalTile());
                } else {
                    grid.markCrater(col, row);
                }
            }
        }
    }

    private void syncSessionFromGrid(GameSession session) {
        clearExistingPlants(session);
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                if (grid.isCrater(col, row)) {
                    session.getBoard().setTile(col, row, new CraterTile());
                    continue;
                }
                if (!(session.getBoard().getTile(col, row) instanceof NormalTile)) {
                    session.getBoard().setTile(col, row, new NormalTile());
                }
                String name = grid.getPlant(col, row);
                if (name != null) {
                    session.placeDefensePlant(name, col, row);
                }
            }
        }
    }

    private record CascadeResult(int matchesCleared, int sunAwarded) {
    }
}
