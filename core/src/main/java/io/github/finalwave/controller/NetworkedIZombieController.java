package io.github.finalwave.controller;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.collection.CollectionPlantQuery;
import io.github.finalwave.model.collection.CollectionService;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog;
import io.github.finalwave.model.minigame.izombie.NetworkedIZombieHandler;
import io.github.finalwave.model.minigame.mode.NetworkedIZombieMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.match.MatchEndPayload;
import io.github.finalwave.network.match.MatchEndReason;
import io.github.finalwave.network.match.MatchReactionPayload;
import io.github.finalwave.network.match.MatchRole;
import io.github.finalwave.network.match.MatchSyncService;
import io.github.finalwave.network.match.MatchWinner;
import io.github.finalwave.view.api.minigame.DuelPickController;
import io.github.finalwave.view.api.minigame.IZombieView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class NetworkedIZombieController extends ViewController implements MatchListener, DuelPickController {

    private final User user;
    private final NetworkedIZombieMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private final MatchRole role;
    private final MatchSyncService matchSyncService;
    private final String opponentUsername;
    private final CollectionService collectionService;
    private final Map<String, CollectionPlantEntry> plantEntries = new LinkedHashMap<>();
    private final List<String> pickPool;
    private final int pickSlots;
    private final long pickDeadlineMillis;
    private final List<String> localPicks = new ArrayList<>();
    private boolean deferMatchExit;
    private boolean finishedHandled;
    private boolean plantPicksReady;
    private boolean guestPicksReady;
    private List<String> guestPicks = List.of();
    private String phase = IZombieDuelCatalog.PHASE_PICKING;
    private int secondsLeft = IZombieDuelCatalog.ROUND_SECONDS;
    private Consumer<MatchReactionPayload> reactionViewListener;
    private Consumer<Void> phaseChangeListener;

    public NetworkedIZombieController(User user,
                                    NetworkedIZombieMode mode,
                                    GameSession session,
                                    MiniGameStageConfig stage,
                                    MatchRole role,
                                    MatchSyncService matchSyncService,
                                    String opponentUsername) {
        this(user, mode, session, stage, role, matchSyncService, opponentUsername, null);
    }

    public NetworkedIZombieController(User user,
                                    NetworkedIZombieMode mode,
                                    GameSession session,
                                    MiniGameStageConfig stage,
                                    MatchRole role,
                                    MatchSyncService matchSyncService,
                                    String opponentUsername,
                                    io.github.finalwave.network.match.MatchStartPayload start) {
        this.user = user;
        this.mode = mode;
        this.session = session;
        this.stage = stage;
        this.role = role;
        this.matchSyncService = matchSyncService;
        this.opponentUsername = opponentUsername;
        this.collectionService = CollectionService.createDefault(mode.plantRegistry());
        this.session.setMatchListener(this);
        this.pickSlots = start != null && start.getSlots() > 0
                ? start.getSlots()
                : (role == MatchRole.ZOMBIE
                ? IZombieDuelCatalog.ZOMBIE_SLOTS
                : IZombieDuelCatalog.PLANT_SLOTS);
        int pickSeconds = start != null && start.getPickSeconds() > 0
                ? start.getPickSeconds()
                : IZombieDuelCatalog.PICK_SECONDS;
        this.pickDeadlineMillis = System.currentTimeMillis() + pickSeconds * 1000L;
        if (start != null && start.getPhase() != null && !start.getPhase().isBlank()) {
            this.phase = start.getPhase();
        }
        if (start != null && start.getRoundSeconds() > 0) {
            this.secondsLeft = start.getRoundSeconds();
        }
        this.pickPool = resolvePickPool();
        buildPlantEntries();
        matchSyncService.setStateListener(this::onNetworkState);
        matchSyncService.setGuestPicksListener(this::onGuestPicks);
        matchSyncService.setReactionListener(this::onReactionInbound);
    }

    private List<String> resolvePickPool() {
        if (role == MatchRole.ZOMBIE) {
            List<String> aliases = mode.allZombieAliases();
            return aliases.isEmpty() ? IZombieDuelCatalog.ZOMBIE_POOL : aliases;
        }
        List<String> owned = collectionService.selectablePlantNames(user);
        if (owned == null || owned.isEmpty()) {
            return IZombieDuelCatalog.DEFAULT_PLANTS;
        }
        return List.copyOf(owned);
    }

    public CollectionPlantEntry plantEntry(String name) {
        return name == null ? null : plantEntries.get(name);
    }

    public CollectionPlantDetail plantDetail(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return collectionService.plantDetail(user, name);
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

    public List<String> previewLaneNames() {
        if (role == MatchRole.ZOMBIE) {
            return IZombieDuelCatalog.DEFAULT_PLANTS;
        }
        return IZombieDuelCatalog.DEFAULT_ZOMBIES;
    }

    private void buildPlantEntries() {
        if (role == MatchRole.ZOMBIE) {
            return;
        }
        for (CollectionPlantEntry entry : collectionService.listPlants(user, CollectionPlantQuery.all())) {
            if (entry != null && pickPool.contains(entry.name())) {
                plantEntries.put(entry.name(), entry);
            }
        }
    }

    private void onNetworkState() {
        if (role == MatchRole.ZOMBIE) {
            String previous = phase;
            io.github.finalwave.network.match.MatchStatePayload state = matchSyncService.lastGuestState();
            if (state != null && IZombieDuelCatalog.PHASE_PLAYING.equals(state.getPhase())) {
                phase = IZombieDuelCatalog.PHASE_PLAYING;
                secondsLeft = state.getSecondsLeft();
            } else if (!session.getIZombieZombiePool().isEmpty()) {
                phase = IZombieDuelCatalog.PHASE_PLAYING;
            }
            if (!previous.equals(phase)) {
                notifyPhaseChange();
            }
            getViewApi().showStageStarted(
                    stage.getStageIndex(),
                    stage.getRedLineColumn(),
                    stage.getStartingSun());
            getViewApi().showRoster(session.getIZombieZombiePool(), session.getIZombieZombieCosts());
        }
        requestHostSync();
    }

    private void onGuestPicks(List<String> picks) {
        if (role != MatchRole.PLANT || IZombieDuelCatalog.PHASE_PLAYING.equals(phase)) {
            return;
        }
        guestPicks = picks == null ? List.of() : List.copyOf(picks);
        guestPicksReady = true;
        tryStartPlay();
    }

    private void onReactionInbound(MatchReactionPayload payload) {
        Consumer<MatchReactionPayload> listener = reactionViewListener;
        if (listener != null) {
            listener.accept(payload);
        }
    }

    @Override
    public void displayMenu() {
        getViewApi().showStageStarted(
                stage.getStageIndex(),
                stage.getRedLineColumn(),
                stage.getStartingSun());
        if (role == MatchRole.ZOMBIE) {
            getViewApi().showRoster(session.getIZombieZombiePool(), session.getIZombieZombieCosts());
        } else {
            getViewApi().showRoster(List.of(), Map.of());
        }
    }

    public MatchRole role() {
        return role;
    }

    @Override
    public boolean zombieSide() {
        return role == MatchRole.ZOMBIE;
    }

    public String opponentUsername() {
        return opponentUsername;
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

    public List<String> pickPool() {
        return pickPool;
    }

    @Override
    public int pickSlots() {
        return pickSlots;
    }

    @Override
    public List<String> localPicks() {
        return List.copyOf(localPicks);
    }

    @Override
    public int pickSecondsLeft() {
        if (!isPicking()) {
            return 0;
        }
        long remaining = pickDeadlineMillis - System.currentTimeMillis();
        return (int) Math.max(0L, (remaining + 999L) / 1000L);
    }

    public int secondsLeft() {
        if (isPicking()) {
            return IZombieDuelCatalog.ROUND_SECONDS;
        }
        if (session.getActiveMiniGameHandler() instanceof NetworkedIZombieHandler handler) {
            return handler.secondsLeft();
        }
        io.github.finalwave.network.match.MatchStatePayload state = matchSyncService.lastGuestState();
        if (state != null) {
            return state.getSecondsLeft();
        }
        return secondsLeft;
    }

    public void setReactionViewListener(Consumer<MatchReactionPayload> reactionViewListener) {
        this.reactionViewListener = reactionViewListener;
    }

    public void setPhaseChangeListener(Consumer<Void> phaseChangeListener) {
        this.phaseChangeListener = phaseChangeListener;
    }

    @Override
    public void togglePick(String name) {
        if (!isPicking() || name == null || name.isBlank()) {
            return;
        }
        String trimmed = name.trim();
        if (!pickPool.contains(trimmed)) {
            return;
        }
        if (localPicks.contains(trimmed)) {
            localPicks.remove(trimmed);
            return;
        }
        if (localPicks.size() >= pickSlots) {
            return;
        }
        localPicks.add(trimmed);
    }

    @Override
    public void submitPicks() {
        if (!isPicking()) {
            return;
        }
        if (role == MatchRole.PLANT) {
            plantPicksReady = true;
            tryStartPlay();
            return;
        }
        matchSyncService.sendGuestPicks(List.copyOf(localPicks));
        guestPicksReady = true;
    }

    public void sendReaction(String kind, int index) {
        if (!isPlaying()) {
            return;
        }
        String username = user == null ? "" : user.getUsername();
        matchSyncService.sendReaction(kind, index, username);
    }

    public void setDeferMatchExit(boolean deferMatchExit) {
        this.deferMatchExit = deferMatchExit;
    }

    public void confirmMatchExit() {
        sendForfeitAndLeave();
    }

    public void restartMatch() {
        sendForfeitAndLeave();
    }

    private void sendForfeitAndLeave() {
        matchSyncService.sendForfeit();
        matchSyncService.setStateListener(null);
        matchSyncService.setListener(null);
        matchSyncService.setGuestPicksListener(null);
        matchSyncService.setReactionListener(null);
        matchSyncService.clear();
        navigator.pop();
    }

    public void advance(int ticks) {
        if (role != MatchRole.PLANT || ticks <= 0) {
            return;
        }
        if (isPicking()) {
            if (System.currentTimeMillis() >= pickDeadlineMillis) {
                plantPicksReady = true;
                guestPicksReady = true;
                tryStartPlay();
            }
            return;
        }
        session.advanceTicks(ticks);
        if (session.getActiveMiniGameHandler() instanceof NetworkedIZombieHandler handler) {
            secondsLeft = handler.secondsLeft();
        }
        matchSyncService.tickHost();
        maybeReturnAfterMatch();
    }

    public void advanceGuest(int ticks) {
        if (role != MatchRole.ZOMBIE || ticks <= 0) {
            return;
        }
        if (isPicking() && System.currentTimeMillis() >= pickDeadlineMillis && !guestPicksReady) {
            submitPicks();
        }
        matchSyncService.tickGuest();
    }

    public void tickHostSync() {
        if (role == MatchRole.PLANT) {
            matchSyncService.tickHost();
            maybeReturnAfterMatch();
        }
    }

    public void requestHostSync() {
        if (role == MatchRole.ZOMBIE) {
            matchSyncService.sendGuestReady();
        }
    }

    private void tryStartPlay() {
        if (role != MatchRole.PLANT || IZombieDuelCatalog.PHASE_PLAYING.equals(phase)) {
            return;
        }
        boolean timedOut = System.currentTimeMillis() >= pickDeadlineMillis;
        if (!timedOut && !(plantPicksReady && guestPicksReady)) {
            return;
        }
        List<String> plants = plantPicksReady ? List.copyOf(localPicks) : List.of();
        List<String> zombies = guestPicksReady ? guestPicks : List.of();
        mode.applyPicks(session, plants, zombies);
        phase = IZombieDuelCatalog.PHASE_PLAYING;
        secondsLeft = IZombieDuelCatalog.ROUND_SECONDS;
        getViewApi().showRoster(List.of(), Map.of());
        matchSyncService.pushHostSnapshotNow();
        notifyPhaseChange();
    }

    private void notifyPhaseChange() {
        Consumer<Void> listener = phaseChangeListener;
        if (listener != null) {
            listener.accept(null);
        }
    }

    public PlantPlacementResult plantSeed(String plantName, int col, int row) {
        if (role != MatchRole.PLANT || isPicking()) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        PlantPlacementResult result = session.tryPlant(plantName, col, row, 1);
        if (result == PlantPlacementResult.SUCCESS) {
            tickHostSync();
        }
        return result;
    }

    public boolean shovelAt(int col, int row) {
        if (role != MatchRole.PLANT || isPicking()) {
            return false;
        }
        if (!session.pluckPlant(col, row)) {
            return false;
        }
        tickHostSync();
        return true;
    }

    public PlantPlacementResult placeZombie(String alias, int col, int row) {
        if (role != MatchRole.ZOMBIE || isPicking()) {
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
        if (!session.getIZombieZombiePool().contains(type)) {
            getViewApi().errorNotInRoster(type);
            return PlantPlacementResult.NOT_IN_LOADOUT;
        }
        if (col <= session.getIZombiePlacementColumn()) {
            getViewApi().errorBeyondPlantingLine(col, row, session.getIZombiePlacementColumn());
            return PlantPlacementResult.BEYOND_PLANTING_LINE;
        }
        matchSyncService.sendGuestPlaceZombie(type, col, row);
        getViewApi().showZombiePlaced(type, col, row);
        return PlantPlacementResult.SUCCESS;
    }

    public boolean collectSunAt(int col, int row) {
        if (role != MatchRole.PLANT || isPicking()) {
            return false;
        }
        boolean collected = session.collectSunAt(col, row);
        if (collected) {
            tickHostSync();
        }
        return collected;
    }

    public void cheatAddSun(int amount) {
        if (amount <= 0) {
            return;
        }
        if (role == MatchRole.ZOMBIE) {
            session.addIZombieSunBalance(amount);
        } else {
            session.addSunBalance(amount);
        }
    }

    public void handleNetworkMatchEnd(MatchEndPayload payload) {
        if (payload == null || payload.getMatchId() == null) {
            return;
        }
        String activeMatchId = matchSyncService.matchId();
        if (activeMatchId == null || !activeMatchId.equals(payload.getMatchId())) {
            return;
        }
        if (payload.getReason() == MatchEndReason.OPPONENT_DISCONNECTED) {
            returnToMatchmakingAfterOpponentLeft();
            return;
        }
        if (payload.getWinner() == null) {
            return;
        }
        boolean zombieWon = payload.getWinner() == MatchWinner.ZOMBIE;
        if (role == MatchRole.ZOMBIE) {
            if (zombieWon) {
                session.winMatch();
            } else {
                session.loseMatch();
            }
        } else if (zombieWon) {
            session.loseMatch();
        } else {
            session.winMatch();
        }
        maybeReturnAfterMatch();
    }

    private void returnToMatchmakingAfterOpponentLeft() {
        if (finishedHandled) {
            return;
        }
        finishedHandled = true;
        matchSyncService.setStateListener(null);
        matchSyncService.setListener(null);
        matchSyncService.setGuestPicksListener(null);
        matchSyncService.setReactionListener(null);
        matchSyncService.clear();
        getViewApi().showOpponentLeft();
        navigator.pop();
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
        if (deferMatchExit) {
            return;
        }
        matchSyncService.clear();
        navigator.pop();
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
        if (role != MatchRole.PLANT) {
            return;
        }
        getViewApi().showWinMessage();
    }

    @Override
    public void onLose() {
        if (role != MatchRole.PLANT) {
            return;
        }
        getViewApi().showLoseMessage();
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
}
