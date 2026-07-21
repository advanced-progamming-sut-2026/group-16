package model.game.mode;

import model.adventure.ChapterConfig;
import model.adventure.ChapterId;
import model.adventure.ChapterRules;
import model.adventure.LevelConfig;
import model.adventure.LevelType;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.SlipperyTile;
import model.game.WaveManager;
import model.game.board.GameBoard;
import model.game.board.tile.GraveTile;
import model.game.board.tile.IceTile;
import model.game.board.tile.LowBeachTile;
import model.game.board.tile.NecromancyTile;
import model.game.board.tile.NormalTile;
import model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdventureMode extends GameMode {

    private final ChapterConfig chapter;
    private final LevelConfig level;
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;
    private final Random random;
    private final int difficultyLevel;
    private int currentWaterColumns;
    private final List<FrozenZombieMarker> preFrozenMarkers = new ArrayList<>();

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
                rules.isSkySunEnabled() && level.getType() != LevelType.NIGHT_OPS);
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

    private void applyWaterColumns(GameBoard board, int waterColumns) {
        int cols = board.getCols();
        int fromCol = Math.max(0, cols - waterColumns);
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < cols; col++) {
                if (col >= fromCol) {
                    board.setTile(col, row, new LowBeachTile());
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
            for (var plant : board.getAllPlants()) {
                if (plant.getRow() != row || !plant.isAlive()) {
                    continue;
                }
                if (plant.hasTag(model.game.entity.plant.PlantTag.FIRE)) {
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
            if (plant.getCol() >= fromCol && !plant.hasTag(model.game.entity.plant.PlantTag.WATER)) {
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

    private void spawnFromNecromancy(GameSession session) {
        GameBoard board = session.getBoard();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (board.getTile(col, row) instanceof NecromancyTile
                        && board.getPlantAt(col, row) == null) {
                    session.spawnZombieOfType("ZombieDefault", row, col + 0.5);
                }
            }
        }
    }

    public boolean areZombiesImmuneToChill() {
        return chapter.getRules().areZombiesImmuneToChill();
    }

    private record FrozenZombieMarker(int col, int row) {
    }
}
