package io.github.finalwave.model.game;

import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.beghouled.BeghouledBoard;
import io.github.finalwave.model.minigame.beghouled.BeghouledSwapOutcome;
import io.github.finalwave.model.minigame.beghouled.BeghouledSwapResult;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeCatalog;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeOutcome;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeResult;
import io.github.finalwave.model.minigame.bowling.BowlingNut;
import io.github.finalwave.model.minigame.bowling.BowlingNutSystem;
import io.github.finalwave.model.minigame.bowling.BowlingNutType;
import io.github.finalwave.model.minigame.izombie.IZombiePacketRecharge;
import io.github.finalwave.model.quest.event.GameEvent;

import java.util.List;
import java.util.Map;
import java.util.Random;


final class GameSessionMiniGameState {

    private final GameSession session;

    private boolean walnutBowlingActive;
    private int walnutBowlingRedLineColumn = -1;
    private final BowlingNutSystem bowlingNutSystem;

    private boolean iZombieActive;
    private int iZombiePlacementColumn = -1;
    private List<String> iZombieZombiePool = List.of();
    private Map<String, Integer> iZombieZombieCosts = Map.of();
    private boolean[] iZombieBrainsEaten = new boolean[0];
    private int iZombieSunBalance;

    private boolean beghouledActive;
    private int beghouledMatchTarget;
    private final BeghouledBoard beghouledBoard = new BeghouledBoard();

    GameSessionMiniGameState(GameSession session, Random random) {
        this.session = session;
        this.bowlingNutSystem = new BowlingNutSystem(random);
    }

    void activateWalnutBowling(int redLineColumn) {
        walnutBowlingActive = true;
        walnutBowlingRedLineColumn = Math.max(0, redLineColumn);
    }

    boolean isWalnutBowlingActive() {
        return walnutBowlingActive;
    }

    int getWalnutBowlingRedLineColumn() {
        return walnutBowlingRedLineColumn;
    }

    BowlingNutSystem getBowlingNutSystem() {
        return bowlingNutSystem;
    }

