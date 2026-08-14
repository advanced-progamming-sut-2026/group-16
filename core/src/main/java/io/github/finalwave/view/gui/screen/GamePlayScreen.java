package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.hud.AlertBanner;
import io.github.finalwave.view.gui.hud.LevelObjectiveBanner;
import io.github.finalwave.view.gui.hud.MatchResultModal;
import io.github.finalwave.view.gui.hud.NpcDialogBox;
import io.github.finalwave.view.gui.hud.PauseButton;
import io.github.finalwave.view.gui.hud.PauseModal;
import io.github.finalwave.view.gui.hud.PlantFoodCounter;
import io.github.finalwave.view.gui.hud.SeedBankBar;
import io.github.finalwave.view.gui.hud.ShovelButton;
import io.github.finalwave.view.gui.hud.SpeedButton;
import io.github.finalwave.view.gui.hud.SunCounter;
import io.github.finalwave.view.gui.hud.WaveProgressMeter;
import io.github.finalwave.view.gui.input.LawnInputController;
import io.github.finalwave.view.gui.input.ToolMode;
import io.github.finalwave.view.gui.match.MatchClock;
import io.github.finalwave.view.gui.render.BattlefieldGroup;
import io.github.finalwave.view.gui.render.ChapterBackground;
import io.github.finalwave.view.gui.render.LawnGridOverlay;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.sync.SunSync;
import io.github.finalwave.view.gui.widget.PvzButtons;


public final class GamePlayScreen extends MenuScreen {
    private final LawnGridOverlay gridOverlay = new LawnGridOverlay();
    private final PauseModal pauseModal = new PauseModal();
    private final MatchResultModal resultModal = new MatchResultModal();

    private GamePlayController controller;
    private MatchClock clock;
    private LawnLayout layout;
    private ChapterBackground chapterBackground;
    private BattlefieldGroup battlefield;
    private EntityAnimationCatalog catalog;
    private LawnInputController input;
    private SeedBankBar seedBank;
    private SunCounter sunCounter;
    private PlantFoodCounter plantFoodCounter;
    private WaveProgressMeter waveMeter;
    private SpeedButton speedButton;
    private AlertBanner alertBanner;
    private LevelObjectiveBanner objectiveBanner;
    private NpcDialogBox npcDialog;
    private Table debugCheats;
    private boolean resultShown;

