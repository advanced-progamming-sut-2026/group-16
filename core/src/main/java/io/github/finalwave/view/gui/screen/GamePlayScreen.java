package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.controller.CouchIZombieController;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.controller.IZombieController;
import io.github.finalwave.controller.NetworkedIZombieController;
import io.github.finalwave.controller.ScoreGamePlayController;
import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.controller.WalnutBowlingController;
import io.github.finalwave.controller.ZombotanyController;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.ChapterRules;
import io.github.finalwave.model.collection.CollectionService;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.SeedPlacement;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.minigame.GroundSeedPacket;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeRule;
import io.github.finalwave.model.minigame.izombie.IZombieHandler;
import io.github.finalwave.network.match.MatchRole;
import io.github.finalwave.model.scoregame.MeowPointBreakdown;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.hud.AlertBanner;
import io.github.finalwave.view.gui.hud.CouchPickOverlay;
import io.github.finalwave.view.gui.hud.DuelPickOverlay;
import io.github.finalwave.view.gui.hud.LevelObjectiveBanner;
import io.github.finalwave.view.gui.hud.MatchResultModal;
import io.github.finalwave.view.gui.hud.NpcDialogBox;
import io.github.finalwave.view.gui.hud.NpcDialogLine;
import io.github.finalwave.view.gui.hud.NpcDialogScript;
import io.github.finalwave.view.gui.hud.PauseButton;
import io.github.finalwave.view.gui.hud.PauseModal;
import io.github.finalwave.view.gui.hud.PlantFoodCounter;
import io.github.finalwave.view.gui.hud.ReactionBar;
import io.github.finalwave.view.gui.hud.ReactionToast;
import io.github.finalwave.view.gui.hud.SeedBankBar;
import io.github.finalwave.view.gui.hud.ShovelButton;
import io.github.finalwave.view.gui.hud.SpeedButton;
import io.github.finalwave.view.gui.hud.SunCounter;
import io.github.finalwave.view.gui.hud.WaveProgressMeter;
import io.github.finalwave.view.gui.hud.PlantSandboxPanel;
import io.github.finalwave.view.gui.hud.ZombieRosterBar;
import io.github.finalwave.view.gui.hud.ZombieSandboxPanel;
import io.github.finalwave.view.gui.hud.special.BeghouledUpgradeBar;
import io.github.finalwave.view.gui.hud.special.ConveyorBeltBar;
import io.github.finalwave.view.gui.hud.special.LoveYourPlantsCounter;
import io.github.finalwave.view.gui.hud.special.MeowPointBanner;
import io.github.finalwave.view.gui.hud.special.StartWaveButton;
import io.github.finalwave.view.gui.widget.IcebergFlashOverlay;
import io.github.finalwave.view.gui.hud.special.TimedWarPanel;
import io.github.finalwave.view.gui.hud.special.ZombossHealthMeter;
import io.github.finalwave.view.gui.input.ControllerLawnHost;
import io.github.finalwave.view.gui.input.CouchZombieKeys;
import io.github.finalwave.view.gui.input.LawnActionHost;
import io.github.finalwave.view.gui.input.LawnInputController;
import io.github.finalwave.view.gui.input.ToolMode;
import io.github.finalwave.view.gui.match.ControllerTicker;
import io.github.finalwave.view.gui.match.MatchClock;
import io.github.finalwave.view.gui.render.BattlefieldGroup;
import io.github.finalwave.view.gui.render.ChapterBackground;
import io.github.finalwave.view.gui.render.LawnGridOverlay;
import io.github.finalwave.view.gui.render.LawnHighlights;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.ScreenShake;
import io.github.finalwave.view.gui.render.clip.ExplosionLooks;
import io.github.finalwave.view.gui.render.clip.GraveClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.clip.ProjectileClips;
import io.github.finalwave.view.gui.render.clip.ZombossClips;
import io.github.finalwave.view.gui.render.clip.ZombotanyLooks;
import io.github.finalwave.view.gui.render.sync.ArcadeObstacleSync;
import io.github.finalwave.view.gui.render.sync.BowlingNutSync;
import io.github.finalwave.view.gui.render.sync.DeadLineSync;
import io.github.finalwave.view.gui.render.sync.PianoObstacleSync;
import io.github.finalwave.view.gui.render.sync.ProtectTileSync;
import io.github.finalwave.view.gui.render.sync.SandstormSync;
import io.github.finalwave.view.gui.render.sync.SlipperyTileSync;
import io.github.finalwave.view.gui.render.sync.SunSync;
import io.github.finalwave.view.gui.render.sync.VaseSync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class GamePlayScreen extends MenuScreen {
    private final LawnGridOverlay gridOverlay = new LawnGridOverlay();
    private final PauseModal pauseModal = new PauseModal();
    private final MatchResultModal resultModal = new MatchResultModal();
    private final ScreenShake screenShake = new ScreenShake();
    private final Map<String, Integer> lastMeowScores = new HashMap<>();

    private GamePlayController controller;
    private VaseBreakerController vaseBreaker;
    private WalnutBowlingController walnutBowling;
    private IZombieController iZombie;
    private NetworkedIZombieController networkedIZombie;
    private CouchIZombieController couchIZombie;
    private CouchPickOverlay couchPickOverlay;
    private CouchZombieKeys couchKeys;
    private LawnHighlights couchHighlights;
    private Label zombieSunLabel;
    private BeghouledController beghouled;
    private ZombotanyController zombotany;
    private MatchClock clock;
    private LawnLayout layout;
    private ChapterBackground chapterBackground;
    private BattlefieldGroup battlefield;
    private EntityAnimationCatalog catalog;
    private LawnInputController input;
    private SeedBankBar seedBank;
    private ZombieRosterBar zombieRoster;
    private ZombieSandboxPanel zombieSandbox;
    private PlantSandboxPanel plantSandbox;
    private BeghouledUpgradeBar upgradeBar;
    private ConveyorBeltBar conveyorBeltBar;
    private StartWaveButton startWaveButton;
    private DuelPickOverlay duelPickOverlay;
    private ReactionBar reactionBar;
    private Label duelClockLabel;
    private Table hudTop;
    private Table hudBottom;
    private SunCounter sunCounter;
    private final Vector2 sunHudTmp = new Vector2();
    private TimedWarPanel timedWarPanel;
    private LoveYourPlantsCounter loveYourPlantsCounter;
    private MeowPointBanner meowPointBanner;
    private final Set<String> preloadedPlantPams = new HashSet<>();
    private PlantFoodCounter plantFoodCounter;
    private WaveProgressMeter waveMeter;
    private ZombossHealthMeter zombossMeter;
    private Label beghouledRemaining;
    private SpeedButton speedButton;
    private AlertBanner alertBanner;
    private LevelObjectiveBanner objectiveBanner;
    private NpcDialogBox npcDialog;
    private IcebergFlashOverlay icebergFlashOverlay;
    private WidgetGroup cursorLayer;
    private boolean resultShown;
    private boolean bossOutroQueued;
    private float resultHold;

    public GamePlayScreen(PvzGame game) {
        super(game, new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
    }

    public void bind(GamePlayController controller) {
        this.controller = controller;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
        bindMatch(controller == null ? null : controller.getUser(), controller == null ? null : controller.session());
    }

    public void bind(NetworkedIZombieController networkedIZombie) {
        this.controller = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = networkedIZombie;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
        bindMatch(networkedIZombie == null ? null : networkedIZombie.getUser(),
                networkedIZombie == null ? null : networkedIZombie.session());
    }

    public void bind(CouchIZombieController couchIZombie) {
        this.controller = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = couchIZombie;
        this.beghouled = null;
        this.zombotany = null;
        bindMatch(couchIZombie == null ? null : couchIZombie.getUser(),
                couchIZombie == null ? null : couchIZombie.session());
    }

    public void bind(VaseBreakerController vaseBreaker) {
        this.controller = null;
        this.vaseBreaker = vaseBreaker;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.zombotany = null;
        bindMatch(vaseBreaker == null ? null : vaseBreaker.getUser(), vaseBreaker == null ? null : vaseBreaker.session());
    }

    public void bind(WalnutBowlingController walnutBowling) {
        this.controller = null;
        this.vaseBreaker = null;
        this.walnutBowling = walnutBowling;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
        bindMatch(walnutBowling == null ? null : walnutBowling.getUser(),
                walnutBowling == null ? null : walnutBowling.session());
    }

    public void bind(IZombieController iZombie) {
        this.controller = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = iZombie;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
        bindMatch(iZombie == null ? null : iZombie.getUser(), iZombie == null ? null : iZombie.session());
    }

    public void bind(BeghouledController beghouled) {
        this.controller = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = beghouled;
        this.zombotany = null;
        bindMatch(beghouled == null ? null : beghouled.getUser(), beghouled == null ? null : beghouled.session());
    }

    public void bind(ZombotanyController zombotany) {
        this.controller = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = zombotany;
        bindMatch(zombotany == null ? null : zombotany.getUser(), zombotany == null ? null : zombotany.session());
    }

    private void bindMatch(User user, GameSession session) {
        this.resultShown = false;
        this.bossOutroQueued = false;
        this.resultHold = 0f;
        this.clock = null;
        lastMeowScores.clear();
        screenShake.reset();
        preloadedPlantPams.clear();
        this.chapterBackground = new ChapterBackground(assets, ChapterId.ANCIENT_EGYPT);
        this.layout = lawnLayoutFor(5, 9);
        this.catalog = new EntityAnimationCatalog(assets.root());
        pauseModal.dismiss();
        resultModal.dismiss();
        if (objectiveBanner != null) {
            objectiveBanner.reset();
        }
        if (alertBanner != null) {
            alertBanner.reset();
        }
        if (npcDialog != null) {
            npcDialog.hide();
        }
        if (user == null && session == null) {
            return;
        }
        clock = new MatchClock(matchTicker(), user, networkedZombieRole());
        if (session != null && session.getBoard() != null) {
            GameBoard board = session.getBoard();
            chapterBackground = new ChapterBackground(assets, ChapterId.fromName(session.getChapterId()));
            layout = lawnLayoutFor(board.getRows(), board.getCols());
            preloadMatchAssets(session);
        }
        if (user != null) {
            bindCurrency(user);
        }
        if (sunCounter != null) {
            sunCounter.resetShown();
        }
    }

    private LawnLayout lawnLayoutFor(int rows, int cols) {
        chapterBackground.layoutFor(WORLD_WIDTH, WORLD_HEIGHT, cols);
        return chapterBackground.lawnLayout(rows, cols);
    }

    @Override
    protected void ensureMusic() {
        assets.audio().playBattle();
    }

    @Override
    protected void buildUi() {
        setBackground(null);
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        contentLayer.setTouchable(Touchable.enabled);
        battlefield = new BattlefieldGroup();
        battlefield.bind(assets, layout, catalog);
        contentLayer.addActor(battlefield);
        cursorLayer = new WidgetGroup();
        cursorLayer.setFillParent(true);
        cursorLayer.setTouchable(Touchable.disabled);
        stage.addActor(cursorLayer);
        input = new LawnInputController(
                lawnHost(),
                layout,
                battlefield,
                assets,
                catalog,
                cursorLayer,
                this::inputBlocked);
        battlefield.setSunCollector(sun -> input != null && input.collectSun(sun));
        battlefield.setBeghouledController(beghouled, this::inputBlocked);
        battlefield.setShakeListener(screenShake::trigger);
        buildHud();
        battlefield.setSunHudTarget(this::sunHudCenter);
        battlefield.setSunDeferred(amount -> {
            if (sunCounter != null) {
                sunCounter.hold(amount);
            }
        });
        battlefield.setSunArrived(amount -> {
            if (sunCounter != null) {
                sunCounter.release(amount);
            }
        });
        battlefield.setSunFlightsAborted(() -> {
            if (sunCounter != null) {
                sunCounter.clearHeld();
            }
        });
        icebergFlashOverlay = new IcebergFlashOverlay();
        icebergFlashOverlay.fitStage(WORLD_WIDTH, WORLD_HEIGHT);
        modalLayer.addActor(icebergFlashOverlay);
        startIntroDialog();
        finishNetworkedMatchBoot();
        finishCouchMatchBoot();
    }

    private void finishCouchMatchBoot() {
        if (couchIZombie == null) {
            return;
        }
        resultShown = false;
        if (resultModal != null) {
            resultModal.dismiss();
        }
        if (clock != null) {
            clock.setPaused(false);
            clock.setResultShowing(false);
        }
        couchIZombie.setPhaseChangeListener(this::rebuildCouchOverlays);
        couchKeys = new CouchZombieKeys(couchIZombie, this::inputBlocked);
        couchKeys.activate();
        if (couchHighlights != null) {
            couchHighlights.dispose();
        }
        couchHighlights = new LawnHighlights(battlefield.highlightLayer());
        rebuildCouchOverlays();
        buildHud();
        refreshHud();
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, couchKeys));
    }

    private void rebuildCouchOverlays() {
        if (couchPickOverlay != null) {
            couchPickOverlay.remove();
            couchPickOverlay = null;
        }
        if (couchKeys != null) {
            if (couchIZombie != null && couchIZombie.isPicking()) {
                couchPickOverlay = new CouchPickOverlay(assets, couchIZombie);
                couchKeys.setPickOverlay(couchPickOverlay);
                modalLayer.addActor(couchPickOverlay);
                couchPickOverlay.updateChrome();
                if (hudLayer != null) {
                    hudLayer.setTouchable(Touchable.disabled);
                }
                return;
            }
            couchKeys.setPickOverlay(null);
            couchKeys.resetCursor();
        }
        if (hudLayer != null) {
            hudLayer.setTouchable(Touchable.childrenOnly);
            buildHud();
        }
    }

    private void finishNetworkedMatchBoot() {
        if (networkedIZombie == null) {
            return;
        }
        resultShown = false;
        if (resultModal != null) {
            resultModal.dismiss();
        }
        if (clock != null) {
            clock.setPaused(false);
            clock.setResultShowing(false);
        }
        networkedIZombie.setReactionViewListener(payload ->
                ReactionToast.show(modalLayer, assets.skin(), payload));
        networkedIZombie.setPhaseChangeListener(ignored -> rebuildDuelOverlays());
        rebuildDuelOverlays();
        buildHud();
        refreshHud();
        if (networkedIZombie.role() == MatchRole.PLANT) {
            networkedIZombie.tickHostSync();
        } else {
            networkedIZombie.requestHostSync();
        }
    }

    private void rebuildDuelOverlays() {
        if (duelPickOverlay != null) {
            duelPickOverlay.remove();
            duelPickOverlay = null;
        }
        if (reactionBar != null) {
            if (reactionBar.getParent() != null) {
                reactionBar.getParent().remove();
            } else {
                reactionBar.remove();
            }
            reactionBar = null;
        }
        if (networkedIZombie == null) {
            return;
        }
        if (networkedIZombie.isPicking()) {
            duelPickOverlay = new DuelPickOverlay(assets, networkedIZombie);
            modalLayer.addActor(duelPickOverlay);
            duelPickOverlay.updateChrome();
            if (hudLayer != null) {
                hudLayer.setTouchable(Touchable.disabled);
            }
            return;
        }
        if (hudLayer != null) {
            hudLayer.setTouchable(Touchable.childrenOnly);
            buildHud();
        }
        reactionBar = new ReactionBar(assets.skin(), (kind, index) -> networkedIZombie.sendReaction(kind, index));
        Table holder = new Table();
        holder.setFillParent(true);
        holder.bottom().right().pad(12f);
        holder.add(reactionBar);
        modalLayer.addActor(holder);
    }

    public void dismissResult() {
        resultShown = false;
        if (clock != null) {
            clock.setResultShowing(false);
        }
        pauseModal.dismiss();
        resultModal.dismiss();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Batch batch = stage.getBatch();
        try {
            renderMatch(delta);
        } catch (RuntimeException e) {
            Gdx.app.error("GamePlayScreen", "Match render failed", e);
            if (batch != null && batch.isDrawing()) {
                batch.end();
            }
        }
    }

    private void renderMatch(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }
        float tickFraction = 0f;
        if (clock != null && battlefield != null) {
            clock.update(delta, battlefield);
            tickFraction = clock.tickFraction();
        }
        if (battlefield != null && battlefield.beghouledBusy() && !pauseModal.isShowing()) {
            battlefield.setPlantLayerPlaying(true);
        }
        if (battlefield != null && matchSession() != null) {
            battlefield.sync(matchSession(), tickFraction);
        }
        if (icebergFlashOverlay != null && matchSession() != null) {
            icebergFlashOverlay.sync(matchSession());
        }
        if (clock != null && battlefield != null) {
            float unitSpeed = networkedOnlineMatch() ? 1f : clock.speed();
            float environmentSpeed = unitSpeed;
            GameSession session = matchSession();
            if (session != null
                    && session.isDeadLineActive()
                    && session.getMatchResult() != MatchResult.IN_PROGRESS) {
                environmentSpeed = 1f;
            }
            battlefield.setPlaybackSpeed(unitSpeed, environmentSpeed);
        }
        if (input != null) {
            input.update();
        }
        updateCouchCursorHighlight();
        pollResult(delta);
        viewport.apply();
        screenShake.update(delta);
        if (battlefield != null) {
            battlefield.setPosition(screenShake.offsetX(), screenShake.offsetY());
        }
        Batch batch = stage.getBatch();
        batch.setColor(Color.WHITE);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (chapterBackground != null) {
            chapterBackground.draw(batch, viewport);
        }
        batch.end();
        batch.setColor(Color.WHITE);
        if (shouldDrawGrid()) {
            gridOverlay.draw(viewport, layout);
            Gdx.gl.glEnable(GL20.GL_BLEND);
        }
        stage.act(delta);
        float flyDelta = 0f;
        if (clock == null || !clock.shouldFreeze()) {
            float speed = networkedOnlineMatch() ? 1f : (clock == null ? 1f : clock.speed());
            flyDelta = delta * speed;
        }
        if (battlefield != null) {
            battlefield.tickSunFlights(flyDelta);
        }
        refreshHud();
        stage.draw();
        batch.setColor(Color.WHITE);
    }

    public void showAlert(String message) {
        if (alertBanner != null) {
            alertBanner.show(message);
            return;
        }
        if (message != null && !message.isBlank()) {
            toastMessage(message);
        }
    }

    public void showWaveAlert(int waveNumber, boolean finalWave) {
        if (alertBanner == null) {
            return;
        }
        if (finalWave) {
            alertBanner.show("A huge wave of zombies is approaching!");
        } else if (waveNumber > 1) {
            alertBanner.show("A huge wave of zombies is approaching!");
        }
        enqueueChapterAlerts();
    }

    public void playStartChant(Runnable onFinished) {
        if (alertBanner == null) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        alertBanner.showSequence(List.of("READY", "SET", "PLANT!"), onFinished);
    }

    public void showPauseModal() {
        openPause();
    }

    public void hidePauseModal() {
        resumeMatch();
    }

    public void showResult(MatchResult result) {
        if (resultShown || result == null || result == MatchResult.IN_PROGRESS) {
            return;
        }
        if (deadlineHoldRemaining(result) > 0f) {
            return;
        }
        if (npcDialog != null && npcDialog.isShowing()) {
            return;
        }
        if (!bossOutroQueued && matchSession() != null && matchSession().isBossActive()) {
            List<NpcDialogLine> lines = NpcDialogScript.forBossResult(result);
            bossOutroQueued = true;
            if (!lines.isEmpty() && npcDialog != null) {
                if (clock != null) {
                    clock.setPaused(true);
                }
                npcDialog.setOnFinished(() -> presentResult(result));
                npcDialog.show(lines);
                return;
            }
        }
        presentResult(result);
    }

    public void refreshHud() {
        if (controller == null && vaseBreaker == null && walnutBowling == null && iZombie == null
                && networkedIZombie == null && beghouled == null && zombotany == null && couchIZombie == null) {
            return;
        }
        User user = matchUser();
        GameSession session = matchSession();
        if (user != null) {
            bindCurrency(user);
        }
        if (sunCounter != null && session != null) {
            sunCounter.setAmount(displaySunBalance(session));
            sunCounter.setCounting(clock == null || !clock.shouldFreeze());
            float countSpeed = clock == null ? 1f : (networkedOnlineMatch() ? 1f : clock.speed());
            sunCounter.setCountSpeed(countSpeed);
        }
        if (timedWarPanel != null) {
            timedWarPanel.refresh(session);
        }
        if (loveYourPlantsCounter != null) {
            loveYourPlantsCounter.refresh(session);
        }
        pollMeowPoints();
        if (plantFoodCounter != null && session != null) {
            plantFoodCounter.setCount(session.getPlantFoodCount());
        }
        boolean vaseMode = vaseBreaker != null;
        boolean bowlingMode = walnutBowling != null;
        boolean couchMode = couchIZombie != null;
        boolean iZombieMode = iZombie != null || networkedZombieRole() || couchMode;
        boolean hideAdventureHud = vaseMode || bowlingMode;
        boolean hideSeeds = hideSeedTools();
        if (sunCounter != null) {
            boolean conveyor = session != null && session.isConveyorBeltActive();
            sunCounter.setVisible(!hideAdventureHud && !conveyor);
        }
        if (zombieSunLabel != null) {
            boolean showZombieSun = couchMode && session != null;
            zombieSunLabel.setVisible(showZombieSun);
            if (showZombieSun) {
                zombieSunLabel.setText("Zombie sun: " + session.getIZombieSunBalance());
            }
        }
        if (plantFoodCounter != null) {
            plantFoodCounter.setVisible(!hideSeeds);
        }
        if (waveMeter != null) {
            boolean boss = session != null && session.isBossActive();
            waveMeter.setVisible(!sandboxMatch() && !vaseMode && !iZombieMode && !boss);
        }
        if (zombossMeter != null) {
            boolean boss = session != null && session.isBossActive();
            zombossMeter.setVisible(boss);
            if (boss) {
                zombossMeter.refresh(session);
            }
        }
        if (beghouledRemaining != null) {
            boolean showMatches = beghouled != null && session != null && session.isBeghouledActive()
                    && session.getBeghouledBoard() != null;
            beghouledRemaining.setVisible(showMatches);
            if (showMatches) {
                int left = Math.max(0, session.getBeghouledMatchTarget() - session.getBeghouledBoard().getMatchesMade());
                beghouledRemaining.setText(left + " matches left");
            }
        }
        boolean sandbox = sandboxMatch();
        boolean couchSeedBank = couchMode && session != null;
        if (seedBank != null && session != null) {
            if ((hideSeeds || sandbox) && !couchSeedBank) {
                seedBank.setVisible(false);
            } else {
                seedBank.setVisible(true);
                seedBank.refresh(session, user, seedBoosts(),
                        input == null ? null : input.mode());
            }
        }
        if (plantSandbox != null) {
            if (sandbox) {
                plantSandbox.refresh(session, input == null ? null : input.mode());
            } else {
                plantSandbox.setVisible(false);
            }
        }
        if (zombieRoster != null) {
            if (iZombieMode) {
                if (couchKeys != null) {
                    zombieRoster.keyboardSelect(couchKeys.selectedAlias());
                }
                zombieRoster.refresh(session, input == null ? null : input.mode());
            } else {
                zombieRoster.setVisible(false);
            }
        }
        if (duelClockLabel != null) {
            if (couchIZombie != null) {
                duelClockLabel.setVisible(true);
                if (couchIZombie.isPicking()) {
                    duelClockLabel.setText("Picking "
                            + Math.max(couchIZombie.plantPicks().pickSecondsLeft(),
                            couchIZombie.zombiePicks().pickSecondsLeft()) + "s");
                    if (couchPickOverlay != null) {
                        couchPickOverlay.updateChrome();
                    }
                    if (hudLayer != null) {
                        hudLayer.setTouchable(Touchable.disabled);
                    }
                } else {
                    if (couchIZombie.session().getActiveMiniGameHandler()
                            instanceof io.github.finalwave.model.minigame.izombie.NetworkedIZombieHandler handler) {
                        duelClockLabel.setText("Time " + handler.secondsLeft() + "s");
                    }
                    if (hudLayer != null && hudLayer.getTouchable() == Touchable.disabled) {
                        hudLayer.setTouchable(Touchable.childrenOnly);
                    }
                }
            } else if (networkedIZombie != null) {
                duelClockLabel.setVisible(true);
                if (networkedIZombie.isPicking()) {
                    duelClockLabel.setText("Picking " + networkedIZombie.pickSecondsLeft() + "s");
                    if (duelPickOverlay != null) {
                        duelPickOverlay.updateChrome();
                    }
                    if (hudLayer != null) {
                        hudLayer.setTouchable(Touchable.disabled);
                    }
                } else {
                    duelClockLabel.setText("Time " + networkedIZombie.secondsLeft() + "s");
                    if (hudLayer != null && hudLayer.getTouchable() == Touchable.disabled) {
                        hudLayer.setTouchable(Touchable.childrenOnly);
                    }
                }
            } else {
                duelClockLabel.setVisible(false);
            }
        }
        layoutSandbox();
        if (upgradeBar != null) {
            upgradeBar.refresh(session);
        }
        if (bowlingMode && session != null && input != null
                && input.mode() instanceof ToolMode.Seed seed
                && !session.getConveyorBeltPlants().contains(seed.plantName())) {
            input.setMode(new ToolMode.None());
        }
        if (vaseMode && session != null) {
            if (input != null && input.mode() instanceof ToolMode.Seed seed
                    && !hasGroundPacket(session, seed.plantName())) {
                input.setMode(new ToolMode.None());
            }
            for (GroundSeedPacket packet : session.getGroundSeedPackets()) {
                if (packet != null) {
                    preloadPlantPam(packet.plantName());
                }
            }
        }
        if (conveyorBeltBar != null) {
            boolean freeze = clock != null && clock.shouldFreeze();
            conveyorBeltBar.refresh(session, user, input == null ? null : input.mode(), freeze);
            if (session != null && session.isConveyorBeltActive()) {
                for (String plantName : session.getConveyorBeltPlants()) {
                    preloadPlantPam(plantName);
                }
            }
        }
        if (startWaveButton != null) {
            startWaveButton.refresh(session);
        }
        packTopHudCells();
        if (hudBottom != null && plantFoodCounter != null) {
            Cell<?> foodCell = hudBottom.getCell(plantFoodCounter);
            if (foodCell != null) {
                foodCell.padLeft(plantFoodPad());
                hudBottom.invalidate();
            }
        }
        if (waveMeter != null) {
            waveMeter.refresh(session);
        }
        if (zombossMeter != null && session != null && session.isBossActive()) {
            zombossMeter.refresh(session);
        }
        if (speedButton != null && clock != null && !networkedOnlineMatch()) {
            speedButton.setSpeed(clock.speed());
        }
        if (speedButton != null) {
            speedButton.actor().setVisible(!networkedOnlineMatch());
        }
    }

    private void pollMeowPoints() {
        if (meowPointBanner == null) {
            return;
        }
        boolean scoreGame = controller instanceof ScoreGamePlayController;
        if (!scoreGame) {
            meowPointBanner.refresh(false, 0);
            return;
        }
        ScoreGamePlayController score = (ScoreGamePlayController) controller;
        MeowPointBreakdown breakdown = score.meowPointTracker().getBreakdown();
        meowPointBanner.refresh(true, breakdown.total());
        for (Map.Entry<String, Integer> entry : breakdown.patternScores().entrySet()) {
            int previous = lastMeowScores.getOrDefault(entry.getKey(), 0);
            int current = entry.getValue() == null ? 0 : entry.getValue();
            if (current > previous) {
                int gained = current - previous;
                showAlert(meowLabel(entry.getKey()) + " +" + gained);
                toastMessage(meowLabel(entry.getKey()) + " +" + gained);
            }
            lastMeowScores.put(entry.getKey(), current);
        }
    }

    private static String meowLabel(String id) {
        if (id == null) {
            return "MeowPoint";
        }
        return switch (id) {
            case "pierce-multi-kill" -> "Pierce";
            case "speed-kill" -> "Speed Kill";
            case "simultaneous-kill" -> "Multi-Kill";
            case "mower-sweep" -> "Mower Sweep";
            case "efficient-victory" -> "Perfect Victory";
            default -> id;
        };
    }

    public MatchClock clock() {
        return clock;
    }

    public LawnLayout layout() {
        return layout;
    }

    public BattlefieldGroup battlefield() {
        return battlefield;
    }

    @Override
    public void hide() {
        if (input != null) {
            input.dispose();
            input = null;
        }
        if (cursorLayer != null) {
            cursorLayer.remove();
            cursorLayer = null;
        }
        couchKeys = null;
        couchPickOverlay = null;
        if (couchHighlights != null) {
            couchHighlights.dispose();
            couchHighlights = null;
        }
        pauseModal.dismiss();
        resultModal.dismiss();
        if (objectiveBanner != null) {
            objectiveBanner.dismiss();
        }
        if (alertBanner != null) {
            alertBanner.reset();
        }
        if (battlefield != null) {
            battlefield.clearBattlefield();
        }
        if (icebergFlashOverlay != null) {
            icebergFlashOverlay.remove();
            icebergFlashOverlay.dispose();
            icebergFlashOverlay = null;
        }
        super.hide();
        battlefield = null;
    }

    @Override
    public void dispose() {
        if (input != null) {
            input.dispose();
            input = null;
        }
        if (couchHighlights != null) {
            couchHighlights.dispose();
            couchHighlights = null;
        }
        if (cursorLayer != null) {
            cursorLayer.remove();
            cursorLayer = null;
        }
        gridOverlay.dispose();
        super.dispose();
    }

    private void buildHud() {
        hudLayer.clearChildren();
        hudLayer.setTouchable(Touchable.childrenOnly);
        hudLayer.top().left();
        hudLayer.pad(0f);

        sunCounter = new SunCounter(assets, this::onAddSun);
        timedWarPanel = new TimedWarPanel(assets);
        loveYourPlantsCounter = new LoveYourPlantsCounter(assets);
        meowPointBanner = new MeowPointBanner(assets);
        plantFoodCounter = new PlantFoodCounter(assets, this::onAddPlantFood, this::onPlantFoodDragStart, this::onPlantFoodDrop);
        seedBank = new SeedBankBar(assets, this::onSeed);
        plantSandbox = new PlantSandboxPanel(assets, this::onSeed);
        plantSandbox.setVisible(false);
        zombieRoster = new ZombieRosterBar(assets, this::onZombie);
        zombieRoster.setInputEnabled(couchIZombie == null);
        upgradeBar = new BeghouledUpgradeBar(assets, this::onBeghouledUpgrade);
        conveyorBeltBar = new ConveyorBeltBar(assets, this::onSeed);
        startWaveButton = new StartWaveButton(assets, this::onStartWaves);
        waveMeter = new WaveProgressMeter(assets);
        zombossMeter = new ZombossHealthMeter(assets);
        speedButton = new SpeedButton(assets, this::onSpeed);
        Actor pause = PauseButton.create(assets, this::togglePause);
        boolean hideShovel = hideAdventureTools() || beghouled != null || iZombie != null
                || networkedZombieRole() || couchIZombie != null;
        Actor shovel = hideShovel ? null : ShovelButton.create(assets, this::onShovel);

        hudTop = new Table();
        if (!hideAdventureTools()) {
            hudTop.add(sunCounter).padLeft(sunPad()).padTop(10f);
        }
        duelClockLabel = new Label("", assets.skin(), "medium");
        duelClockLabel.setVisible(false);
        hudTop.add(duelClockLabel).padLeft(12f).padTop(12f);
        hudTop.add(startWaveButton).padLeft(8f).padTop(10f);
        hudTop.add(timedWarPanel).padLeft(8f).padTop(10f);
        hudTop.add(loveYourPlantsCounter).padLeft(8f).padTop(10f);
        hudTop.add(meowPointBanner).padLeft(8f).padTop(10f);
        hudTop.add().expandX();
        Table utilities = new Table();
        if (!networkedOnlineMatch()) {
            utilities.add(speedButton.actor()).size(72f).padRight(2f);
        }
        zombieSandbox = new ZombieSandboxPanel(assets, sandboxHost());
        zombieSandbox.setVisible(false);
        utilities.add(pause).size(72f).padRight(4f);
        utilities.add(currencyBar);
        hudTop.add(utilities).padTop(6f).padRight(8f);

        Table meterOverlay = new Table();
        meterOverlay.setTouchable(Touchable.childrenOnly);
        meterOverlay.add().expandX();
        meterOverlay.add(meterBlock()).padTop(8f).top();
        meterOverlay.add().expandX();

        Stack topStack = new Stack();
        topStack.add(hudTop);
        topStack.add(meterOverlay);
        hudLayer.add(topStack).growX().row();

        Table mid = new Table();
        mid.setTouchable(Touchable.childrenOnly);
        if (iZombie != null || networkedZombieRole() || couchIZombie != null) {
            mid.add(zombieRoster).left().top().padLeft(8f).padTop(6f);
        }
        if (couchIZombie != null) {
            zombieSunLabel = new Label("", assets.skin(), "secondary");
            mid.add(zombieSunLabel).left().top().padLeft(14f).padTop(10f);
        }
        if ((!hideSeedTools() && !sandboxMatch()) || networkedPlantRole() || couchIZombie != null) {
            mid.add(seedBank).left().top().padLeft(8f).padTop(6f);
        }
        mid.add().expand();
        hudLayer.add(mid).grow().row();

        hudBottom = new Table();
        if (beghouled != null) {
            hudBottom.add(upgradeBar).left().padLeft(16f).padBottom(12f);
        }
        if (!hideSeedTools()) {
            hudBottom.add(plantFoodCounter).left().padLeft(plantFoodPad()).padBottom(18f);
        }
        hudBottom.add().expandX();
        if (shovel != null) {
            hudBottom.add(shovel).size(84f).padRight(20f).padBottom(18f);
        }
        hudLayer.add(hudBottom).growX();
        hudLayer.addActor(conveyorBeltBar);
        if (plantSandbox != null) {
            hudLayer.addActor(plantSandbox);
        }
        if (zombieSandbox != null) {
            hudLayer.addActor(zombieSandbox);
        }

        if (alertBanner != null) {
            alertBanner.remove();
        }
        if (objectiveBanner != null) {
            objectiveBanner.dismiss();
        }
        if (npcDialog != null) {
            npcDialog.remove();
        }
        alertBanner = new AlertBanner(assets.skin());
        if (objectiveBanner == null) {
            objectiveBanner = new LevelObjectiveBanner();
        }
        npcDialog = new NpcDialogBox(assets);
        stage.addActor(alertBanner);
        stage.addActor(npcDialog);
    }

    private Table meterBlock() {
        Table block = new Table();
        Stack meters = new Stack();
        meters.add(waveMeter);
        meters.add(zombossMeter);
        zombossMeter.setVisible(false);
        block.add(meters).size(420f, 48f).row();
        beghouledRemaining = new Label("", assets.skin(), "medium");
        beghouledRemaining.setAlignment(Align.center);
        beghouledRemaining.setFontScale(0.7f);
        beghouledRemaining.setVisible(false);
        block.add(beghouledRemaining).padTop(2f).row();
        Label title = new Label(levelCaption(), assets.skin(), "medium");
        title.setAlignment(Align.center);
        title.setFontScale(0.62f);
        block.add(title).padTop(2f);
        return block;
    }

    private void startIntroDialog() {
        if (sandboxMatch() || npcDialog == null || controller == null || vaseBreaker != null
                || walnutBowling != null || iZombie != null || networkedIZombie != null
                || beghouled != null || zombotany != null || couchIZombie != null) {
            showObjectiveIfNeeded();
            return;
        }
        List<NpcDialogLine> script = NpcDialogScript.forLevel(controller.chapter(), controller.level());
        if (script.isEmpty()) {
            showObjectiveIfNeeded();
            return;
        }
        assets.pamPlayer().loadAsync(LawnAssetIds.CRAZY_DAVE_PAM, () -> {
        });
        assets.pamPlayer().loadAsync(LawnAssetIds.PENNY_PAM, () -> {
        });
        if (clock != null) {
            clock.setPaused(true);
        }
        npcDialog.setOnFinished(this::showObjectiveIfNeeded);
        npcDialog.show(script);
    }

    private void showObjectiveIfNeeded() {
        if (sandboxMatch() || objectiveBanner == null || controller == null || vaseBreaker != null
                || walnutBowling != null || iZombie != null || networkedIZombie != null
                || beghouled != null || zombotany != null || couchIZombie != null) {
            resumeAfterIntro();
            return;
        }
        if (clock != null) {
            clock.setPaused(true);
        }
        objectiveBanner.show(
                stage,
                viewport,
                assets,
                controller.level(),
                this::afterObjectives);
    }

    private void afterObjectives() {
        if (shouldPlayStartChant()) {
            if (clock != null) {
                clock.setPaused(true);
            }
            playStartChant(this::resumeAfterIntro);
            return;
        }
        resumeAfterIntro();
    }

    private boolean shouldPlayStartChant() {
        if (sandboxMatch() || controller == null || vaseBreaker != null || walnutBowling != null
                || iZombie != null || beghouled != null || zombotany != null) {
            return false;
        }
        GameSession session = controller.session();
        return session == null || !session.isPrepPhaseActive();
    }

    private void enqueueChapterAlerts() {
        if (alertBanner == null || controller == null || controller.chapter() == null) {
            return;
        }
        ChapterConfig chapter = controller.chapter();
        ChapterRules rules = chapter.getRules();
        if (rules.hasNecromancyTiles() && rules.hasGravesOnWaveStart()) {
            alertBanner.show("The graves stir with necromancy!");
        }
        if (rules.hasLowBeachEmerge()) {
            alertBanner.show("Zombies emerge from the low beach!");
        }
    }

    private void resumeAfterIntro() {
        if (clock != null && !resultShown && !pauseModal.isShowing()
                && (npcDialog == null || !npcDialog.isShowing())
                && (objectiveBanner == null || !objectiveBanner.isShowing())) {
            clock.setPaused(false);
        }
    }

    private String levelCaption() {
        if (vaseBreaker != null && vaseBreaker.getStage() != null) {
            MiniGameStageConfig stage = vaseBreaker.getStage();
            return "Vasebreaker - Stage " + stage.getStageIndex();
        }
        if (walnutBowling != null && walnutBowling.getStage() != null) {
            MiniGameStageConfig stage = walnutBowling.getStage();
            return "Wallnut Bowling - Stage " + stage.getStageIndex();
        }
        if (iZombie != null && iZombie.getStage() != null) {
            MiniGameStageConfig stage = iZombie.getStage();
            return "I, Zombie - Stage " + stage.getStageIndex();
        }
        if (networkedIZombie != null && networkedIZombie.getStage() != null) {
            MiniGameStageConfig stage = networkedIZombie.getStage();
            String role = networkedIZombie.role() == MatchRole.PLANT ? "Plants" : "Zombies";
            String opponent = networkedIZombie.opponentUsername();
            String versus = opponent == null || opponent.isBlank() ? "" : " vs " + opponent;
            return "I, Zombie Online (" + role + ")" + versus;
        }
        if (couchIZombie != null) {
            return "I, Zombie - Couch Play";
        }
        if (beghouled != null && beghouled.getStage() != null) {
            MiniGameStageConfig stage = beghouled.getStage();
            return "Beghouled - Stage " + stage.getStageIndex();
        }
        if (zombotany != null && zombotany.getStage() != null) {
            MiniGameStageConfig stage = zombotany.getStage();
            return "Zombotany - Stage " + stage.getStageIndex();
        }
        if (controller == null || controller.chapter() == null || controller.level() == null) {
            return "";
        }
        return controller.chapter().getDisplayName() + " - Day " + controller.level().getIndex();
    }

    private void onSeed(String plantName) {
        GameSession session = matchSession();
        if (session != null) {
            session.noteImitaterTargetSeed(plantName);
        }
        if (input != null) {
            input.toggleSeed(plantName);
        }
    }

    private void onZombie(String alias) {
        if (couchIZombie != null) {
            if (couchKeys != null) {
                couchKeys.selectAlias(alias);
            }
            return;
        }
        if (input != null) {
            input.toggleZombie(alias);
        }
    }

    private void onBeghouledUpgrade(String plantName) {
        if (beghouled == null || inputBlocked()) {
            return;
        }
        beghouled.upgradePlant(plantName);
    }

    private void onShovel() {
        if (input != null) {
            input.toggleShovel();
        }
    }

    private Vector2 sunHudCenter() {
        if (sunCounter == null || !sunCounter.hasParent() || !sunCounter.isVisible()) {
            return null;
        }
        return sunCounter.iconCenterStage(sunHudTmp);
    }

    private void onAddSun() {
        User user = matchUser();
        if (user == null || !user.isDebugMode()) {
            return;
        }
        if (controller != null) {
            controller.cheatAddSun(50);
            return;
        }
        if (iZombie != null) {
            iZombie.cheatAddSun(50);
            return;
        }
        if (networkedIZombie != null) {
            networkedIZombie.cheatAddSun(50);
        }
        if (couchIZombie != null) {
            couchIZombie.session().addSunBalance(50);
        }
        if (zombotany != null) {
            zombotany.cheatAddSun(50);
        }
    }

    private void onAddPlantFood() {
        User user = matchUser();
        if (user == null || !user.isDebugMode()) {
            return;
        }
        if (zombotany != null && zombotany.session() != null) {
            if (zombotany.session().getPlantFoodCount() >= PlantFoodCounter.SLOT_COUNT) {
                return;
            }
            zombotany.cheatAddPlantFood();
            return;
        }
        if (controller == null || controller.session() == null) {
            return;
        }
        if (controller.session().getPlantFoodCount() >= PlantFoodCounter.SLOT_COUNT) {
            return;
        }
        controller.cheatAddPlantFood();
    }

    private void onPlantFoodDragStart() {
        if (input != null) {
            input.beginPlantFoodDrag();
        }
    }

    private void onPlantFoodDrop(float stageX, float stageY) {
        if (input != null) {
            input.dropPlantFoodAtStage(stageX, stageY);
        }
    }

    private void onStartWaves() {
        if (controller == null || inputBlocked()) {
            return;
        }
        GameSession session = controller.session();
        if (session == null || !session.isPrepPhaseActive()) {
            return;
        }
        controller.startWaves();
    }

    private void onSpeed() {
        if (networkedOnlineMatch()) {
            return;
        }
        if (clock != null) {
            clock.cycleSpeed();
        }
    }

    private void togglePause() {
        if (networkedOnlineMatch()) {
            if (pauseModal.isShowing()) {
                resumeMatch();
                return;
            }
            openPause();
            return;
        }
        if (resultShown || resultModal.isShowing() || matchFinished()
                || (npcDialog != null && npcDialog.isShowing())
                || (objectiveBanner != null && objectiveBanner.isShowing())) {
            return;
        }
        if (pauseModal.isShowing()) {
            resumeMatch();
            return;
        }
        openPause();
    }

    private void openPause() {
        if (networkedOnlineMatch()) {
            if (input != null) {
                input.setMode(new ToolMode.None());
            }
            pauseModal.showOnlineExit(modalLayer, viewport, assets, this::exitMatch);
            return;
        }
        if (clock != null) {
            clock.setPaused(true);
        }
        if (input != null) {
            input.setMode(new ToolMode.None());
        }
        pauseModal.show(modalLayer, viewport, assets, this::resumeMatch, this::restartMatch, this::saveAndExit);
    }

    private void resumeMatch() {
        pauseModal.dismiss();
        if (networkedOnlineMatch()) {
            return;
        }
        if (clock != null && !resultShown && (npcDialog == null || !npcDialog.isShowing())
                && (objectiveBanner == null || !objectiveBanner.isShowing())) {
            clock.setPaused(false);
        }
    }

    private void exitMatch() {
        if (vaseBreaker != null) {
            vaseBreaker.confirmMatchExit();
            return;
        }
        if (walnutBowling != null) {
            walnutBowling.confirmMatchExit();
            return;
        }
        if (iZombie != null) {
            iZombie.confirmMatchExit();
            return;
        }
        if (networkedIZombie != null) {
            networkedIZombie.confirmMatchExit();
            return;
        }
        if (couchIZombie != null) {
            couchIZombie.confirmMatchExit();
            return;
        }
        if (beghouled != null) {
            beghouled.confirmMatchExit();
            return;
        }
        if (zombotany != null) {
            zombotany.confirmMatchExit();
            return;
        }
        if (controller != null) {
            controller.confirmMatchExit();
        }
    }

    private void saveAndExit() {
        if (controller != null) {
            controller.saveAndExit();
            return;
        }
        exitMatch();
    }

    private void restartMatch() {
        if (vaseBreaker != null) {
            vaseBreaker.restartMatch();
            return;
        }
        if (walnutBowling != null) {
            walnutBowling.restartMatch();
            return;
        }
        if (iZombie != null) {
            iZombie.restartMatch();
            return;
        }
        if (couchIZombie != null) {
            couchIZombie.restartMatch();
            return;
        }
        if (beghouled != null) {
            beghouled.restartMatch();
            return;
        }
        if (zombotany != null) {
            zombotany.restartMatch();
            return;
        }
        if (controller != null) {
            controller.restartMatch();
        }
    }

    private void pollResult(float delta) {
        if (matchSession() == null) {
            return;
        }
        GameSession session = matchSession();
        MatchResult result = session.getMatchResult();
        if (result == MatchResult.IN_PROGRESS) {
            resultHold = 0f;
            return;
        }
        resultHold += Math.max(0f, delta);
        if (deadlineHoldRemaining(result) > 0f) {
            return;
        }
        showResult(result);
    }

    private float deadlineHoldRemaining(MatchResult result) {
        if (matchSession() == null || !matchSession().isDeadLineActive()) {
            return 0f;
        }
        return Math.max(0f, DeadLineSync.resultHoldSeconds(result) - resultHold);
    }

    private void presentResult(MatchResult result) {
        if (clock != null) {
            clock.setResultShowing(true);
        }
        resultShown = true;
        pauseModal.dismiss();
        if (couchIZombie != null) {
            resultModal.show(
                    modalLayer,
                    viewport,
                    assets.skin(),
                    result == MatchResult.WON ? "Victory" : "Defeat",
                    result == MatchResult.WON
                            ? "Player 1 (Plants) wins!"
                            : "Player 2 (Zombies) wins!",
                    this::exitMatch,
                    this::restartMatch);
            return;
        }
        resultModal.show(
                modalLayer,
                viewport,
                assets.skin(),
                result,
                this::exitMatch,
                this::restartMatch);
    }

    private void updateCouchCursorHighlight() {
        if (couchHighlights == null) {
            return;
        }
        boolean showCursor = couchIZombie != null
                && couchKeys != null
                && couchIZombie.isPlaying()
                && !inputBlocked()
                && couchKeys.cursorCol() >= 0;
        if (showCursor) {
            couchHighlights.setTint(COUCH_CURSOR_TINT);
            couchHighlights.show(layout, couchKeys.cursorCol(), couchKeys.cursorRow());
        } else {
            couchHighlights.hide();
        }
    }

    private static final Color COUCH_CURSOR_TINT = Color.valueOf("7FD4FF80");

    private boolean inputBlocked() {
        return resultShown
                || pauseModal.isShowing()
                || (npcDialog != null && npcDialog.isShowing())
                || (objectiveBanner != null && objectiveBanner.isShowing())
                || (!networkedOnlineMatch() && clock != null && clock.isPaused())
                || matchFinished()
                || (battlefield != null && battlefield.beghouledBusy())
                || (networkedIZombie != null && networkedIZombie.isPicking())
                || (couchIZombie != null && couchIZombie.isPicking());
    }

    private boolean networkedOnlineMatch() {
        return networkedIZombie != null;
    }

    private boolean matchFinished() {
        return matchSession() != null
                && matchSession().getMatchResult() != MatchResult.IN_PROGRESS;
    }

    private boolean shouldDrawGrid() {
        return layout != null
                && matchUser() != null
                && matchUser().isShowLawnGrid();
    }

    private void packTopHudCells() {
        if (hudTop == null) {
            return;
        }
        packHudCell(hudTop.getCell(sunCounter), sunCounter, sunPad(), 10f);
        packHudCell(hudTop.getCell(startWaveButton), startWaveButton, 8f, 10f);
        packHudCell(hudTop.getCell(timedWarPanel), timedWarPanel, 8f, 10f);
        packHudCell(hudTop.getCell(loveYourPlantsCounter), loveYourPlantsCounter, 8f, 10f);
        packHudCell(hudTop.getCell(meowPointBanner), meowPointBanner, 8f, 10f);
        hudTop.invalidate();
        hudTop.invalidateHierarchy();
    }

    private static void packHudCell(Cell<?> cell, Actor actor, float padLeft, float padTop) {
        if (cell == null || actor == null) {
            return;
        }
        if (actor.isVisible()) {
            cell.width(Value.prefWidth);
            cell.height(Value.prefHeight);
            cell.padLeft(padLeft).padTop(padTop).padRight(0f).padBottom(0f);
            return;
        }
        cell.width(0f);
        cell.height(0f);
        cell.pad(0f);
    }

    private float sunPad() {
        if (sandboxMatch()) {
            return PlantSandboxPanel.PANEL_WIDTH + 20f;
        }
        if (conveyorBeltBar != null && conveyorBeltBar.isVisible()) {
            return conveyorBeltBar.stripWidth() + 16f;
        }
        return 16f;
    }

    private float plantFoodPad() {
        if (sandboxMatch()) {
            return PlantSandboxPanel.PANEL_WIDTH + 20f;
        }
        if (conveyorBeltBar != null && conveyorBeltBar.isVisible()) {
            return conveyorBeltBar.stripWidth() + 16f;
        }
        return 128f;
    }

    private void preloadPlantPam(String plantName) {
        if (plantName == null || plantName.isBlank() || catalog == null || !preloadedPlantPams.add(plantName)) {
            return;
        }
        preload(catalog.plantIdle(plantName).path());
    }

    private void preloadMatchAssets(GameSession session) {
        if (catalog == null) {
            catalog = new EntityAnimationCatalog(assets.root());
        }
        preload(ExplosionLooks.CHERRY_PATH);
        preload(ExplosionLooks.CHERRY_REAR_PATH);
        preload(ExplosionLooks.MINE_PATH);
        assets.region(ExplosionLooks.SCORCH_IMAGE);
        if (session.isConveyorBeltActive()) {
            assets.region(LawnAssetIds.CONVEYOR_BELT);
            assets.region(LawnAssetIds.CONVEYOR_SIDE);
        }
        if (session.isBossActive()) {
            assets.region(LawnAssetIds.ZOMBOSS_METER);
            assets.region(LawnAssetIds.ZOMBOSS_FILL);
            assets.region(LawnAssetIds.ZOMBOSS_HEAD);
            assets.region(LawnAssetIds.ZOMBOSS_NOTCH);
            assets.region(LawnAssetIds.ZOMBOSS_SKULL);
            preload(catalog.zombiePath("ZombieEgyptZomboss"));
            preload(catalog.zombiePath("ZombieDarkZomboss"));
            preload(catalog.zombiePath("ZombieIceageZomboss"));
            preload(catalog.zombiePath("ZombieBeachZomboss"));
            preload(catalog.zombiePath("ZombieDarkImpDragon"));
            preload(ZombossClips.EGYPT_MISSILE);
            preload(ZombossClips.ICE_MISSILE);
            preload(ZombossClips.DARK_FIREBALL);
            preload(ZombossClips.SHARK);
            preload(ZombossClips.TURBINE);
            preload(ZombossClips.FIRE_TILE);
            preload(ZombossClips.GLACIER);
            preload(ZombossClips.CHILL_WIND);
            preload(ZombossClips.ICE_BLOCK_ZOMBIE);
            preload(ZombossClips.ICE_BLOCK_ZOMBIE_SPAWN);
        }
        if (!session.getProtectedSeedPlacements().isEmpty()) {
            assets.region(LawnAssetIds.PROTECT_TILE);
            preload(ProtectTileSync.PAM_PATH);
        }
        if (session.isPrepPhaseActive()) {
            assets.region(LawnAssetIds.PURPLE_BUTTON);
            assets.region(LawnAssetIds.PURPLE_BUTTON_DOWN);
        }
        if (controller != null
                && !NpcDialogScript.forLevel(controller.chapter(), controller.level()).isEmpty()) {
            assets.region(LawnAssetIds.SPEECH_BUBBLE);
            assets.pamPlayer().loadSync(LawnAssetIds.CRAZY_DAVE_PAM);
            assets.pamPlayer().loadSync(LawnAssetIds.PENNY_PAM);
        }
        if (session.isDeadLineActive()) {
            assets.pamPlayer().loadSync(DeadLineSync.PAM_PATH);
        }
        Set<String> loadout = session.getSelectedLoadout();
        if (loadout != null) {
            for (String plantName : loadout) {
                preloadPlantPam(plantName);
            }
        }
        for (String plantName : session.getConveyorBeltPlants()) {
            preloadPlantPam(plantName);
        }
        for (SeedPlacement placement : session.getProtectedSeedPlacements()) {
            preloadPlantPam(placement.getPlantName());
        }
        if (session.isConveyorBeltActive() && matchUser() != null) {
            for (String plantName : CollectionService.selectablePlantNames(
                    matchUser(), session.getPlantRegistry())) {
                preloadPlantPam(plantName);
            }
        }
        if (session.isWalnutBowlingActive()) {
            preload(BowlingNutSync.WALLNUT_PAM);
            preload(BowlingNutSync.EXPLODEONUT_PAM);
            preload(BowlingNutSync.TALLNUT_PAM);
        }
        if (!session.getVases().isEmpty()) {
            preload(VaseSync.BROWN_PAM);
            preload(VaseSync.GREEN_PAM);
            preload(VaseSync.GARGANTUAR_PAM);
            for (String smashImage : VaseSync.SMASH_IMAGES) {
                assets.region(smashImage);
            }
        }
        if (vaseBreaker != null && vaseBreaker.getStage() != null) {
            MiniGameStageConfig stage = vaseBreaker.getStage();
            if (stage.getPlantSeedPool() != null) {
                for (String plantName : stage.getPlantSeedPool()) {
                    preloadPlantPam(plantName);
                }
            }
            if (stage.getZombiePool() != null) {
                for (String alias : stage.getZombiePool()) {
                    preload(catalog.zombiePath(alias));
                }
            }
            preload(catalog.zombiePath("ZombieGargantuar"));
        }
        if (iZombie != null && iZombie.getStage() != null) {
            MiniGameStageConfig stage = iZombie.getStage();
            if (stage.getPlantSeedPool() != null) {
                for (String plantName : stage.getPlantSeedPool()) {
                    preloadPlantPam(plantName);
                }
            }
            if (stage.getZombiePool() != null) {
                for (String alias : stage.getZombiePool()) {
                    String path = catalog.zombiePath(alias);
                    preloadSync(path);
                }
            }
            preloadSync(catalog.zombiePath(IZombieHandler.SUN_PRODUCER_ALIAS));
            assets.region(LawnAssetIds.BRAIN);
            assets.region(LawnAssetIds.SUN_ICON);
        }
        if (beghouled != null && beghouled.getStage() != null) {
            MiniGameStageConfig stage = beghouled.getStage();
            if (stage.getPlantSeedPool() != null) {
                for (String plantName : stage.getPlantSeedPool()) {
                    preloadPlantPam(plantName);
                }
            }
            if (stage.getUpgrades() != null) {
                for (BeghouledUpgradeRule rule : stage.getUpgrades()) {
                    preloadPlantPam(rule.fromPlant());
                    preloadPlantPam(rule.toPlant());
                }
            }
            if (stage.getZombiePool() != null) {
                for (String alias : stage.getZombiePool()) {
                    preload(catalog.zombiePath(alias));
                }
            }
        }
        if (zombotany != null && zombotany.getStage() != null) {
            MiniGameStageConfig stage = zombotany.getStage();
            if (stage.getPlantSeedPool() != null) {
                for (String plantName : stage.getPlantSeedPool()) {
                    preloadPlantPam(plantName);
                }
            }
            if (stage.getZombiePool() != null) {
                for (String alias : stage.getZombiePool()) {
                    preload(catalog.zombiePath(alias));
                }
            }
            for (String plantName : ZombotanyLooks.overlayPlants()) {
                preloadPlantPam(plantName);
            }
        }
        ProjectileClips projectileClips = new ProjectileClips();
        for (String flightPath : projectileClips.flightPaths()) {
            preload(flightPath);
        }
        for (String splatPath : projectileClips.splatPaths()) {
            preload(splatPath);
        }
        preload(PlantClips.ICE_BLOCK_PATH);
        preload(PlantClips.OCTOPUS_PATH);
        preload(PlantClips.GRAVE_BUSTER_DIRT_PATH);
        preload(ExplosionLooks.BONE_HIT_PATH);
        assets.region(ExplosionLooks.BONE_PROJECTILE_IMAGE);
        preload(ArcadeObstacleSync.PAM_PATH);
        preload(PianoObstacleSync.PAM_PATH);
        preload(SunSync.SUN_PATH);
        preload(mowerPath(ChapterId.fromName(session.getChapterId())));
        if (ChapterId.fromName(session.getChapterId()) == ChapterId.ANCIENT_EGYPT) {
            preload(SandstormSync.REAR_PATH);
            preload(SandstormSync.TOP_PATH);
        }
        if (ChapterId.fromName(session.getChapterId()) == ChapterId.FROSTBITE_CAVES) {
            preload(SlipperyTileSync.UP_PATH);
            preload(SlipperyTileSync.DOWN_PATH);
            preload(ZombossClips.CHILL_WIND);
        }
        for (String gravePath : GraveClips.preloadPaths(ChapterId.fromName(session.getChapterId()))) {
            preload(gravePath);
        }
        if (session.getWaveManager() != null) {
            for (String alias : session.getWaveManager().getZombiePool()) {
                preload(catalog.zombiePath(alias));
            }
        }
    }

    private void preload(String pamPath) {
        if (pamPath != null && !pamPath.isBlank()) {
            assets.pamPlayer().loadAsync(pamPath, () -> {
            });
        }
    }

    private void preloadSync(String pamPath) {
        if (pamPath == null || pamPath.isBlank()) {
            return;
        }
        try {
            assets.pamPlayer().loadSync(pamPath);
        } catch (RuntimeException e) {
            Gdx.app.error("GamePlayScreen", "Failed to load " + pamPath, e);
        }
    }

    private static String mowerPath(ChapterId chapterId) {
        if (chapterId == null) {
            return "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";
        }
        return switch (chapterId) {
            case ANCIENT_EGYPT -> "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";
            case FROSTBITE_CAVES -> "768/FULL/MOWERS/MOWER_ICEAGE/MOWER_ICEAGE.PAM";
            case BIG_WAVE_BEACH -> "768/FULL/MOWERS/MOWER_BEACH/MOWER_BEACH.PAM";
            case DARK_AGES -> "768/FULL/MOWERS/MOWER_DARK/MOWER_DARK.PAM";
        };
    }

    private boolean sandboxMatch() {
        GameSession session = matchSession();
        return session != null && session.isSandboxPractice();
    }

    private void layoutSandbox() {
        if (!sandboxMatch()) {
            if (zombieSandbox != null) {
                zombieSandbox.setVisible(false);
            }
            if (plantSandbox != null) {
                plantSandbox.setVisible(false);
            }
            return;
        }
        if (plantSandbox != null) {
            plantSandbox.setVisible(true);
            float height = Math.max(240f, hudLayer.getHeight() - 36f);
            plantSandbox.setSize(PlantSandboxPanel.PANEL_WIDTH, height);
            plantSandbox.setPosition(8f, 18f);
            plantSandbox.validate();
            plantSandbox.toFront();
        }
        if (zombieSandbox != null) {
            zombieSandbox.setVisible(true);
            zombieSandbox.pack();
            float x = Math.max(12f, hudLayer.getWidth() - zombieSandbox.getWidth() - 14f);
            zombieSandbox.setPosition(x, 18f);
            zombieSandbox.toFront();
        }
    }

    private ZombieSandboxPanel.Host sandboxHost() {
        return new ZombieSandboxPanel.Host() {
            @Override
            public void spawnSolo(String alias, int row) {
                sandboxSpawn(alias, row);
            }

            @Override
            public void spawnPack(String alias) {
                sandboxPack(alias);
            }

            @Override
            public void spawnWave() {
                sandboxWave();
            }

            @Override
            public void clearZombies() {
                sandboxClear();
            }

            @Override
            public void dropSun() {
                sandboxDropSun();
            }
        };
    }

    private void sandboxSpawn(String alias, int row) {
        GameSession session = matchSession();
        if (session == null || alias == null) {
            return;
        }
        int lanes = Math.max(1, session.getBoard().getRows());
        int lane = Math.max(0, Math.min(lanes - 1, row));
        double x = Math.max(0.5, session.getBoard().getCols() - 0.6);
        if (session.isPrepPhaseActive() && !session.isSandboxPractice()) {
            session.startZombieWaves();
        }
        if (controller != null) {
            controller.cheatSpawnZombie(alias, lane, x);
            return;
        }
        session.spawnZombieOfType(alias, lane, x);
    }

    private void sandboxWave() {
        GameSession session = matchSession();
        if (session == null) {
            return;
        }
        if (session.isPrepPhaseActive() && !session.isSandboxPractice()) {
            session.startZombieWaves();
        }
        int rows = Math.max(1, session.getBoard().getRows());
        double right = session.getBoard().getCols() - 0.4;
        List<String> aliases = ZombieSandboxPanel.ALIASES;
        for (int i = 0; i < aliases.size(); i++) {
            int row = i % rows;
            int column = i / rows;
            double x = Math.max(right - 6.5, right - column * 1.05);
            if (controller != null) {
                controller.cheatSpawnZombie(aliases.get(i), row, x);
            } else {
                session.spawnZombieOfType(aliases.get(i), row, x);
            }
        }
    }

    private void sandboxPack(String alias) {
        GameSession session = matchSession();
        if (session == null || alias == null) {
            return;
        }
        if (session.isPrepPhaseActive() && !session.isSandboxPractice()) {
            session.startZombieWaves();
        }
        int rows = Math.max(1, session.getBoard().getRows());
        double right = session.getBoard().getCols() - 0.5;
        for (int i = 0; i < 8; i++) {
            int row = i % rows;
            double x = Math.max(right - 3.2, right - (i / rows) * 0.9);
            if (controller != null) {
                controller.cheatSpawnZombie(alias, row, x);
            } else {
                session.spawnZombieOfType(alias, row, x);
            }
        }
    }

    private void sandboxClear() {
        if (controller != null) {
            controller.cheatNuke();
            return;
        }
        GameSession session = matchSession();
        if (session != null) {
            session.nukeAllZombies();
        }
    }

    private void sandboxDropSun() {
        GameSession session = matchSession();
        if (session == null) {
            return;
        }
        int rows = session.getBoard().getRows();
        int col = Math.max(1, session.getBoard().getCols() / 2);
        for (int row = 0; row < rows; row++) {
            if (controller != null) {
                controller.cheatDropSun(col, row, 25);
            } else {
                session.spawnSkySun(col, row, 25);
            }
        }
    }

    private GameSession matchSession() {
        if (vaseBreaker != null) {
            return vaseBreaker.session();
        }
        if (walnutBowling != null) {
            return walnutBowling.session();
        }
        if (iZombie != null) {
            return iZombie.session();
        }
        if (networkedIZombie != null) {
            return networkedIZombie.session();
        }
        if (couchIZombie != null) {
            return couchIZombie.session();
        }
        if (beghouled != null) {
            return beghouled.session();
        }
        if (zombotany != null) {
            return zombotany.session();
        }
        return controller == null ? null : controller.session();
    }

    private Set<String> seedBoosts() {
        if (controller != null) {
            return controller.boostedPlants();
        }
        if (zombotany != null) {
            return zombotany.boostedPlants();
        }
        return Set.of();
    }

    private User matchUser() {
        if (vaseBreaker != null) {
            return vaseBreaker.getUser();
        }
        if (walnutBowling != null) {
            return walnutBowling.getUser();
        }
        if (iZombie != null) {
            return iZombie.getUser();
        }
        if (networkedIZombie != null) {
            return networkedIZombie.getUser();
        }
        if (couchIZombie != null) {
            return couchIZombie.getUser();
        }
        if (beghouled != null) {
            return beghouled.getUser();
        }
        if (zombotany != null) {
            return zombotany.getUser();
        }
        return controller == null ? null : controller.getUser();
    }

    private static boolean hasGroundPacket(GameSession session, String plantName) {
        if (session == null || plantName == null || plantName.isBlank()) {
            return false;
        }
        for (GroundSeedPacket packet : session.getGroundSeedPackets()) {
            if (packet != null && plantName.equals(packet.plantName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hideAdventureTools() {
        return vaseBreaker != null || walnutBowling != null;
    }

    private boolean hideSeedTools() {
        return hideAdventureTools() || beghouled != null || iZombie != null || networkedZombieRole()
                || (networkedIZombie != null && networkedIZombie.isPicking())
                || couchIZombie != null;
    }

    private boolean networkedZombieRole() {
        return networkedIZombie != null && networkedIZombie.role() == MatchRole.ZOMBIE;
    }

    private boolean networkedPlantRole() {
        return networkedIZombie != null && networkedIZombie.role() == MatchRole.PLANT;
    }

    private int displaySunBalance(GameSession session) {
        if (session == null) {
            return 0;
        }
        if (couchIZombie != null) {
            return session.getSunBalance();
        }
        if (iZombie != null || networkedZombieRole()) {
            return session.getIZombieSunBalance();
        }
        return session.getSunBalance();
    }

    private LawnActionHost lawnHost() {
        if (vaseBreaker != null) {
            return new ControllerLawnHost(vaseBreaker);
        }
        if (walnutBowling != null) {
            return new ControllerLawnHost(walnutBowling);
        }
        if (iZombie != null) {
            return new ControllerLawnHost(iZombie);
        }
        if (networkedIZombie != null) {
            return new ControllerLawnHost(networkedIZombie);
        }
        if (couchIZombie != null) {
            return new ControllerLawnHost(couchIZombie);
        }
        if (beghouled != null) {
            return new ControllerLawnHost(beghouled);
        }
        if (zombotany != null) {
            return new ControllerLawnHost(zombotany);
        }
        return new ControllerLawnHost(controller);
    }

    private ControllerTicker matchTicker() {
        if (vaseBreaker != null) {
            return new ControllerTicker(vaseBreaker);
        }
        if (walnutBowling != null) {
            return new ControllerTicker(walnutBowling);
        }
        if (iZombie != null) {
            return new ControllerTicker(iZombie);
        }
        if (networkedIZombie != null) {
            return new ControllerTicker(networkedIZombie);
        }
        if (couchIZombie != null) {
            return new ControllerTicker(couchIZombie);
        }
        if (beghouled != null) {
            return new ControllerTicker(beghouled);
        }
        if (zombotany != null) {
            return new ControllerTicker(zombotany);
        }
        return new ControllerTicker(controller);
    }
}
