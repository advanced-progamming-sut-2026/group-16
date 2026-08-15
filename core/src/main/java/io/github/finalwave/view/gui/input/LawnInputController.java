package io.github.finalwave.view.gui.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.render.BattlefieldGroup;
import io.github.finalwave.view.gui.render.LawnHighlights;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.function.BooleanSupplier;


public final class LawnInputController implements Disposable {
    private static final ToolMode.None NONE = new ToolMode.None();

    private final GamePlayController controller;
    private final LawnLayout layout;
    private final BattlefieldGroup battlefield;
    private final GameAssets assets;
    private final PlantClips plantClips;
    private final BooleanSupplier blocked;
    private final LawnHighlights highlights;
    private final PamActor plantGhost;
    private final Image toolGhost;
    private final Vector2 cursor = new Vector2();
    private ToolMode mode = NONE;
    private InputListener listener;

    public LawnInputController(GamePlayController controller,
                               LawnLayout layout,
                               BattlefieldGroup battlefield,
                               GameAssets assets,
                               EntityAnimationCatalog catalog,
                               BooleanSupplier blocked) {
        this.controller = controller;
        this.layout = layout;
        this.battlefield = battlefield;
        this.assets = assets;
        this.plantClips = new PlantClips(catalog);
        this.blocked = blocked;
        this.highlights = new LawnHighlights(battlefield.highlightLayer());
        this.plantGhost = new PamActor(assets.pamPlayer());
        this.plantGhost.setTouchable(Touchable.disabled);
        this.plantGhost.setVisible(false);
        this.plantGhost.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        this.toolGhost = new Image();
        this.toolGhost.setTouchable(Touchable.disabled);
        this.toolGhost.setVisible(false);
        this.toolGhost.setScaling(Scaling.fit);
        battlefield.fxLayer().addActor(plantGhost);
        battlefield.fxLayer().addActor(toolGhost);
        attach();
    }

    public ToolMode mode() {
        return mode;
    }

    public void setMode(ToolMode next) {
        if (next == null) {
            next = NONE;
        }
        this.mode = next;
        refreshGhost();
        if (mode instanceof ToolMode.None) {
            highlights.hide();
        }
    }

    public void toggleSeed(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            setMode(NONE);
            return;
        }
        if (mode instanceof ToolMode.Seed seed && plantName.equals(seed.plantName())) {
            setMode(NONE);
            return;
        }
        setMode(new ToolMode.Seed(plantName));
    }

    public void toggleShovel() {
        setMode(mode instanceof ToolMode.Shovel ? NONE : new ToolMode.Shovel());
    }

    public void togglePlantFood() {
        if (mode instanceof ToolMode.PlantFood) {
            setMode(NONE);
            return;
        }
        setMode(new ToolMode.PlantFood());
    }

    public void update() {
        if (blocked()) {
            plantGhost.setVisible(false);
            toolGhost.setVisible(false);
            highlights.hide();
            return;
        }
        Stage stage = battlefield.getStage();
        if (stage == null) {
            return;
        }
        cursor.set(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(cursor);
        battlefield.stageToLocalCoordinates(cursor);
        int col = layout.colAt(cursor.x);
        int row = layout.rowAt(cursor.y);
        if (col >= 0 && row >= 0) {
            highlights.show(layout, col, row);
        } else {
            highlights.hide();
        }
        placeGhost(cursor.x, cursor.y);
    }

    public void collectSun(Sun sun) {
        if (sun == null || sun.isExpired() || blocked()) {
            return;
        }
        tryCollect(sun.getCol(), sun.getRow());
    }

    @Override
    public void dispose() {
        if (listener != null) {
            battlefield.removeListener(listener);
            listener = null;
        }
        highlights.dispose();
        plantGhost.remove();
        toolGhost.remove();
    }

    private void attach() {
        listener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) {
                    setMode(NONE);
                    return true;
                }
                if (button != Input.Buttons.LEFT || blocked()) {
                    return false;
                }
                int col = layout.colAt(x);
                int row = layout.rowAt(y);
                if (col < 0 || row < 0) {
                    return false;
                }
                return apply(col, row);
            }
        };
        battlefield.addListener(listener);
    }

    private boolean apply(int col, int row) {
        if (tryCollect(col, row)) {
            return true;
        }
        if (mode instanceof ToolMode.Seed seed) {
            controller.plantAt(seed.plantName(), col, row);
            return true;
        }
        if (mode instanceof ToolMode.Shovel) {
            controller.shovelAt(col, row);
            return true;
        }
        if (mode instanceof ToolMode.PlantFood) {
            controller.feedAt(col, row);
            return true;
        }
        return false;
    }

    private boolean tryCollect(int col, int row) {
        GameSession session = controller.session();
        if (session == null) {
            return false;
        }
        for (Sun sun : session.getSunItems()) {
            if (sun != null && !sun.isExpired() && sun.getCol() == col && sun.getRow() == row) {
                return controller.collectSunAt(col, row);
            }
        }
        return false;
    }

    private void refreshGhost() {
        plantGhost.setVisible(false);
        toolGhost.setVisible(false);
        if (mode instanceof ToolMode.Seed seed) {
            EntityAnimationCatalog.ClipSpec idle = plantClips.idle(seed.plantName());
            plantGhost.setSize(layout.tileWidth(), layout.tileHeight());
            plantGhost.setClip(idle.path(), idle.clip(), plantClips.scale(seed.plantName()), true);
            plantGhost.setVisible(true);
            return;
        }
        if (mode instanceof ToolMode.Shovel) {
            toolGhost.setDrawable(new TextureRegionDrawable(assets.region(LawnAssetIds.FLOATING_SHOVEL)));
            toolGhost.setSize(72f, 72f);
            toolGhost.setVisible(true);
            return;
        }
        if (mode instanceof ToolMode.PlantFood) {
            toolGhost.setDrawable(new TextureRegionDrawable(assets.region(LawnAssetIds.FLOATING_PLANTFOOD)));
            toolGhost.setSize(72f, 72f);
            toolGhost.setVisible(true);
        }
    }

    private void placeGhost(float x, float y) {
        if (plantGhost.isVisible()) {
            plantGhost.setPosition(x - plantGhost.getWidth() / 2f, y - plantGhost.getHeight() * LawnLayout.PLANT_ANCHOR_Y);
        }
        if (toolGhost.isVisible()) {
            toolGhost.setPosition(x - toolGhost.getWidth() / 2f, y - toolGhost.getHeight() / 2f);
        }
    }

    private boolean blocked() {
        if (blocked != null && blocked.getAsBoolean()) {
            return true;
        }
        GameSession session = controller == null ? null : controller.session();
        return session == null || session.getMatchResult() != MatchResult.IN_PROGRESS;
    }
}
