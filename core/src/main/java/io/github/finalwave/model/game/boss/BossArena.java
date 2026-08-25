package io.github.finalwave.model.game.boss;

import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.FireTile;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class BossArena {

    public static final double CHARGE_END_X = 1.4;
    public static final double CHARGE_STEP = 0.28;

    private final GameSession session;
    private final Zombie boss;
    private final Random random;
    private final ChapterId chapter;

    public BossArena(GameSession session, Zombie boss, Random random, ChapterId chapter) {
        this.session = session;
        this.boss = boss;
        this.random = random == null ? new Random() : random;
        this.chapter = chapter == null ? ChapterId.ANCIENT_EGYPT : chapter;
    }

    public GameSession session() {
        return session;
    }

    public Zombie boss() {
        return boss;
    }

    public Random random() {
        return random;
    }

    public ChapterId chapter() {
        return chapter;
    }

    public GameBoard board() {
        return session.getBoard();
    }

    public double homeX() {
        return Math.max(1.0, board().getCols() - 1.0);
    }

    public int primaryRow() {
        return boss.getRow();
    }

    public int maxPrimaryRow() {
        return Math.max(0, board().getRows() - 2);
    }

    public void setClip(String clip) {
        boss.setPresentationClip(clip);
    }

    public void emit(BossVfx.Kind kind, int col, int row) {
        session.addBossVfx(new BossVfx(kind, col, row));
    }

    public boolean destroyPlantAt(int col, int row) {
        Plant plant = board().getPlantAt(col, row);
        if (plant == null || !plant.isAlive()) {
            return false;
        }
        plant.takeDamage(plant.getHealth() + 99999);
        session.removePlantFromBoard(plant);
        return true;
    }

    public int destroyPlantsOnOccupiedRows() {
        int destroyed = 0;
        for (int row : occupiedRows()) {
            destroyed += destroyPlantsOnRow(row);
        }
        return destroyed;
    }

    public int destroyPlantsOnRow(int row) {
        int destroyed = 0;
        for (Plant plant : List.copyOf(board().getAllPlants())) {
            if (plant.getRow() == row && plant.isAlive()) {
                plant.takeDamage(plant.getHealth() + 99999);
                session.removePlantFromBoard(plant);
                destroyed++;
            }
        }
        return destroyed;
    }

    public int[] occupiedRows() {
        int primary = Math.max(0, Math.min(maxPrimaryRow(), boss.getRow()));
        if (primary != boss.getRow()) {
            boss.setRow(primary);
        }
        return new int[]{primary, primary + 1};
    }

    public void placeGraves(int count) {
        int placed = 0;
        int attempts = 0;
        while (placed < count && attempts < 80) {
            attempts++;
            int col = 2 + random.nextInt(Math.max(1, board().getCols() - 3));
            int row = random.nextInt(board().getRows());
            Tile tile = board().getTile(col, row);
            if (board().getPlantAt(col, row) != null
                    || tile == null
                    || tile.isWater()
                    || tile.isGrave()
                    || tile.isIce()
                    || tile.isFire()) {
                continue;
            }
            board().setTile(col, row, new GraveTile());
            MatchListener listener = session.getMatchListener();
            if (listener != null) {
                listener.onGraveCreated(col, row, GraveTile.Loot.NONE.name());
            }
            placed++;
        }
    }

    public void placeFire(int col, int row) {
        if (!board().inBounds(col, row)) {
            return;
        }
        Tile tile = board().getTile(col, row);
        if (tile != null && tile.isWater()) {
            return;
        }
        board().setTile(col, row, new FireTile());
    }

    public void placeIce(int col, int row) {
        if (!board().inBounds(col, row)) {
            return;
        }
        board().setTile(col, row, new IceTile());
    }

    public Zombie spawnMinion(String alias, int row, double x) {
        try {
            return session.spawnZombieOfType(alias, row, x);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public void strikeFireball(int col, int row) {
        destroyPlantAt(col, row);
        placeFire(col, row);
        emit(BossVfx.Kind.FIREBALL, col, row);
        spawnImpAt(col, row);
    }

    public Zombie spawnImpAt(int col, int row) {
        double x = Math.min(board().getCols() - 0.5, Math.max(0.5, col + 0.5));
        String imp = BossCatalog.impAlias(chapter);
        Zombie spawned = spawnMinion(imp, row, x);
        if (spawned == null) {
            spawned = spawnMinion("ZombieImp", row, x);
        }
        return spawned;
    }

    public int scorchOccupiedRows() {
        int burned = 0;
        for (int row : occupiedRows()) {
            destroyPlantsOnRow(row);
            for (int col = 0; col < board().getCols(); col++) {
                placeFire(col, row);
                emit(BossVfx.Kind.FIREBALL, col, row);
                burned++;
            }
        }
        return burned;
    }

    public void pickUniqueCells(List<int[]> dest, int count) {
        dest.clear();
        if (count <= 0) {
            return;
        }
        Set<String> used = new HashSet<>();
        List<int[]> plants = new ArrayList<>();
        for (Plant plant : board().getAllPlants()) {
            if (plant.isAlive()) {
                plants.add(new int[]{plant.getCol(), plant.getRow()});
            }
        }
        while (dest.size() < count && !plants.isEmpty()) {
            int[] picked = plants.remove(random.nextInt(plants.size()));
            if (used.add(key(picked[0], picked[1]))) {
                dest.add(picked);
            }
        }
        int attempts = 0;
        while (dest.size() < count && attempts < 80) {
            attempts++;
            int col = random.nextInt(board().getCols());
            int row = random.nextInt(board().getRows());
            Tile tile = board().getTile(col, row);
            if (tile == null || tile.isWater()) {
                continue;
            }
            if (used.add(key(col, row))) {
                dest.add(new int[]{col, row});
            }
        }
        if (dest.isEmpty()) {
            dest.add(new int[]{3, occupiedRows()[0]});
        }
    }

    private static String key(int col, int row) {
        return col + "," + row;
    }

    public int pickPlantCell(int[] cell) {
        List<int[]> plants = new ArrayList<>();
        for (Plant plant : board().getAllPlants()) {
            if (plant.isAlive()) {
                plants.add(new int[]{plant.getCol(), plant.getRow()});
            }
        }
        if (!plants.isEmpty()) {
            int[] picked = plants.get(random.nextInt(plants.size()));
            cell[0] = picked[0];
            cell[1] = picked[1];
            return 1;
        }
        int attempts = 0;
        while (attempts < 40) {
            attempts++;
            int col = random.nextInt(board().getCols());
            int row = random.nextInt(board().getRows());
            Tile tile = board().getTile(col, row);
            if (tile != null && !tile.blocksPlanting()) {
                cell[0] = col;
                cell[1] = row;
                return 0;
            }
        }
        cell[0] = 3;
        cell[1] = occupiedRows()[0];
        return 0;
    }

    public void applyIceWind(int rowCount) {
        applyIceWindOnRows(pickAdjacentRows(rowCount), BossCatalog.ICE_WIND_FROST_STACKS);
    }

    public int[] pickAdjacentRows(int count) {
        int span = Math.max(1, Math.min(count, board().getRows()));
        int maxStart = Math.max(0, board().getRows() - span);
        int start = random.nextInt(maxStart + 1);
        int[] rows = new int[span];
        for (int i = 0; i < span; i++) {
            rows[i] = start + i;
        }
        return rows;
    }

    public int applyIceWindOnRows(int[] rows, int stacks) {
        if (rows == null || rows.length == 0) {
            return 0;
        }
        int times = Math.max(1, stacks);
        int hit = 0;
        for (int row : rows) {
            for (Plant plant : board().getAllPlants()) {
                if (plant.getRow() != row || !plant.isAlive() || plant.hasTag(PlantTag.FIRE)) {
                    continue;
                }
                for (int i = 0; i < times; i++) {
                    session.addPlantFrostStack(plant);
                }
                hit++;
            }
        }
        return hit;
    }

    public int freezeColumn(int col) {
        int spawned = 0;
        double x = Math.min(board().getCols() - 0.5, col + 0.5);
        for (int row = 0; row < board().getRows(); row++) {
            destroyPlantAt(col, row);
            placeIce(col, row);
            emit(BossVfx.Kind.GLACIER, col, row);
            Zombie frozen = spawnMinion("ZombieDefault", row, x);
            if (frozen != null) {
                frozen.applyFreeze(BossCatalog.FROZEN_ZOMBIE_TICKS);
                spawned++;
            }
        }
        return spawned;
    }

    public int pickWaterPlantCells(List<int[]> dest, int count) {
        dest.clear();
        if (count <= 0) {
            return 0;
        }
        List<int[]> water = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (Plant plant : board().getAllPlants()) {
            if (!plant.isAlive()) {
                continue;
            }
            Tile tile = board().getTile(plant.getCol(), plant.getRow());
            if (tile == null || !tile.isWater()) {
                continue;
            }
            if (used.add(key(plant.getCol(), plant.getRow()))) {
                water.add(new int[]{plant.getCol(), plant.getRow()});
            }
        }
        while (dest.size() < count && !water.isEmpty()) {
            dest.add(water.remove(random.nextInt(water.size())));
        }
        return dest.size();
    }

    public boolean swallowPlantAt(int col, int row) {
        return destroyPlantAt(col, row);
    }

    public boolean swallowWaterPlant(int[] cell) {
        List<Plant> water = new ArrayList<>();
        for (Plant plant : board().getAllPlants()) {
            if (!plant.isAlive()) {
                continue;
            }
            Tile tile = board().getTile(plant.getCol(), plant.getRow());
            if (tile != null && tile.isWater()) {
                water.add(plant);
            }
        }
        if (water.isEmpty()) {
            return false;
        }
        Plant plant = water.get(random.nextInt(water.size()));
        cell[0] = plant.getCol();
        cell[1] = plant.getRow();
        return destroyPlantAt(plant.getCol(), plant.getRow());
    }

    public void pullOccupiedTowardMouth() {
        double mouthX = Math.max(0.5, boss.getX() - BossCatalog.VACUUM_MOUTH_GAP);
        int[] rows = occupiedRows();
        pullPlantsOnRows(rows, mouthX);
        pullZombiesOnRows(rows, mouthX);
    }

    private void pullPlantsOnRows(int[] rows, double mouthX) {
        List<Plant> targets = new ArrayList<>();
        for (Plant plant : List.copyOf(board().getAllPlants())) {
            if (plant.isAlive() && occupiesAny(rows, plant.getRow())) {
                targets.add(plant);
            }
        }
        targets.sort((left, right) -> {
            int byCol = Integer.compare(right.getCol(), left.getCol());
            if (byCol != 0) {
                return byCol;
            }
            return Boolean.compare(left.hasTag(PlantTag.WATER), right.hasTag(PlantTag.WATER));
        });
        var context = session.getContext();
        for (Plant plant : targets) {
            if (!plant.isAlive()) {
                continue;
            }
            if (plant.getCol() + 0.5 >= mouthX) {
                destroyPlantAt(plant.getCol(), plant.getRow());
                continue;
            }
            boolean moved = context != null
                    && context.movePlant(plant, plant.getCol() + 1, plant.getRow());
            if (!moved) {
                destroyPlantAt(plant.getCol(), plant.getRow());
            }
        }
    }

    private void pullZombiesOnRows(int[] rows, double mouthX) {
        for (Zombie zombie : List.copyOf(session.getZombies())) {
            if (zombie == boss || !zombie.isAlive() || zombie.isBoss()) {
                continue;
            }
            if (!occupiesAnyOccupied(zombie, rows)) {
                continue;
            }
            if (zombie.getX() >= mouthX) {
                zombie.takeDirectDamage(zombie.getHealth() + 99999);
                session.handleZombieKilled(zombie);
            } else {
                zombie.moveRight(BossCatalog.VACUUM_ZOMBIE_STEP);
            }
        }
    }

    private static boolean occupiesAny(int[] rows, int row) {
        for (int occupied : rows) {
            if (occupied == row) {
                return true;
            }
        }
        return false;
    }

    private static boolean occupiesAnyOccupied(Zombie zombie, int[] rows) {
        for (int row : rows) {
            if (zombie.occupiesRow(row)) {
                return true;
            }
        }
        return false;
    }

    public void vacuumOccupied() {
        destroyPlantsOnOccupiedRows();
        for (Zombie zombie : List.copyOf(session.getZombies())) {
            if (zombie == boss || !zombie.isAlive() || zombie.isBoss()) {
                continue;
            }
            if (!zombie.occupiesRow(occupiedRows()[0]) && !zombie.occupiesRow(occupiedRows()[1])) {
                continue;
            }
            zombie.takeDirectDamage(zombie.getHealth() + 99999);
            session.handleZombieKilled(zombie);
        }
    }

    public int randomIdleTicks() {
        int span = BossCatalog.IDLE_MAX_TICKS - BossCatalog.IDLE_MIN_TICKS + 1;
        return BossCatalog.IDLE_MIN_TICKS + random.nextInt(span);
    }

    public double spawnX() {
        return Math.max(0.5, board().getCols() - 0.5);
    }
}
