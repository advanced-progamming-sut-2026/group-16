package io.github.finalwave.model.game.mode;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.ChapterRules;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.SlipperyTile;
import io.github.finalwave.model.game.WaveManager;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.LowBeachTile;
import io.github.finalwave.model.game.board.tile.NecromancyTile;
import io.github.finalwave.model.game.board.tile.NormalTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.board.tile.WaterTile;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AdventureMode extends GameMode {

    private final ChapterConfig chapter;
    private final LevelConfig level;
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;
    private final Random random;
    private final int difficultyLevel;
    private int currentWaterColumns;
    private final List<FrozenZombieMarker> preFrozenMarkers = new ArrayList<>();
    private final Set<String> lowBeachCells = new HashSet<>();

    public AdventureMode(ChapterConfig chapter,
                         LevelConfig level,
                         PlantRegistry plantRegistry,
                         ZombieRegistry zombieRegistry,
                         int difficultyLevel,
                         Random random) {
        if (chapter == null || level == null || plantRegistry == null || zombieRegistry == null) {
            throw new IllegalArgumentException("adventure mode dependencies must not be null");
        }
        this.chapter = chapter;
        this.level = level;
        this.plantRegistry = plantRegistry;
        this.zombieRegistry = zombieRegistry;
        this.difficultyLevel = Math.max(1, Math.min(5, difficultyLevel));
        this.random = random == null ? new Random() : random;
        this.currentWaterColumns = chapter.getRules().getInitialWaterColumns();
    }

    public ChapterConfig getChapter() {
        return chapter;
    }

    public LevelConfig getLevel() {
        return level;
    }

    public ChapterId getChapterId() {
        return chapter.getId();
    }

    public int getCurrentWaterColumns() {
        return currentWaterColumns;
    }

    public GameSession createSession() {
        GameBoard board = new GameBoard();
        applyBoardLayout(board);

        int zombieDiff = Math.max(1, (int) Math.round(
                difficultyLevel * WaveManager.zombieStatScale(difficultyLevel)));
        GameSession session = new GameSession(
                plantRegistry, board, level.getStartingSun(), zombieRegistry, zombieDiff, random);
        session.setChapterId(chapter.getId().getKey());
        session.setLevelId(chapter.getId().getKey() + "-L" + level.getIndex());
        session.setNightLevel(chapter.getId() == ChapterId.DARK_AGES
                || level.getType() == LevelType.NIGHT_OPS);

        ChapterRules rules = chapter.getRules();
        session.getSkySunSystem().setEnabled(
                rules.isSkySunEnabled()
                        && level.getType() != LevelType.NIGHT_OPS
                        && level.getType() != LevelType.CONVEYOR_BELT);
        session.setZombiesImmuneToChill(rules.areZombiesImmuneToChill());
        session.applyUserDifficulty(difficultyLevel);

        List<String> pool = level.getAllowedZombieAliases().isEmpty()
                ? List.of("ZombieDefault")
                : level.getAllowedZombieAliases();
        WaveManager waveManager = new WaveManager(
                level.getWaveCount(), level.getBaseWaveCost(), pool, random);
        waveManager.setWaveCostDifficultyScale(WaveManager.waveCostScale(difficultyLevel));
        if (rules.hasSandstormOnFinalWave()) {
            waveManager.enableSandstormOnFinalWave(
                    rules.getSandstormMinOffset(), rules.getSandstormMaxOffset());
        }
        session.setWaveManager(waveManager);
        session.setWavesAutoStart(true);

        placePreFrozenZombies(session);
        return session;
    }

    private void applyBoardLayout(GameBoard board) {
        ChapterRules rules = chapter.getRules();
        if (rules.hasWaterColumns()) {
            pickLowBeachCells(board, rules);
            applyWaterColumns(board, currentWaterColumns);
        }
        if (rules.hasGravesAtStart()) {
            placeRandomGraves(board, rules.getStartingGraveCount(), rules.hasNecromancyTiles(),
                    rules.hasGraveLoot(), null);
        }
        if (rules.hasSlipperyTiles()) {
            placeSlipperyTiles(board);
        }
        if (rules.hasPreFrozenZombies()) {
            markPreFrozenSlots(board);
        }
    }

    private void pickLowBeachCells(GameBoard board, ChapterRules rules) {
        lowBeachCells.clear();
        if (!rules.hasLowBeachEmerge()) {
            return;
        }
        int cols = board.getCols();
        int rows = board.getRows();
        int maxTide = Math.min(Math.max(1, rules.getMaxTideColumn()), cols);
        int permanent = Math.min(2, Math.max(1, rules.getInitialWaterColumns()));
        int tideFrom = cols - maxTide;
        int permanentFrom = cols - permanent;
        List<String> candidates = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = tideFrom; col < permanentFrom; col++) {
                candidates.add(cellKey(col, row));
            }
        }
        if (candidates.isEmpty()) {
            // Narrow board: allow a few spots in the permanent zone instead.
            for (int row = 0; row < rows; row++) {
                for (int col = tideFrom; col < cols; col++) {
                    candidates.add(cellKey(col, row));
                }
            }
        }
        Collections.shuffle(candidates, random);
        int target = Math.max(2, candidates.size() / 3);
        target = Math.min(target, Math.max(0, candidates.size() - 1));
        for (int i = 0; i < target; i++) {
            lowBeachCells.add(candidates.get(i));
        }
    }

    private void applyWaterColumns(GameBoard board, int waterColumns) {
        int cols = board.getCols();
        int fromCol = Math.max(0, cols - waterColumns);
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < cols; col++) {
                boolean underWater = col >= fromCol;
                boolean lowBeach = lowBeachCells.contains(cellKey(col, row));
                if (underWater && lowBeach) {
                    board.setTile(col, row, new LowBeachTile(true));
                } else if (underWater) {
                    board.setTile(col, row, new WaterTile());
                } else if (lowBeach) {
                    board.setTile(col, row, new LowBeachTile(false));
                } else {
                    board.setTile(col, row, new NormalTile());
                }
            }
        }
    }


    public void onWaveStarted(GameSession session, int waveNumber) {
        ChapterRules rules = chapter.getRules();
        if (rules.hasWaterColumns()) {
            int min = rules.getInitialWaterColumns();
            int max = Math.min(rules.getMaxTideColumn(), session.getBoard().getCols());
            currentWaterColumns = min + random.nextInt(Math.max(1, max - min + 1));
            drownPlantsUnderTide(session);
            applyWaterColumns(session.getBoard(), currentWaterColumns);
        }
        if (rules.hasLowBeachEmerge()) {
            spawnLowBeachEmerges(session);
        }
        if (rules.hasGravesOnWaveStart()) {
            placeRandomGraves(session.getBoard(), 1 + random.nextInt(2), rules.hasNecromancyTiles(),
                    rules.hasGraveLoot(), session);
            if (rules.hasNecromancyTiles()) {
                spawnFromNecromancy(session);
            }
        }
        if (rules.isIceWindEnabled()) {
            applyIceWind(session);
        }
    }

    private void applyIceWind(GameSession session) {
        GameBoard board = session.getBoard();
        int rowCount = board.getRows();
        int hitRows = 1 + random.nextInt(Math.min(3, Math.max(1, rowCount)));
        java.util.Set<Integer> rows = new java.util.HashSet<>();
        while (rows.size() < hitRows) {
            rows.add(random.nextInt(rowCount));
        }
        for (int row : rows) {
            session.applyRowEffect(row, GameSession.ROW_EFFECT_ICE_WIND, GameSession.ICE_WIND_DURATION_TICKS);
            for (var plant : board.getAllPlants()) {
                if (plant.getRow() != row || !plant.isAlive()) {
                    continue;
                }
                if (plant.hasTag(io.github.finalwave.model.game.entity.plant.PlantTag.FIRE)) {
                    continue;
                }
                session.addPlantFrostStack(plant);
            }
        }
    }

    private void drownPlantsUnderTide(GameSession session) {
        GameBoard board = session.getBoard();
        int fromCol = Math.max(0, board.getCols() - currentWaterColumns);
        for (var plant : board.getAllPlants()) {
            if (plant.getCol() >= fromCol && !plant.hasTag(PlantTag.WATER)) {
                plant.takeDamage(plant.getHealth() + 99999);
                session.removePlantFromBoard(plant);
            }
        }
    }

    private void placeRandomGraves(GameBoard board, int count, boolean necromancy,
                                   boolean withLoot, GameSession session) {
        int placed = 0;
        int attempts = 0;
        while (placed < count && attempts < 100) {
            attempts++;
            int col = 2 + random.nextInt(Math.max(1, board.getCols() - 3));
            int row = random.nextInt(board.getRows());
            if (board.getPlantAt(col, row) != null) {
                continue;
            }
            if (board.getTile(col, row).isWater() || board.getTile(col, row).isGrave()) {
                continue;
            }
            GraveTile.Loot loot = GraveTile.Loot.NONE;
            if (withLoot) {
                int roll = random.nextInt(100);
                if (roll < 20) {
                    loot = GraveTile.Loot.SUN_50;
                } else if (roll < 35) {
                    loot = GraveTile.Loot.PLANT_FOOD;
                }
            }
            GraveTile grave = necromancy && random.nextBoolean()
                    ? new NecromancyTile(loot)
                    : new GraveTile(loot);
            board.setTile(col, row, grave);
            if (session != null && session.getMatchListener() != null) {
                session.getMatchListener().onGraveCreated(col, row, loot.name());
            }
            placed++;
        }
    }

    private void placeSlipperyTiles(GameBoard board) {
        for (int i = 0; i < 3; i++) {
            int col = 3 + random.nextInt(Math.max(1, board.getCols() - 4));
            int row = random.nextInt(board.getRows());
            SlipperyTile.SlipDirection dir = random.nextBoolean()
                    ? SlipperyTile.SlipDirection.UP
                    : SlipperyTile.SlipDirection.DOWN;
            board.setTile(col, row, new SlipperyTile(dir));
        }
    }

    private void markPreFrozenSlots(GameBoard board) {
        for (int i = 0; i < 2; i++) {
            int col = board.getCols() - 2;
            int row = random.nextInt(board.getRows());
            board.setTile(col, row, new IceTile());
            preFrozenMarkers.add(new FrozenZombieMarker(col, row));
        }
    }

    private void placePreFrozenZombies(GameSession session) {
        if (!chapter.getRules().hasPreFrozenZombies()) {
            return;
        }
        for (FrozenZombieMarker marker : preFrozenMarkers) {
            try {
                Zombie zombie = session.spawnZombieOfType("ZombieDefault", marker.row(), marker.col());
                zombie.applyFreeze(600);
            } catch (RuntimeException ignored) {
                // pool may not allow spawn at that tile in some configs
            }
        }
    }

    private void spawnLowBeachEmerges(GameSession session) {
        GameBoard board = session.getBoard();
        List<int[]> emergeCells = new ArrayList<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (tile instanceof LowBeachTile lowBeach && lowBeach.isFlooded()) {
                    emergeCells.add(new int[]{col, row});
                }
            }
        }
        if (emergeCells.isEmpty()) {
            return;
        }
        Collections.shuffle(emergeCells, random);
        int emergeCount = Math.min(1 + random.nextInt(2), emergeCells.size());
        for (int i = 0; i < emergeCount; i++) {
            int[] cell = emergeCells.get(i);
            try {
                session.spawnZombieOfType(pickPoolZombieAlias(), cell[1], cell[0] + 0.5);
            } catch (RuntimeException ignored) {
                // spawn may fail for invalid pool aliases in some configs
            }
        }
    }

    private String pickPoolZombieAlias() {
        List<String> pool = level.getAllowedZombieAliases();
        if (pool == null || pool.isEmpty()) {
            return "ZombieDefault";
        }
        return pool.get(random.nextInt(pool.size()));
    }

    private void spawnFromNecromancy(GameSession session) {
        GameBoard board = session.getBoard();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (board.getTile(col, row) instanceof NecromancyTile
                        && board.getPlantAt(col, row) == null) {
                    try {
                        session.spawnZombieOfType(pickPoolZombieAlias(), row, col + 0.5);
                    } catch (RuntimeException ignored) {
                        // pool may not allow spawn for some aliases
                    }
                }
            }
        }
    }

    public boolean areZombiesImmuneToChill() {
        return chapter.getRules().areZombiesImmuneToChill();
    }

    private static String cellKey(int col, int row) {
        return col + ":" + row;
    }

    private record FrozenZombieMarker(int col, int row) {
    }
}
