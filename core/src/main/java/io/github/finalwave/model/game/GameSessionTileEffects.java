package io.github.finalwave.model.game;

import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.FireTile;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.NormalTile;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.zombie.ArcadeObstacle;
import io.github.finalwave.model.game.entity.zombie.PianoObstacle;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


final class GameSessionTileEffects {

    private final GameSession session;
    private final List<PlantCovering> plantCoverings = new ArrayList<>();
    private final List<ArcadeObstacle> arcadeObstacles = new ArrayList<>();
    private final List<PianoObstacle> pianoObstacles = new ArrayList<>();
    private final List<PendingGraveLanding> pendingGraveLandings = new ArrayList<>();
    private final List<PendingLaneLaser> pendingLaneLasers = new ArrayList<>();
    private final AtomicLong nextBoneId = new AtomicLong();
    private final Map<PlantCategory, Integer> familyBoostEndTicks = new HashMap<>();
    private final Map<Integer, FieldModifier> rowModifiers = new HashMap<>();
    private final Map<Integer, Map<String, Integer>> rowEffects = new HashMap<>();

    GameSessionTileEffects(GameSession session) {
        this.session = session;
    }

    List<PlantCovering> getPlantCoverings() {
        return List.copyOf(plantCoverings);
    }

    List<ArcadeObstacle> getArcadeObstacles() {
        return List.copyOf(arcadeObstacles);
    }

    List<PianoObstacle> getPianoObstacles() {
        return List.copyOf(pianoObstacles);
    }

    List<PendingGraveLanding> getPendingGraveLandings() {
        return List.copyOf(pendingGraveLandings);
    }

    void tickCoveringsAndObstacles() {
        tickPendingGraveLandings();
        tickPendingLaneLasers();
        plantCoverings.removeIf(covering -> {
            covering.onTickUpdate(session.getContext());
            return covering.isDead();
        });
        arcadeObstacles.removeIf(ArcadeObstacle::isDead);
        pianoObstacles.removeIf(PianoObstacle::isDead);
    }

    void removeDeadCoveringsAndObstacles() {
        plantCoverings.removeIf(PlantCovering::isDead);
        arcadeObstacles.removeIf(ArcadeObstacle::isDead);
        pianoObstacles.removeIf(PianoObstacle::isDead);
    }

    PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health) {
        return coverPlant(plant, type, health, null);
    }

    PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health, Zombie source) {
        if (plant == null || !plant.isAlive() || type == null) {
            return null;
        }
        for (PlantCovering covering : plantCoverings) {
            if (covering.isAlive() && covering.getCoveredPlant() == plant
                    && covering.getType() == type) {
                return covering;
            }
        }
        int hold = type == PlantCovering.Type.OCTOPUS && source != null ? 12 : 0;
        int flight = type == PlantCovering.Type.OCTOPUS && source != null ? 8 : 0;
        double fromX = source != null ? source.getX() - 0.2 : plant.getCol() + 0.5;
        double fromY = source != null ? source.getY() : plant.getRow();
        PlantCovering covering = new PlantCovering(type, plant, Math.max(1, health),
                fromX, fromY, flight, hold);
        plantCoverings.add(covering);
        return covering;
    }

    void registerHunterIceHit(Plant plant) {
        addPlantFrostStack(plant);
    }

    void addPlantFrostStack(Plant plant) {
        if (plant == null || !plant.isAlive()) {
            return;
        }
        int hits = plant.addHostileIceStack("frost");
        if (hits >= 3) {
            coverPlant(plant, PlantCovering.Type.HUNTER_ICE, 600);
            plant.clearHostileIce();
        }
    }

    void clearGraveAt(int col, int row) {
        GameBoard board = session.getBoard();
        if (!board.inBounds(col, row) || !board.getTile(col, row).isGrave()) {
            return;
        }
        var tile = board.getTile(col, row);
        GraveTile.Loot loot = GraveTile.Loot.NONE;
        if (tile instanceof GraveTile grave) {
            loot = grave.getLoot();
        }
        board.setTile(col, row, new NormalTile());
        if (loot == GraveTile.Loot.SUN_50) {
            session.addSunBalance(50);
        } else if (loot == GraveTile.Loot.PLANT_FOOD) {
            session.addPlantFood(1);
        }
    }

    boolean damageGraveAt(int col, int row, int amount) {
        GameBoard board = session.getBoard();
        if (!board.inBounds(col, row) || amount <= 0) {
            return false;
        }
        var tile = board.getTile(col, row);
        if (!(tile instanceof GraveTile grave) || grave.isDestroyed()) {
            return false;
        }
        grave.takeDamage(amount);
        if (grave.isDestroyed()) {
            clearGraveAt(col, row);
        }
        return true;
    }

    boolean damageIceAt(int col, int row, int amount) {
        GameBoard board = session.getBoard();
        if (!board.inBounds(col, row) || amount <= 0) {
            return false;
        }
        var tile = board.getTile(col, row);
        if (!(tile instanceof IceTile ice) || ice.isDestroyed()) {
            return false;
        }
        ice.takeDamage(amount);
        if (ice.isDestroyed()) {
            clearIceAt(col, row);
        }
        return true;
    }

    void clearIceAt(int col, int row) {
        GameBoard board = session.getBoard();
        if (!board.inBounds(col, row) || !board.getTile(col, row).isIce()) {
            return;
        }
        board.setTile(col, row, new NormalTile());
    }

    void tickAdjacentFireIceMelt() {
        GameBoard board = session.getBoard();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (!(board.getTile(col, row) instanceof IceTile ice) || ice.isDestroyed()) {
                    continue;
                }
                if (hasAdjacentFirePlant(col, row)) {
                    damageIceAt(col, row, IceTile.ADJACENT_FIRE_DAMAGE_PER_TICK);
                }
            }
        }
    }

    private boolean hasAdjacentFirePlant(int col, int row) {
        GameBoard board = session.getBoard();
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) {
                    continue;
                }
                int nCol = col + dCol;
                int nRow = row + dRow;
                if (!board.inBounds(nCol, nRow)) {
                    continue;
                }
                Plant plant = board.getPlantAt(nCol, nRow);
                if (plant != null && plant.isAlive() && plant.hasTag(PlantTag.FIRE)) {
                    return true;
                }
            }
        }
        return false;
    }

    void pushArcadeObstacle(Zombie pusher) {
        if (pusher == null || pusher.isDead()) {
            return;
        }
        ArcadeObstacle obstacle = arcadeObstacles.stream()
                .filter(candidate -> pusher.getId().equals(candidate.getPusherId()))
                .findFirst()
                .orElseGet(() -> {
                    ArcadeObstacle created = new ArcadeObstacle(pusher);
                    arcadeObstacles.add(created);
                    return created;
                });
        obstacle.follow(pusher);
        crushAt(obstacle.getX(), obstacle.getRow(), pusher);
    }

    void releaseArcadeObstacle(String pusherId) {
        arcadeObstacles.forEach(obstacle -> obstacle.releasePusher(pusherId));
    }

    void pushPianoObstacle(Zombie pusher) {
        if (pusher == null || pusher.isDead()) {
            return;
        }
        PianoObstacle obstacle = pianoObstacles.stream()
                .filter(candidate -> pusher.getId().equals(candidate.getPusherId()))
                .findFirst()
                .orElseGet(() -> {
                    PianoObstacle created = new PianoObstacle(pusher);
                    pianoObstacles.add(created);
                    return created;
                });
        obstacle.follow(pusher);
        crushAt(obstacle.getX(), obstacle.getRow(), pusher);
    }

    void releasePianoObstacle(String pusherId) {
        pianoObstacles.forEach(obstacle -> obstacle.releasePusher(pusherId));
    }

    void followPushedObstacles() {
        for (ArcadeObstacle obstacle : arcadeObstacles) {
            if (obstacle.isDead() || obstacle.getPusherId() == null) {
                continue;
            }
            Zombie pusher = findPusher(obstacle.getPusherId());
            if (pusher != null) {
                obstacle.follow(pusher);
                crushAt(obstacle.getX(), obstacle.getRow(), pusher);
            }
        }
        for (PianoObstacle obstacle : pianoObstacles) {
            if (obstacle.isDead() || obstacle.getPusherId() == null) {
                continue;
            }
            Zombie pusher = findPusher(obstacle.getPusherId());
            if (pusher != null) {
                obstacle.follow(pusher);
                crushAt(obstacle.getX(), obstacle.getRow(), pusher);
            }
        }
    }

    private Zombie findPusher(String pusherId) {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getId().equals(pusherId)) {
                return zombie;
            }
        }
        return null;
    }

    void queueGraveLanding(PendingGraveLanding landing) {
        if (landing != null) {
            pendingGraveLandings.add(landing);
        }
    }

    long nextBoneId() {
        return nextBoneId.incrementAndGet();
    }

    boolean hasPendingGraveAt(int col, int row) {
        for (PendingGraveLanding landing : pendingGraveLandings) {
            if (landing.col() == col && landing.row() == row) {
                return true;
            }
        }
        return false;
    }

    int pendingGraveCount() {
        return pendingGraveLandings.size();
    }

    private void tickPendingGraveLandings() {
        List<PendingGraveLanding> next = new ArrayList<>();
        for (PendingGraveLanding landing : pendingGraveLandings) {
            PendingGraveLanding stepped = landing.tickDown();
            if (stepped.ticksRemaining() > 0) {
                next.add(stepped);
                continue;
            }
            session.queueLawnBurst(new LawnBurst(LawnBurst.Kind.BONE_HIT, stepped.col(), stepped.row()));
            landGrave(stepped.col(), stepped.row());
        }
        pendingGraveLandings.clear();
        pendingGraveLandings.addAll(next);
    }

    void queueLaneLaser(int row, int fromCol, int span, int delayTicks) {
        queueLaneLaser(row, fromCol, span, delayTicks, fromCol + 0.5);
    }

    void queueLaneLaser(int row, int fromCol, int span, int delayTicks, double originX) {
        if (delayTicks <= 0) {
            fireLaneLaserNow(row, fromCol, span, originX);
            return;
        }
        pendingLaneLasers.add(new PendingLaneLaser(row, fromCol, span, delayTicks, originX));
    }

    private void tickPendingLaneLasers() {
        List<PendingLaneLaser> next = new ArrayList<>();
        for (PendingLaneLaser laser : pendingLaneLasers) {
            int remaining = laser.ticksRemaining() - 1;
            if (remaining > 0) {
                next.add(new PendingLaneLaser(laser.row(), laser.fromCol(), laser.span(), remaining,
                        laser.originX()));
                continue;
            }
            fireLaneLaserNow(laser.row(), laser.fromCol(), laser.span(), laser.originX());
        }
        pendingLaneLasers.clear();
        pendingLaneLasers.addAll(next);
    }

    private void fireLaneLaserNow(int row, int fromCol, int span) {
        fireLaneLaserNow(row, fromCol, span, fromCol + 0.5);
    }

    private void fireLaneLaserNow(int row, int fromCol, int span, double originX) {
        session.queueLawnBurst(new LawnBurst(LawnBurst.Kind.LASER, fromCol, row, Math.max(1, span), originX));
        int end = Math.max(0, fromCol - span);
        for (int col = fromCol; col >= end; col--) {
            Plant plant = session.getBoard().getPlantAt(col, row);
            if (plant != null && plant.canBeTargetedByZombie()) {
                plant.takeDamage(plant.getHealth());
                session.getContext().onPlantDestroyed(plant);
            }
        }
    }

    private void landGrave(int col, int row) {
        GameBoard board = session.getBoard();
        if (!board.inBounds(col, row)) {
            return;
        }
        var tile = board.getTile(col, row);
        Plant plant = board.getPlantAt(col, row);
        if (plant != null || tile == null || tile.blocksPlanting() || tile.isWater()
                || tile.isIce() || tile.isGrave()) {
            return;
        }
        board.setTile(col, row, new GraveTile());
    }

    private void crushAt(double x, int row, Zombie pusher) {
        int left = (int) Math.floor(x);
        int right = pusher == null ? left : (int) Math.floor(pusher.getX());
        if (left > right) {
            int swap = left;
            left = right;
            right = swap;
        }
        for (int col = left; col <= right; col++) {
            Plant plant = session.getBoard().getPlantAt(col, row);
            if (plant != null && plant.canBeTargetedByZombie()) {
                plant.takeDamage(plant.getHealth());
            }
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie != pusher && zombie.isAlive() && zombie.isHypnotized()
                    && zombie.getRow() == row
                    && Math.abs(zombie.getX() - x) <= 0.55) {
                zombie.takeDirectDamage(zombie.getHealth());
                session.handleZombieKilled(zombie);
            }
        }
    }

    void expireTimedEffects() {
        int currentTick = session.getCurrentTick();
        familyBoostEndTicks.entrySet().removeIf(entry -> entry.getValue() <= currentTick);
        rowModifiers.entrySet().removeIf(entry -> entry.getValue().endTick() <= currentTick);
        rowEffects.values().forEach(effects ->
                effects.entrySet().removeIf(entry -> entry.getValue() <= currentTick));
        rowEffects.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    void tickFireTiles() {
        GameBoard board = session.getBoard();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (board.getTile(col, row) instanceof FireTile fire && fire.tickExpired()) {
                    board.setTile(col, row, new NormalTile());
                }
            }
        }
    }

    void resetFamilyCooldowns(PlantCategory category) {
        session.getCooldownTracker().resetCategory(session.getPlantRegistry(), category.name());
    }

    void boostFamily(PlantCategory category, double durationSeconds) {
        int endTick = session.getCurrentTick()
                + (int) Math.ceil(durationSeconds * GameSession.TICKS_PER_SECOND);
        familyBoostEndTicks.merge(category, endTick, Math::max);
    }

    boolean isFamilyBoosted(PlantCategory category) {
        return familyBoostEndTicks.getOrDefault(category, 0) > session.getCurrentTick();
    }

    void applyFieldModifier(int row, double magnitude, double durationSeconds) {
        int endTick = session.getCurrentTick()
                + (int) Math.ceil(durationSeconds * GameSession.TICKS_PER_SECOND);
        rowModifiers.put(row, new FieldModifier(magnitude, endTick));
    }

    double getFieldModifier(int row) {
        FieldModifier modifier = rowModifiers.get(row);
        return modifier == null || modifier.endTick() <= session.getCurrentTick()
                ? 0.0 : modifier.magnitude();
    }

    void applyRowEffect(int row, String effectType, int durationTicks) {
        if (row < 0 || row >= session.getBoard().getRows() || effectType == null
                || effectType.isBlank() || durationTicks <= 0) {
            return;
        }
        int endTick = session.getCurrentTick() + durationTicks;
        rowEffects.computeIfAbsent(row, ignored -> new HashMap<>())
                .merge(effectType, endTick, Math::max);
    }

    boolean isRowEffectActive(int row, String effectType) {
        return rowEffects.getOrDefault(row, Map.of())
                .getOrDefault(effectType, 0) > session.getCurrentTick();
    }

    private record FieldModifier(double magnitude, int endTick) {
    }

    private record PendingLaneLaser(int row, int fromCol, int span, int ticksRemaining, double originX) {
    }
}
