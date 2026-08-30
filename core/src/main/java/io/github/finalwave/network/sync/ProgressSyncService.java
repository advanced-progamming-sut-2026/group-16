package io.github.finalwave.network.sync;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.App;
import io.github.finalwave.model.user.GreenhousePot;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.model.user.UserWriteListener;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.profile.LocalProfileCache;
import io.github.finalwave.profile.ProfileApplier;
import io.github.finalwave.util.SessionResumeCredentials;
import io.github.finalwave.util.StayLoggedInStorage;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class ProgressSyncService implements UserWriteListener, NetworkManager.ConnectionListener {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static volatile ProgressSyncService instance;

    private static final List<String> SYNC_OK_TYPES = List.of(
            MessageTypes.UPDATE_WALLET_OK,
            MessageTypes.UPDATE_PLANT_OK,
            MessageTypes.UPDATE_GREENHOUSE_POT_OK,
            MessageTypes.UPDATE_BOOSTS_OK,
            MessageTypes.UNLOCK_CONTENT_OK,
            MessageTypes.UPDATE_QUEST_PROGRESS_OK,
            MessageTypes.UPDATE_ADVENTURE_OK,
            MessageTypes.UPDATE_MINIGAME_STAGES_OK,
            MessageTypes.UPDATE_MATCH_SAVE_OK,
            MessageTypes.CLEAR_MATCH_SAVE_OK,
            MessageTypes.UPDATE_NEWS_OK,
            MessageTypes.UPDATE_SETTINGS_OK,
            MessageTypes.UPDATE_SCORE_GAME_OK
    );

    private static final List<String> SYNC_FAIL_TYPES = List.of(
            MessageTypes.UPDATE_WALLET_FAIL,
            MessageTypes.UPDATE_PLANT_FAIL,
            MessageTypes.UPDATE_GREENHOUSE_POT_FAIL,
            MessageTypes.UPDATE_BOOSTS_FAIL,
            MessageTypes.UNLOCK_CONTENT_FAIL,
            MessageTypes.UPDATE_QUEST_PROGRESS_FAIL,
            MessageTypes.UPDATE_ADVENTURE_FAIL,
            MessageTypes.UPDATE_MINIGAME_STAGES_FAIL,
            MessageTypes.UPDATE_MATCH_SAVE_FAIL,
            MessageTypes.CLEAR_MATCH_SAVE_FAIL,
            MessageTypes.UPDATE_NEWS_FAIL,
            MessageTypes.UPDATE_SETTINGS_FAIL,
            MessageTypes.UPDATE_SCORE_GAME_FAIL
    );

    private final ProgressSyncNetwork networkManager;
    private final UserDatabase userDatabase;
    private final String host;
    private final int port;
    private final ConcurrentLinkedDeque<PendingWrite> queue = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<String, PendingWrite> inFlight = new ConcurrentHashMap<>();
    private volatile boolean armed;
    private volatile boolean resuming;

    public ProgressSyncService(ProgressSyncNetwork networkManager, UserDatabase userDatabase, String host, int port) {
        this.networkManager = networkManager;
        this.userDatabase = userDatabase;
        this.host = host;
        this.port = port;
        instance = this;
        registerListeners();
    }

    public static ProgressSyncService getInstance() {
        return instance;
    }

    public void arm() {
        armed = true;
        networkManager.armReconnect(host, port);
        flushQueue();
    }

    public void refreshSession() {
        if (!armed) {
            return;
        }
        attemptResume();
    }

    public void disarm() {
        armed = false;
        queue.clear();
        inFlight.clear();
        networkManager.disarmReconnect();
        SessionResumeCredentials.clear();
    }

    @Override
    public void onConnected() {
        if (!armed) {
            return;
        }
        attemptResume();
    }

    @Override
    public void onDisconnected(String reason) {
    }

    @Override
    public void onWalletChanged(User user) {
        pushWallet(user);
    }

    public void pushWallet(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_WALLET, SyncPayloadBuilder.wallet(user));
    }

    @Override
    public void onPlantsChanged(User user, Set<String> plantNames) {
        if (!matchesCurrentUser(user) || plantNames == null) {
            return;
        }
        for (String plantName : plantNames) {
            UpdatePlantPayload payload = SyncPayloadBuilder.plant(user, plantName);
            if (payload != null) {
                enqueue(MessageTypes.UPDATE_PLANT, payload);
            }
        }
    }

    @Override
    public void onGreenhousePotChanged(User user, GreenhousePot pot) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_GREENHOUSE_POT, SyncPayloadBuilder.greenhousePot(pot));
    }

    @Override
    public void onStoredBoostsChanged(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_BOOSTS, SyncPayloadBuilder.boosts(user));
    }

    @Override
    public void onUnlocked(User user, String kind, String name) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UNLOCK_CONTENT, SyncPayloadBuilder.unlock(kind, name));
    }

    @Override
    public void onQuestProgressChanged(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_QUEST_PROGRESS, SyncPayloadBuilder.questProgress(user));
    }

    @Override
    public void onAdventureChanged(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_ADVENTURE, SyncPayloadBuilder.adventure(user));
    }

    @Override
    public void onMiniGameStagesChanged(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_MINIGAME_STAGES, SyncPayloadBuilder.minigameStages(user));
    }

    @Override
    public void onMatchSaved(User user, io.github.finalwave.model.save.MatchSaveSnapshot snapshot) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_MATCH_SAVE, SyncPayloadBuilder.matchSave(snapshot));
    }

    @Override
    public void onMatchCleared(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.CLEAR_MATCH_SAVE, null);
    }

    @Override
    public void onNewsChanged(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_NEWS, SyncPayloadBuilder.news(user));
    }

    @Override
    public void onSettingsChanged(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_SETTINGS, SyncPayloadBuilder.settings(user));
    }

    @Override
    public void onScoreGameChanged(User user) {
        if (!matchesCurrentUser(user)) {
            return;
        }
        enqueue(MessageTypes.UPDATE_SCORE_GAME, SyncPayloadBuilder.scoreGame(user));
    }

    private void enqueue(String type, Object payload) {
        if (!armed || App.getInstance().getCurrentUser() == null) {
            return;
        }
        PendingWrite write = new PendingWrite(type, payload);
        String requestId = networkManager.trySend(type, payload);
        if (requestId == null) {
            queue.add(write);
            return;
        }
        inFlight.put(requestId, write);
    }

    private void flushQueue() {
        if (!networkManager.isConnected()) {
            return;
        }
        PendingWrite write;
        while ((write = queue.peek()) != null) {
            String requestId = networkManager.trySend(write.type(), write.payload());
            if (requestId == null) {
                return;
            }
            queue.poll();
            inFlight.put(requestId, write);
        }
    }

    private void attemptResume() {
        if (resuming) {
            return;
        }
        String username = SessionResumeCredentials.username();
        String passwordHash = SessionResumeCredentials.passwordHash();
        if (username == null || passwordHash == null) {
            StayLoggedInStorage.Session session = StayLoggedInStorage.loadSession();
            if (session != null) {
                username = session.username();
                passwordHash = session.passwordHash();
            }
        }
        if (username == null || passwordHash == null) {
            flushQueue();
            return;
        }
        resuming = true;
        networkManager.trySend(MessageTypes.RESUME, new ResumePayload(username, passwordHash));
    }

    private void handleResumeOk(MessageEnvelope envelope) {
        try {
            LoginOkPayload payload = MAPPER.treeToValue(envelope.getPayload(), LoginOkPayload.class);
            Gdx.app.postRunnable(() -> reconcile(payload));
        } catch (Exception exception) {
            resuming = false;
        }
    }

    private void reconcile(LoginOkPayload payload) {
        try {
            userDatabase.setWriteEventsSuppressed(true);
            User user = ProfileApplier.apply(payload);
            App.getInstance().setCurrentUser(user);
            LocalProfileCache.sync(userDatabase, user, SessionResumeCredentials.passwordHash());
        } finally {
            userDatabase.setWriteEventsSuppressed(false);
            resuming = false;
            flushQueue();
        }
    }

    private void handleSyncOk(MessageEnvelope envelope) {
        inFlight.remove(envelope.getRequestId());
        if (MessageTypes.UPDATE_WALLET_OK.equals(envelope.getType())) {
            handleWalletOk(envelope);
            return;
        }
        if (MessageTypes.UPDATE_SCORE_GAME_OK.equals(envelope.getType())) {
            handleScoreGameOk(envelope);
        }
    }

    private void handleSyncFail(MessageEnvelope envelope) {
        PendingWrite write = inFlight.remove(envelope.getRequestId());
        if (write != null) {
            queue.add(write);
        }
        attemptResume();
    }

    private void handleWalletOk(MessageEnvelope envelope) {
        User user = App.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            UpdateWalletPayload payload = MAPPER.treeToValue(envelope.getPayload(), UpdateWalletPayload.class);
            Gdx.app.postRunnable(() -> SyncCorrections.applyWallet(user, payload));
        } catch (Exception ignored) {
        }
    }

    private void handleScoreGameOk(MessageEnvelope envelope) {
        User user = App.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            UpdateScoreGamePayload payload = MAPPER.treeToValue(envelope.getPayload(), UpdateScoreGamePayload.class);
            Gdx.app.postRunnable(() -> SyncCorrections.applyScoreGame(user, payload));
        } catch (Exception ignored) {
        }
    }

    private void registerListeners() {
        networkManager.registerListener(MessageTypes.LOGIN_OK, envelope -> Gdx.app.postRunnable(this::arm));
        networkManager.registerListener(MessageTypes.RESUME_OK, this::handleResumeOk);
        for (String type : SYNC_OK_TYPES) {
            networkManager.registerListener(type, this::handleSyncOk);
        }
        for (String type : SYNC_FAIL_TYPES) {
            networkManager.registerListener(type, this::handleSyncFail);
        }
    }

    private boolean matchesCurrentUser(User user) {
        User current = App.getInstance().getCurrentUser();
        return current != null && user != null && current.getId() == user.getId();
    }

    int pendingQueueSize() {
        return queue.size();
    }

    int inFlightSize() {
        return inFlight.size();
    }

    private record PendingWrite(String type, Object payload) {
    }
}
