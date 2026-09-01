package io.github.finalwave.controller;

import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.collection.CollectionPlantQuery;
import io.github.finalwave.model.collection.CollectionService;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog;
import io.github.finalwave.model.minigame.izombie.NetworkedIZombieHandler;
import io.github.finalwave.model.minigame.mode.NetworkedIZombieMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.api.minigame.DuelPickController;
import io.github.finalwave.view.api.minigame.IZombieView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CouchIZombieController extends ViewController implements MatchListener {

    private final User user;
    private final NetworkedIZombieMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private final CollectionService collectionService;
    private final Map<String, CollectionPlantEntry> plantEntries = new LinkedHashMap<>();
    private final CouchPickSide plantSide;
    private final CouchPickSide zombieSide;
    private final long pickDeadlineMillis;
    private boolean plantReady;
    private boolean zombieReady;
    private boolean deferMatchExit;
    private boolean finishedHandled;
    private String phase = IZombieDuelCatalog.PHASE_PICKING;
    private Runnable phaseChangeListener;

    public CouchIZombieController(User user,
                                  NetworkedIZombieMode mode,
                                  GameSession session,
                                  MiniGameStageConfig stage) {
        this.user = user;
        this.mode = mode;
        this.session = session;
        this.stage = stage;
        this.collectionService = CollectionService.createDefault(mode.plantRegistry());
        this.session.setMatchListener(this);
        this.plantSide = new CouchPickSide(false, resolvePlantPool(), IZombieDuelCatalog.PLANT_SLOTS);
        this.zombieSide = new CouchPickSide(true, resolveZombiePool(), IZombieDuelCatalog.ZOMBIE_SLOTS);
        this.pickDeadlineMillis = System.currentTimeMillis()
                + IZombieDuelCatalog.PICK_SECONDS * 1000L;
        buildPlantEntries();
    }

    public GameSession session() {
        return session;
    }

    public User getUser() {
        return user;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public String phase() {
        return phase;
    }

    public boolean isPicking() {
        return IZombieDuelCatalog.PHASE_PICKING.equals(phase);
    }

    public boolean isPlaying() {
        return IZombieDuelCatalog.PHASE_PLAYING.equals(phase);
    }

    public CouchPickSide plantPicks() {
        return plantSide;
    }

    public CouchPickSide zombiePicks() {
        return zombieSide;
    }

    public void setPhaseChangeListener(Runnable listener) {
        this.phaseChangeListener = listener;
    }

    public void setDeferMatchExit(boolean deferMatchExit) {
        this.deferMatchExit = deferMatchExit;
    }

    @Override
    public void displayMenu() {
        getViewApi().showStageStarted(
                stage.getStageIndex(),
                stage.getRedLineColumn(),
                stage.getStartingSun());
        getViewApi().showRoster(List.of(), Map.of());
    }

    public void confirmMatchExit() {
        navigator.pop();
    }

    public void restartMatch() {
        navigator.pop();
    }

    public void advance(int ticks) {
        if (ticks <= 0) {
            return;
        }
        if (isPicking()) {
            if (System.currentTimeMillis() >= pickDeadlineMillis) {
                plantReady = true;
                zombieReady = true;
                tryStartPlay();
            }
            return;
        }
        session.advanceTicks(ticks);
        maybeReturnAfterMatch();
    }

    public PlantPlacementResult plantSeed(String plantName, int col, int row) {
        if (isPicking() || plantName == null || plantName.isBlank()) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        return session.tryPlant(plantName.trim(), col, row, 1);
    }

    public boolean shovelAt(int col, int row) {
        return !isPicking() && session.pluckPlant(col, row);
    }

    public boolean collectSunAt(int col, int row) {
        return !isPicking() && session.collectSunAt(col, row);
    }

    public PlantPlacementResult placeZombie(String alias, int col, int row) {
        if (isPicking()) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        if (alias == null || alias.isBlank()) {
            getViewApi().errorUnknownZombie(alias);
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return PlantPlacementResult.OUT_OF_BOUNDS;
        }
        String type = alias.trim();
        PlantPlacementResult result = session.tryPlaceZombie(type, col, row);
        switch (result) {
            case SUCCESS -> getViewApi().showZombiePlaced(type, col, row);
            case BEYOND_PLANTING_LINE -> getViewApi().errorBeyondPlantingLine(
                    col, row, session.getIZombiePlacementColumn());
            case NOT_IN_LOADOUT -> getViewApi().errorNotInRoster(type);
            case INSUFFICIENT_SUN -> {
                Integer cost = session.getIZombieZombieCosts().get(type);
                getViewApi().errorInsufficientSun(
                        type, cost == null ? 0 : cost, session.getIZombieSunBalance());
            }
            case ON_COOLDOWN -> getViewApi().errorOnCooldown(type);
            case OUT_OF_BOUNDS -> getViewApi().errorInvalidLocation(col, row);
            default -> getViewApi().errorInvalidLocation(col, row);
        }
        maybeReturnAfterMatch();
        return result;
    }

    public int zombieSunBalance() {
        return session.getIZombieSunBalance();
    }

    public CollectionPlantEntry plantEntry(String name) {
        return name == null ? null : plantEntries.get(name);
    }

    public int plantCost(String name) {
        PlantDefinition definition = mode.plantRegistry().getDefinition(name);
        return definition == null ? 0 : definition.getCost();
    }

    public boolean plantBoosted(String name) {
        return user != null && user.hasStoredBoost(name);
    }

    public boolean plantSelectable(String name) {
        return collectionService.canSelectPlant(user, name);
    }

    private void buildPlantEntries() {
        for (CollectionPlantEntry entry : collectionService.listPlants(user, CollectionPlantQuery.all())) {
            if (entry != null && plantSide.pickPool().contains(entry.name())) {
                plantEntries.put(entry.name(), entry);
            }
        }
    }

    private void tryStartPlay() {
        if (!isPicking()) {
            return;
        }
        boolean timedOut = System.currentTimeMillis() >= pickDeadlineMillis;
        if (!timedOut && !(plantReady && zombieReady)) {
            return;
        }
        mode.applyPicks(session, plantSide.localPicks(), zombieSide.localPicks());
        phase = IZombieDuelCatalog.PHASE_PLAYING;
        getViewApi().showRoster(session.getIZombieZombiePool(), session.getIZombieZombieCosts());
        notifyPhaseChange();
    }

    private void notifyPhaseChange() {
        if (phaseChangeListener != null) {
            phaseChangeListener.run();
        }
    }

    private void maybeReturnAfterMatch() {
        if (finishedHandled) {
            return;
        }
        MatchResult result = session.getMatchResult();
        if (result != MatchResult.WON && result != MatchResult.LOST) {
            return;
        }
        finishedHandled = true;
        if (result == MatchResult.WON) {
            getViewApi().showWinMessage();
        } else {
            getViewApi().showLoseMessage();
        }
        if (!deferMatchExit) {
            navigator.pop();
        }
    }

    private List<String> resolvePlantPool() {
        List<String> owned = collectionService.selectablePlantNames(user);
        if (owned == null || owned.isEmpty()) {
            return IZombieDuelCatalog.DEFAULT_PLANTS;
        }
        return List.copyOf(owned);
    }

    private List<String> resolveZombiePool() {
        List<String> aliases = mode.allZombieAliases();
        return aliases.isEmpty() ? IZombieDuelCatalog.ZOMBIE_POOL : aliases;
    }

    private IZombieView getViewApi() {
        return (IZombieView) view;
    }

    @Override
    public void onBrainEaten(int row) {
        getViewApi().showBrainEaten(row);
    }

    @Override
    public void onWin() {
        getViewApi().showWinMessage();
    }

    @Override
    public void onLose() {
        getViewApi().showLoseMessage();
    }

    @Override
    public void onZombieSpawned(String type, int wave, int lane, int cost) {
    }

    @Override
    public void onZombieDied(String type, double x, double y) {
    }

    @Override
    public void onPlantDestroyed(Plant plant, int x, int y) {
    }

    @Override
    public void onSunDropped(SunType type, int x, int y) {
    }

    @Override
    public void onLawnMowerTriggered(int row, List<Zombie> killed) {
    }

    public final class CouchPickSide implements DuelPickController {
        private final boolean zombie;
        private final List<String> pool;
        private final int slots;
        private final List<String> picks = new ArrayList<>();

        private CouchPickSide(boolean zombie, List<String> pool, int slots) {
            this.zombie = zombie;
            this.pool = pool;
            this.slots = slots;
        }

        public boolean ready() {
            return zombie ? zombieReady : plantReady;
        }

        @Override
        public boolean zombieSide() {
            return zombie;
        }

        @Override
        public List<String> pickPool() {
            return pool;
        }

        @Override
        public int pickSlots() {
            return slots;
        }

        @Override
        public List<String> localPicks() {
            return List.copyOf(picks);
        }

        @Override
        public int pickSecondsLeft() {
            if (!isPicking()) {
                return 0;
            }
            long remaining = pickDeadlineMillis - System.currentTimeMillis();
            return (int) Math.max(0L, (remaining + 999L) / 1000L);
        }

        @Override
        public void togglePick(String name) {
            if (!isPicking() || name == null || name.isBlank()) {
                return;
            }
            String trimmed = name.trim();
            if (!pool.contains(trimmed)) {
                return;
            }
            if (picks.contains(trimmed)) {
                picks.remove(trimmed);
                return;
            }
            if (picks.size() >= slots) {
                return;
            }
            picks.add(trimmed);
        }

        @Override
        public void submitPicks() {
            if (!isPicking()) {
                return;
            }
            if (zombie) {
                zombieReady = true;
            } else {
                plantReady = true;
            }
            tryStartPlay();
        }
    }
}
