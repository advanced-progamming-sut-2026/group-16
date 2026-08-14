package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.List;


public final class PlantSync {
    private static final Color ICE_TINT = new Color(0.55f, 0.9f, 1f, 1f);
    private static final Color DISABLED = new Color(0.62f, 0.62f, 0.62f, 1f);

    private final GameAssets assets;
    private final LawnLayout layout;
    private final PlantClips clips;
    private final Group layer;
    private final ActorRegistry<Plant, PamActor> plants = new ActorRegistry<>();
    private final ActorRegistry<Plant, PamActor> iceBlocks = new ActorRegistry<>();

    public PlantSync(GameAssets assets, LawnLayout layout, PlantClips clips, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.clips = clips;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        List<Plant> live = new ArrayList<>();
        List<Plant> frozen = new ArrayList<>();
        for (Plant plant : board.getAllPlants()) {
            if (plant == null || !plant.isAlive()) {
                continue;
            }
            live.add(plant);
            if (freezeLevel(plant, session) >= 2) {
                frozen.add(plant);
            }
        }
        plants.sync(live, this::spawnPlant, (plant, actor) -> updatePlant(plant, actor, board, session), PamActor::remove);
        iceBlocks.sync(frozen, this::spawnIce, this::updateIce, PamActor::remove);
    }

    public void clear() {
        plants.clear(PamActor::remove);
        iceBlocks.clear(PamActor::remove);
    }

    private PamActor spawnPlant(Plant plant) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        layer.addActor(actor);
        return actor;
    }

    private PamActor spawnIce(Plant plant) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.setClip(PlantClips.ICE_BLOCK_PATH, PlantClips.ICE_BLOCK_CLIP, LawnLayout.ICE_BLOCK_SCALE, true);
        layer.addActor(actor);
        return actor;
    }

    private void updatePlant(Plant plant, PamActor actor, GameBoard board, GameSession session) {
        Vector2 center = layout.cellCenter(plant.getCol(), plant.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        var spec = clips.idle(plant.getName());
        actor.setClip(spec.path(), spec.clip(), clips.scale(plant.getName()), true);
        actor.setUserObject(sortKey(plant, board, 0));
        int freeze = freezeLevel(plant, session);
        if (plant.isDisabled() || plant.isCatTransformed()) {
            actor.setTint(DISABLED);
        } else if (freeze == 1) {
            actor.setTint(ICE_TINT);
        } else {
            actor.setTint(Color.WHITE);
        }
    }

    private void updateIce(Plant plant, PamActor actor) {
        Vector2 center = layout.cellCenter(plant.getCol(), plant.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        actor.setClip(PlantClips.ICE_BLOCK_PATH, PlantClips.ICE_BLOCK_CLIP, LawnLayout.ICE_BLOCK_SCALE, true);
        actor.setUserObject(sortKey(plant, null, 2));
    }

    private static int freezeLevel(Plant plant, GameSession session) {
        int stacks = plant.getHostileIceStacks(null);
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (covering != null
                    && covering.isAlive()
                    && covering.getCoveredPlant() == plant
                    && covering.getType() == PlantCovering.Type.HUNTER_ICE) {
                return 2;
            }
        }
        if (stacks >= 2) {
            return 2;
        }
        if (stacks >= 1) {
            return 1;
        }
        return 0;
    }

    private static int sortKey(Plant plant, GameBoard board, int extraDepth) {
        int depth = extraDepth;
        if (extraDepth == 0 && board != null && board.getOverlayPlantAt(plant.getCol(), plant.getRow()) == plant) {
            depth = 1;
        }
        return plant.getRow() * 8 + depth;
    }
}
