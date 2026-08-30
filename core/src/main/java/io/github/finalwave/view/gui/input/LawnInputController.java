package io.github.finalwave.view.gui.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.PepperMuzzles;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.minigame.GroundSeedPacket;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.render.BattlefieldGroup;
import io.github.finalwave.view.gui.render.LawnHighlights;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.sync.PlantSync;
import io.github.finalwave.view.gui.render.clip.ZombieClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.function.BooleanSupplier;


public final class LawnInputController implements Disposable {
    private static final ToolMode.None NONE = new ToolMode.None();

    private final LawnActionHost host;
    private final LawnLayout layout;
    private final BattlefieldGroup battlefield;
    private final GameAssets assets;
    private final PlantClips plantClips;
    private final ZombieClips zombieClips;
    private final BooleanSupplier blocked;
    private final LawnHighlights highlights;
    private final PamActor plantGhost;
    private final Image toolGhost;
    private final Vector2 cursor = new Vector2();
    private final Vector2 lawnCursor = new Vector2();
    private ToolMode mode = NONE;
    private InputListener listener;

    public LawnInputController(LawnActionHost host,
                               LawnLayout layout,
                               BattlefieldGroup battlefield,
                               GameAssets assets,
                               EntityAnimationCatalog catalog,
                               Group cursorLayer,
                               BooleanSupplier blocked) {
        this.host = host;
        this.layout = layout;
        this.battlefield = battlefield;
        this.assets = assets;
        this.plantClips = new PlantClips(catalog);
        this.zombieClips = new ZombieClips(catalog);
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
        Group overlay = cursorLayer == null ? battlefield.fxLayer() : cursorLayer;
        overlay.addActor(plantGhost);
        overlay.addActor(toolGhost);
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

    public void toggleZombie(String alias) {
        if (alias == null || alias.isBlank()) {
            setMode(NONE);
            return;
        }
        if (mode instanceof ToolMode.Zombie zombie && alias.equals(zombie.alias())) {
            setMode(NONE);
            return;
        }
        setMode(new ToolMode.Zombie(alias));
    }

    public void toggleShovel() {
        setMode(mode instanceof ToolMode.Shovel ? NONE : new ToolMode.Shovel());
    }

    public void beginPlantFoodDrag() {
        if (blocked()) {
            return;
        }
        setMode(new ToolMode.PlantFood());
    }

    public void dropPlantFoodAtStage(float stageX, float stageY) {
        if (!(mode instanceof ToolMode.PlantFood)) {
            return;
        }
        if (blocked()) {
            setMode(NONE);
            return;
        }
        cursor.set(stageX, stageY);
        battlefield.stageToLocalCoordinates(cursor);
        int col = layout.colAt(cursor.x);
        int row = layout.rowAt(cursor.y);
        setMode(NONE);
        if (col >= 0 && row >= 0) {
            host.feedAt(col, row);
        }
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
        placeGhost(cursor.x, cursor.y);
        lawnCursor.set(cursor);
        battlefield.stageToLocalCoordinates(lawnCursor);
        int col = layout.colAt(lawnCursor.x);
        int row = layout.rowAt(lawnCursor.y);
        if (col >= 0 && row >= 0) {
            highlights.show(layout, col, row);
        } else {
            highlights.hide();
        }
    }

    public boolean collectSun(Sun sun) {
        if (sun == null || sun.isExpired() || blocked()) {
            return false;
        }
        return tryCollect(sun.getCol(), sun.getRow());
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
        GameSession session = host.session();
        GroundSeedPacket packet = session == null ? null : session.getGroundSeedPacketAt(col, row);
        if (mode instanceof ToolMode.Seed seed) {
            if (packet != null && !seed.plantName().equals(packet.plantName())) {
                toggleSeed(packet.plantName());
                return true;
            }
            host.plantSeed(seed.plantName(), col, row);
            return true;
        }
        if (mode instanceof ToolMode.Zombie zombie) {
            host.placeZombie(zombie.alias(), col, row);
            return true;
        }
        if (mode instanceof ToolMode.Shovel) {
            host.shovelAt(col, row);
            return true;
        }
        if (mode instanceof ToolMode.PlantFood) {
            host.feedAt(col, row);
            return true;
        }
        if (session != null && session.getVaseAt(col, row) != null) {
            return host.smashVase(col, row);
        }
        if (packet != null) {
            toggleSeed(packet.plantName());
            return true;
        }
        return false;
    }

    private boolean tryCollect(int col, int row) {
        GameSession session = host.session();
        if (session == null) {
            return false;
        }
        for (Sun sun : session.getSunItems()) {
            if (sun != null && !sun.isExpired() && sun.getCol() == col && sun.getRow() == row) {
                return host.collectSunAt(col, row);
            }
        }
        return false;
    }

    private void refreshGhost() {
        plantGhost.setVisible(false);
        toolGhost.setVisible(false);
        if (mode instanceof ToolMode.Seed seed) {
            boolean mint = isMint(seed.plantName());
            EntityAnimationCatalog.ClipSpec spec = mint
                    ? plantClips.clip(seed.plantName(), "loop", "idle")
                    : plantClips.idle(seed.plantName());
            plantGhost.setSize(layout.tileWidth(), layout.tileHeight());
            plantGhost.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
            plantGhost.setClip(spec.path(), spec.clip(), plantClips.scale(seed.plantName()), true);
            PlantSync.applyMagnetGhostVisibility(plantGhost, seed.plantName());
            applyPlantGhostOffset(seed.plantName());
            plantGhost.setVisible(true);
            return;
        }
        if (mode instanceof ToolMode.Zombie zombie) {
            EntityAnimationCatalog.ClipSpec idle = zombieClips.idle(zombie.alias());
            plantGhost.setSize(layout.tileWidth(), layout.tileHeight());
            plantGhost.setAnchor(0.5f, LawnLayout.ZOMBIE_ANCHOR_Y);
            plantGhost.setClip(idle.path(), idle.clip(), LawnLayout.ZOMBIE_SCALE, true);
            plantGhost.setVisible(true);
            return;
        }
        plantGhost.setDrawOffset(0f, 0f);
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
            float anchor = mode instanceof ToolMode.Zombie
                    ? LawnLayout.ZOMBIE_ANCHOR_Y
                    : LawnLayout.PLANT_ANCHOR_Y;
            float yOffset = 0f;
            if (mode instanceof ToolMode.Seed seed && isMint(seed.plantName())) {
                yOffset = layout.tileHeight() * LawnLayout.MINT_Y_OFFSET;
            }
            plantGhost.setPosition(
                    x - plantGhost.getWidth() / 2f,
                    y - plantGhost.getHeight() * anchor + yOffset);
        }
        if (toolGhost.isVisible()) {
            toolGhost.setPosition(x - toolGhost.getWidth() / 2f, y - toolGhost.getHeight() / 2f);
        }
    }

    private boolean blocked() {
        if (blocked != null && blocked.getAsBoolean()) {
            return true;
        }
        GameSession session = host == null ? null : host.session();
        return session == null || session.getMatchResult() != MatchResult.IN_PROGRESS;
    }

    private static boolean isMint(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        return lower.equals("enlighten-mint")
                || lower.equals("appease-mint")
                || lower.equals("arma-mint")
                || lower.equals("bombard-mint")
                || lower.equals("enforce-mint")
                || lower.equals("reinforce-mint")
                || lower.equals("enchant-mint");
    }

    private void applyPlantGhostOffset(String plantName) {
        if (Plant.PEPPER_PULT.equals(plantName)) {
            plantGhost.setDrawOffset(
                    (float) PepperMuzzles.drawX() * layout.tileWidth(),
                    (float) PepperMuzzles.drawY() * layout.tileHeight());
            return;
        }
        plantGhost.setDrawOffset(0f, 0f);
    }
}
