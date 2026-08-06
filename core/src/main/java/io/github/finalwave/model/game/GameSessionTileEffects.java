package io.github.finalwave.model.game;

import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.NormalTile;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.zombie.ArcadeObstacle;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


final class GameSessionTileEffects {

    private final GameSession session;
    private final List<PlantCovering> plantCoverings = new ArrayList<>();
    private final List<ArcadeObstacle> arcadeObstacles = new ArrayList<>();
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

    void tickCoveringsAndObstacles() {
        plantCoverings.removeIf(covering -> {
            covering.onTickUpdate(session.getContext());
            return covering.isDead();
        });
        arcadeObstacles.removeIf(ArcadeObstacle::isDead);
    }

    void removeDeadCoveringsAndObstacles() {
        plantCoverings.removeIf(PlantCovering::isDead);
        arcadeObstacles.removeIf(ArcadeObstacle::isDead);
    }

    PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health) {
        if (plant == null || !plant.isAlive() || type == null) {
            return null;
        }
        for (PlantCovering covering : plantCoverings) {
            if (covering.isAlive() && covering.getCoveredPlant() == plant
                    && covering.getType() == type) {
                return covering;
            }
        }
        PlantCovering covering = new PlantCovering(type, plant, Math.max(1, health));
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
        int col = (int) Math.floor(obstacle.getX());
        Plant plant = session.getBoard().getPlantAt(col, obstacle.getRow());
        if (plant != null && plant.canBeTargetedByZombie()) {
            plant.takeDamage(plant.getHealth());
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie != pusher && zombie.isAlive() && zombie.isHypnotized()
                    && zombie.getRow() == obstacle.getRow()
                    && Math.abs(zombie.getX() - obstacle.getX()) <= 0.55) {
                zombie.takeDirectDamage(zombie.getHealth());
                session.handleZombieKilled(zombie);
            }
        }
    }

    void releaseArcadeObstacle(String pusherId) {
        arcadeObstacles.forEach(obstacle -> obstacle.releasePusher(pusherId));
    }

    void expireTimedEffects() {
        int currentTick = session.getCurrentTick();
        familyBoostEndTicks.entrySet().removeIf(entry -> entry.getValue() <= currentTick);
        rowModifiers.entrySet().removeIf(entry -> entry.getValue().endTick() <= currentTick);
        rowEffects.values().forEach(effects ->
                effects.entrySet().removeIf(entry -> entry.getValue() <= currentTick));
        rowEffects.entrySet().removeIf(entry -> entry.getValue().isEmpty());
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
}
