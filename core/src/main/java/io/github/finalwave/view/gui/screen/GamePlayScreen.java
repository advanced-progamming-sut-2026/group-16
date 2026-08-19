package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.SeedPlacement;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
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
import io.github.finalwave.view.gui.hud.special.ConveyorBeltBar;
import io.github.finalwave.view.gui.hud.special.StartWaveButton;
import io.github.finalwave.view.gui.hud.special.TimedWarPanel;
import io.github.finalwave.view.gui.input.LawnInputController;
import io.github.finalwave.view.gui.input.ToolMode;
import io.github.finalwave.view.gui.match.MatchClock;
import io.github.finalwave.view.gui.render.BattlefieldGroup;
import io.github.finalwave.view.gui.render.ChapterBackground;
import io.github.finalwave.view.gui.render.LawnGridOverlay;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.GraveClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.sync.DeadLineSync;
import io.github.finalwave.view.gui.render.sync.ProtectTileSync;
import io.github.finalwave.view.gui.render.sync.SunSync;

import java.util.HashSet;
import java.util.Set;


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
    private ConveyorBeltBar conveyorBeltBar;
    private StartWaveButton startWaveButton;
    private Table hudTop;
    private Table hudBottom;
    private SunCounter sunCounter;
    private TimedWarPanel timedWarPanel;
    private final Set<String> preloadedPlantPams = new HashSet<>();
    private PlantFoodCounter plantFoodCounter;
    private WaveProgressMeter waveMeter;
    private SpeedButton speedButton;
    private AlertBanner alertBanner;
    private LevelObjectiveBanner objectiveBanner;
    private NpcDialogBox npcDialog;
    private WidgetGroup cursorLayer;
    private boolean resultShown;
    private float resultHold;

    public GamePlayScreen(PvzGame game) {
        super(game, new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));
    }

    public void bind(GamePlayController controller) {
        this.controller = controller;
        this.resultShown = false;
        this.resultHold = 0f;
        this.clock = null;
        preloadedPlantPams.clear();
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
        cursorLayer = new WidgetGroup();
        cursorLayer.setFillParent(true);
        cursorLayer.setTouchable(Touchable.disabled);
        stage.addActor(cursorLayer);
        input = new LawnInputController(
                controller,
                layout,
                battlefield,
                assets,
                catalog,
                cursorLayer,
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
        if (clock != null && battlefield != null) {
            float unitSpeed = clock.speed();
            float environmentSpeed = unitSpeed;
            GameSession session = controller == null ? null : controller.session();
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
        refreshHud();
        pollResult(delta);
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
        if (resultShown || result == null || result == MatchResult.IN_PROGRESS) {
            return;
        }
        if (deadlineHoldRemaining(result) > 0f) {
            return;
        }
        presentResult(result);
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
        if (timedWarPanel != null) {
            timedWarPanel.refresh(session);
        }
        if (plantFoodCounter != null && session != null) {
            plantFoodCounter.setCount(session.getPlantFoodCount());
        }
        if (seedBank != null && session != null) {
            seedBank.refresh(session, user, controller.boostedPlants(), input == null ? null : input.mode());
        }
        if (conveyorBeltBar != null) {
            boolean freeze = clock != null && clock.shouldFreeze();
            conveyorBeltBar.refresh(session, user, input == null ? null : input.mode(), freeze);
            updateSunPad();
            if (session != null && session.isConveyorBeltActive()) {
                for (String plantName : session.getConveyorBeltPlants()) {
                    preloadPlantPam(plantName);
                }
            }
        }
        if (startWaveButton != null) {
            startWaveButton.refresh(session);
        }
        if (waveMeter != null) {
            waveMeter.refresh(session);
        }
        if (speedButton != null && clock != null) {
            speedButton.setSpeed(clock.speed());
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
        if (cursorLayer != null) {
            cursorLayer.remove();
            cursorLayer = null;
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
        plantFoodCounter = new PlantFoodCounter(assets, this::onAddPlantFood, this::onPlantFoodDragStart, this::onPlantFoodDrop);
        seedBank = new SeedBankBar(assets, this::onSeed);
        conveyorBeltBar = new ConveyorBeltBar(assets, this::onSeed);
        startWaveButton = new StartWaveButton(assets, this::onStartWaves);
        waveMeter = new WaveProgressMeter(assets);
        speedButton = new SpeedButton(assets, this::onSpeed);
        Actor pause = PauseButton.create(assets, this::togglePause);
        Actor shovel = ShovelButton.create(assets, this::onShovel);

        hudTop = new Table();
        hudTop.add(sunCounter).padLeft(sunPad()).padTop(10f);
        hudTop.add(startWaveButton).padLeft(8f).padTop(10f);
        hudTop.add(timedWarPanel).padLeft(8f).padTop(10f);
        hudTop.add().expandX();
        hudTop.add(meterBlock()).padTop(8f);
        hudTop.add().expandX();
        hudTop.add(speedButton.actor()).size(84f).padRight(8f).padTop(8f);
        hudTop.add(pause).size(84f).padRight(8f).padTop(8f);
        hudTop.add(currencyBar).padTop(12f).padRight(20f);
        hudLayer.add(hudTop).growX().row();

        Table mid = new Table();
        mid.setTouchable(Touchable.childrenOnly);
        mid.add(seedBank).left().top().padLeft(8f).padTop(6f);
        mid.add().expand();
        hudLayer.add(mid).grow().row();

        hudBottom = new Table();
        hudBottom.add(plantFoodCounter).left().padLeft(plantFoodPad()).padBottom(18f);
        hudBottom.add().expandX();
        hudBottom.add(shovel).size(84f).padRight(20f).padBottom(18f);
        hudLayer.add(hudBottom).growX();
        hudLayer.addActor(conveyorBeltBar);

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

    private Table meterBlock() {
        Table block = new Table();
        block.add(waveMeter).size(420f, 48f).row();
        Label title = new Label(levelCaption(), assets.skin(), "medium");
        title.setAlignment(Align.center);
        title.setFontScale(0.62f);
        block.add(title).padTop(2f);
        return block;
    }

    private String levelCaption() {
        if (controller == null || controller.chapter() == null || controller.level() == null) {
            return "";
        }
        return controller.chapter().getDisplayName() + " - Day " + controller.level().getIndex();
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

    private void onAddSun() {
        if (controller != null) {
            controller.cheatAddSun(50);
        }
    }

    private void onAddPlantFood() {
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
        if (clock != null) {
            clock.cycleSpeed();
        }
    }

    private void togglePause() {
        if (resultShown || resultModal.isShowing() || matchFinished()) {
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
        pauseModal.show(modalLayer, viewport, assets, this::resumeMatch, this::restartMatch, this::exitMatch);
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

    private void pollResult(float delta) {
        if (controller == null || controller.session() == null) {
            return;
        }
        GameSession session = controller.session();
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
        if (controller == null || controller.session() == null || !controller.session().isDeadLineActive()) {
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
        resultModal.show(
                modalLayer,
                viewport,
                assets.skin(),
                result,
                this::exitMatch,
                result == MatchResult.LOST ? this::restartMatch : null);
    }

    private boolean inputBlocked() {
        return resultShown
                || pauseModal.isShowing()
                || (clock != null && clock.isPaused())
                || matchFinished();
    }

    private boolean matchFinished() {
        return controller != null
                && controller.session() != null
                && controller.session().getMatchResult() != MatchResult.IN_PROGRESS;
    }

    private boolean shouldDrawGrid() {
        return layout != null
                && controller != null
                && controller.getUser() != null
                && controller.getUser().isShowLawnGrid();
    }

    private void updateSunPad() {
        if (hudTop != null && sunCounter != null) {
            Cell<?> sunCell = hudTop.getCell(sunCounter);
            if (sunCell != null) {
                sunCell.padLeft(sunPad());
                hudTop.invalidate();
            }
        }
        if (hudBottom != null && plantFoodCounter != null) {
            Cell<?> foodCell = hudBottom.getCell(plantFoodCounter);
            if (foodCell != null) {
                foodCell.padLeft(plantFoodPad());
                hudBottom.invalidate();
            }
        }
    }

    private float sunPad() {
        if (conveyorBeltBar != null && conveyorBeltBar.isVisible()) {
            return conveyorBeltBar.stripWidth() + 16f;
        }
        return 16f;
    }

    private float plantFoodPad() {
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
        if (session.isConveyorBeltActive()) {
            assets.region(LawnAssetIds.CONVEYOR_BELT);
            assets.region(LawnAssetIds.CONVEYOR_SIDE);
        }
        if (!session.getProtectedSeedPlacements().isEmpty()) {
            assets.region(LawnAssetIds.PROTECT_TILE);
            preload(ProtectTileSync.PAM_PATH);
        }
        if (session.isPrepPhaseActive()) {
            assets.region(LawnAssetIds.PURPLE_BUTTON);
            assets.region(LawnAssetIds.PURPLE_BUTTON_DOWN);
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
        if (session.isConveyorBeltActive() && controller != null && controller.getUser() != null) {
            for (String plantName : controller.getUser().getPlantProgress().getUnlockedPlantNames()) {
                preloadPlantPam(plantName);
            }
        }
        preload(PlantClips.ICE_BLOCK_PATH);
        preload(SunSync.SUN_PATH);
        preload(mowerPath(ChapterId.fromName(session.getChapterId())));
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