    public GamePlayScreen(PvzGame game) {
        super(game, new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
    }

    public void bind(GamePlayController controller) {
        this.controller = controller;
        this.resultShown = false;
        this.clock = null;
        this.chapterBackground = new ChapterBackground(assets, ChapterId.ANCIENT_EGYPT);
        this.layout = lawnLayoutFor(5, 9);
        this.catalog = new EntityAnimationCatalog(assets.root());
        pauseModal.dismiss();
        resultModal.dismiss();
        if (objectiveBanner != null) {
            objectiveBanner.reset();
        }
        if (controller == null) {
            return;
        }
        User user = controller.getUser();
        GameSession session = controller.session();
        clock = new MatchClock(controller, user);
        if (session != null && session.getBoard() != null) {
            GameBoard board = session.getBoard();
            chapterBackground = new ChapterBackground(assets, ChapterId.fromName(session.getChapterId()));
            layout = lawnLayoutFor(board.getRows(), board.getCols());
            preloadMatchAssets(session);
        }
        if (user != null) {
            bindCurrency(user);
        }
    }

    private LawnLayout lawnLayoutFor(int rows, int cols) {
        chapterBackground.layoutFor(WORLD_WIDTH, WORLD_HEIGHT, cols);
        return chapterBackground.lawnLayout(rows, cols);
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
        input = new LawnInputController(
                controller,
                layout,
                battlefield,
                assets,
                catalog,
                this::inputBlocked);
        battlefield.setSunCollector(sun -> {
            if (input != null) {
                input.collectSun(sun);
            }
        });
        buildHud();
        if (objectiveBanner != null && controller != null) {
            objectiveBanner.showOnce(controller.chapter(), controller.level());
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }
        float tickFraction = 0f;
        if (clock != null && battlefield != null) {
            clock.update(delta, battlefield);
            tickFraction = clock.tickFraction();
        }
        if (battlefield != null && controller != null && controller.session() != null) {
            battlefield.sync(controller.session(), tickFraction);
        }
        if (input != null) {
            input.update();
        }
        refreshHud();
        pollResult();
        viewport.apply();
        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (chapterBackground != null) {
            chapterBackground.draw(batch, viewport);
        }
        batch.end();
        if (shouldDrawGrid()) {
            gridOverlay.draw(viewport, layout);
            Gdx.gl.glEnable(GL20.GL_BLEND);
        }
        stage.act(delta);
        stage.draw();
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

    public void showPauseModal() {
        openPause();
    }

    public void hidePauseModal() {
        resumeMatch();
    }

    public void showResult(MatchResult result) {
        if (clock != null) {
            clock.setResultShowing(true);
        }
        if (resultShown || result == null || result == MatchResult.IN_PROGRESS) {
            return;
        }
        resultShown = true;
        pauseModal.dismiss();
        resultModal.show(
                modalLayer,
                viewport,
                assets.skin(),
                result,
                this::exitMatch,
                result == MatchResult.LOST ? this::restartMatch : null);
    }

    public void refreshHud() {
        if (controller == null) {
            return;
        }
        User user = controller.getUser();
        GameSession session = controller.session();
        if (user != null) {
            bindCurrency(user);
        }
        if (sunCounter != null && session != null) {
            sunCounter.setAmount(session.getSunBalance());
        }
        if (plantFoodCounter != null && session != null) {
            plantFoodCounter.setCount(session.getPlantFoodCount());
        }
        if (seedBank != null && session != null) {
            seedBank.refresh(session, user, controller.boostedPlants(), input == null ? null : input.mode());
        }
        if (waveMeter != null) {
            waveMeter.refresh(session);
        }
        if (speedButton != null && clock != null) {
            speedButton.setSpeed(clock.speed());
        }
        if (debugCheats != null) {
            debugCheats.setVisible(user != null && user.isDebugMode());
        }
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
        pauseModal.dismiss();
        resultModal.dismiss();
        if (battlefield != null) {
            battlefield.clearBattlefield();
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
        gridOverlay.dispose();
        super.dispose();
    }

    private void buildHud() {
        hudLayer.clearChildren();
        hudLayer.setTouchable(Touchable.childrenOnly);
        hudLayer.top().left();
        hudLayer.pad(0f);

        sunCounter = new SunCounter(assets);
        plantFoodCounter = new PlantFoodCounter(assets, this::onPlantFood);
        seedBank = new SeedBankBar(assets, this::onSeed);
        waveMeter = new WaveProgressMeter(assets);
        speedButton = new SpeedButton(assets, this::onSpeed);
        Actor pause = PauseButton.create(assets, this::togglePause);
        Actor shovel = ShovelButton.create(assets, this::onShovel);
        debugCheats = debugTable();

        Table top = new Table();
        top.add(sunCounter).padLeft(16f).padTop(10f);
        top.add(plantFoodCounter).padLeft(12f).padTop(10f);
        top.add(debugCheats).padLeft(12f).padTop(10f);
        top.add().expandX();
        top.add(shovel).size(84f).padRight(8f).padTop(8f);
        top.add(speedButton.actor()).size(84f).padRight(8f).padTop(8f);
        top.add(pause).size(84f).padRight(8f).padTop(8f);
        top.add(currencyBar).padTop(12f).padRight(20f);
        hudLayer.add(top).growX().row();

        Table mid = new Table();
        mid.add(seedBank).left().top().padLeft(12f).padTop(6f);
        mid.add().expand();
        hudLayer.add(mid).grow().row();
        hudLayer.add(waveMeter).size(420f, 48f).center().padBottom(16f);

        if (alertBanner != null) {
            alertBanner.remove();
        }
        if (objectiveBanner != null) {
            objectiveBanner.remove();
        }
        if (npcDialog != null) {
            npcDialog.remove();
        }
        alertBanner = new AlertBanner(assets.skin());
        objectiveBanner = new LevelObjectiveBanner(assets.skin());
        npcDialog = new NpcDialogBox(assets.skin());
        toastLayer.addActor(alertBanner);
        toastLayer.addActor(objectiveBanner);
        toastLayer.addActor(npcDialog);
        npcDialog.setPosition(640f, 24f);
    }

    private Table debugTable() {
        Table table = new Table();
        TextButton sun = PvzButtons.textButton("+Sun", assets.skin(), "brown", () -> {
            if (controller != null) {
                controller.cheatAddSun(50);
            }
        });
        TextButton food = PvzButtons.textButton("+Food", assets.skin(), "brown", () -> {
            if (controller != null) {
                controller.cheatAddPlantFood();
            }
        });
        table.add(sun).width(110f).height(42f).padRight(8f);
        table.add(food).width(110f).height(42f);
        table.setVisible(false);
        return table;
    }

    private void onSeed(String plantName) {
        if (input != null) {
            input.toggleSeed(plantName);
        }
    }

    private void onShovel() {
        if (input != null) {
            input.toggleShovel();
        }
    }

    private void onPlantFood() {
        if (controller != null && controller.session() != null && controller.session().getPlantFoodCount() <= 0) {
            controller.feedAt(0, 0);
            return;
        }
        if (input != null) {
            input.togglePlantFood();
        }
    }

    private void onSpeed() {
        if (clock != null) {
            clock.cycleSpeed();
        }
    }

    private void togglePause() {
        if (resultShown || resultModal.isShowing()) {
            return;
        }
        if (pauseModal.isShowing()) {
            resumeMatch();
            return;
        }
        openPause();
    }

    private void openPause() {
        if (clock != null) {
            clock.setPaused(true);
        }
        if (input != null) {
            input.setMode(new ToolMode.None());
        }
        pauseModal.show(modalLayer, viewport, assets.skin(), this::resumeMatch, this::restartMatch, this::exitMatch);
    }

    private void resumeMatch() {
        pauseModal.dismiss();
        if (clock != null && !resultShown) {
            clock.setPaused(false);
        }
    }

    private void exitMatch() {
        if (controller != null) {
            controller.confirmMatchExit();
        }
    }

    private void restartMatch() {
        if (controller != null) {
            controller.restartMatch();
        }
    }

    private void pollResult() {
        if (controller == null || controller.session() == null) {
            return;
        }
        MatchResult result = controller.session().getMatchResult();
        if (result != MatchResult.IN_PROGRESS) {
            showResult(result);
        }
    }

    private boolean inputBlocked() {
        return resultShown
                || pauseModal.isShowing()
                || (clock != null && clock.isPaused());
    }

    private boolean shouldDrawGrid() {
        return layout != null
                && controller != null
                && controller.getUser() != null
                && controller.getUser().isShowLawnGrid();
    }

    private void preloadMatchAssets(GameSession session) {
        if (catalog == null) {
            catalog = new EntityAnimationCatalog(assets.root());
        }
        for (String plantName : session.getSelectedLoadout()) {
            preload(catalog.plantIdle(plantName).path());
        }
        for (String plantName : session.getConveyorBeltPlants()) {
            preload(catalog.plantIdle(plantName).path());
        }
        preload(PlantClips.ICE_BLOCK_PATH);
        preload(SunSync.SUN_PATH);
        preload(mowerPath(ChapterId.fromName(session.getChapterId())));
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
}