    PlantPlacementResult tryPlantBowlingNut(String plantName, int col, int row) {
        if (!walnutBowlingActive) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        BowlingNutType type = BowlingNutType.fromPlantName(plantName);
        if (type == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        GameBoard board = session.getBoard();
        if (!board.inBounds(col, row)) {
            return PlantPlacementResult.OUT_OF_BOUNDS;
        }
        if (col > walnutBowlingRedLineColumn) {
            return PlantPlacementResult.BEYOND_PLANTING_LINE;
        }
        if (!session.isConveyorBeltActive() || !session.hasConveyorBeltPlant(plantName)) {
            return PlantPlacementResult.NOT_ON_CONVEYOR_BELT;
        }
        if (board.getGroundPlantAt(col, row) != null || board.getOverlayPlantAt(col, row) != null) {
            return PlantPlacementResult.GROUND_OCCUPIED;
        }
        session.removeConveyorBeltPlant(plantName);
        BowlingNut nut = new BowlingNut(type, col, row);
        bowlingNutSystem.spawn(nut);
        MatchListener matchListener = session.getMatchListener();
        if (matchListener != null) {
            matchListener.onBowlingNutSpawned(plantName, col, row);
        }
        return PlantPlacementResult.SUCCESS;
    }

    void activateIZombie(int placementColumn, List<String> zombiePool, Map<String, Integer> zombieCosts) {
        iZombieActive = true;
        iZombiePlacementColumn = Math.max(0, placementColumn);
        iZombieZombiePool = zombiePool == null ? List.of() : List.copyOf(zombiePool);
        iZombieZombieCosts = zombieCosts == null ? Map.of() : Map.copyOf(zombieCosts);
        iZombieBrainsEaten = new boolean[session.getBoard().getRows()];
        iZombieSunBalance = session.getSunBalance();
    }

    boolean isIZombieActive() {
        return iZombieActive;
    }

    int getIZombiePlacementColumn() {
        return iZombiePlacementColumn;
    }

    List<String> getIZombieZombiePool() {
        return iZombieZombiePool;
    }

    Map<String, Integer> getIZombieZombieCosts() {
        return iZombieZombieCosts;
    }

    boolean isIZombieBrainEaten(int row) {
        return row >= 0 && row < iZombieBrainsEaten.length && iZombieBrainsEaten[row];
    }

    int getIZombieBrainsEatenCount() {
        int count = 0;
        for (boolean eaten : iZombieBrainsEaten) {
            if (eaten) {
                count++;
            }
        }
        return count;
    }

    boolean areAllIZombieBrainsEaten() {
        if (!iZombieActive || iZombieBrainsEaten.length == 0) {
            return false;
        }
        for (boolean eaten : iZombieBrainsEaten) {
            if (!eaten) {
                return false;
            }
        }
        return true;
    }

    /** Marks the row's brain as eaten if it wasn't already; returns whether it was newly marked. */
    boolean markBrainEatenIfNew(int row) {
        if (row >= 0 && row < iZombieBrainsEaten.length && !iZombieBrainsEaten[row]) {
            iZombieBrainsEaten[row] = true;
            return true;
        }
        return false;
    }

    void syncIZombieBrainsFromNetwork(boolean[] eaten) {
        if (eaten == null || eaten.length != iZombieBrainsEaten.length) {
            return;
        }
        System.arraycopy(eaten, 0, iZombieBrainsEaten, 0, eaten.length);
    }

    int getIZombieSunBalance() {
        return iZombieSunBalance;
    }

    void setIZombieSunBalance(int amount) {
        iZombieSunBalance = Math.max(0, amount);
    }

    void addIZombieSunBalance(int amount) {
        if (amount > 0) {
            iZombieSunBalance += amount;
        }
    }

    void withdrawIZombieSun(int amount) {
        if (amount > 0) {
            iZombieSunBalance = Math.max(0, iZombieSunBalance - amount);
        }
    }

    PlantPlacementResult tryPlaceZombie(String alias, int col, int row) {
        if (!iZombieActive) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        if (alias == null || alias.isBlank()) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        String trimmed = alias.trim();
        if (!iZombieZombiePool.contains(trimmed)) {
            return PlantPlacementResult.NOT_IN_LOADOUT;
        }
        if (!session.getBoard().inBounds(col, row)) {
            return PlantPlacementResult.OUT_OF_BOUNDS;
        }
        if (col <= iZombiePlacementColumn) {
            return PlantPlacementResult.BEYOND_PLANTING_LINE;
        }
        Integer cost = iZombieZombieCosts.get(trimmed);
        if (cost == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (session.getIZombieSunBalance() < cost) {
            return PlantPlacementResult.INSUFFICIENT_SUN;
        }
        if (!session.getCooldownTracker().isReady(trimmed)) {
            return PlantPlacementResult.ON_COOLDOWN;
        }
        if (session.getZombieFactory() == null) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        try {
            Zombie placed = session.spawnZombieOfType(trimmed, row, col);
            placed.lockLane();
        } catch (IllegalArgumentException e) {
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        session.withdrawIZombieSun(cost);
        session.getEventBus().publish(new GameEvent.SunSpent(cost));
        session.getCooldownTracker().startCooldown(
                trimmed,
                IZombiePacketRecharge.secondsFor(trimmed, cost),
                GameSession.TICKS_PER_SECOND);
        return PlantPlacementResult.SUCCESS;
    }

    void setIZombieRoster(List<String> zombiePool, Map<String, Integer> zombieCosts) {
        if (!iZombieActive) {
            return;
        }
        iZombieZombiePool = zombiePool == null ? List.of() : List.copyOf(zombiePool);
        iZombieZombieCosts = zombieCosts == null ? Map.of() : Map.copyOf(zombieCosts);
    }

    int getIZombieCheapestRosterCost() {
        if (iZombieZombieCosts.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int cheapest = Integer.MAX_VALUE;
        for (int cost : iZombieZombieCosts.values()) {
            cheapest = Math.min(cheapest, cost);
        }
        return cheapest;
    }

    void activateBeghouled(List<String> plantPool, int matchTarget, BeghouledUpgradeCatalog catalog) {
        beghouledActive = true;
        beghouledMatchTarget = Math.max(1, matchTarget);
        beghouledBoard.configure(plantPool, catalog, session.getRandom());
        beghouledBoard.fillRandomly(session);
    }

    boolean isBeghouledActive() {
        return beghouledActive;
    }

    BeghouledBoard getBeghouledBoard() {
        return beghouledBoard;
    }

    int getBeghouledMatchTarget() {
        return beghouledMatchTarget;
    }

    BeghouledSwapResult trySwapBeghouledPlants(int colA, int rowA, int colB, int rowB) {
        if (!beghouledActive) {
            return BeghouledSwapResult.failure(BeghouledSwapOutcome.OUT_OF_BOUNDS);
        }
        BeghouledSwapResult result = beghouledBoard.trySwap(session, colA, rowA, colB, rowB);
        if (result.outcome() == BeghouledSwapOutcome.SUCCESS
                && session.getActiveMiniGameHandler() != null) {
            session.getActiveMiniGameHandler().onTick(session);
        }
        return result;
    }

    BeghouledUpgradeResult tryBeghouledUpgrade(String plantName) {
        if (!beghouledActive) {
            return BeghouledUpgradeResult.failure(BeghouledUpgradeOutcome.UNKNOWN_UPGRADE);
        }
        return beghouledBoard.applyUpgrade(session, plantName);
    }
}
