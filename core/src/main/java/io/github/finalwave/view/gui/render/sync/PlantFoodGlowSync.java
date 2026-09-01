package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantFoodFxClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class PlantFoodGlowSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<Plant, PamActor> glows = new IdentityHashMap<>();
    private final Set<Plant> offPlaying = Collections.newSetFromMap(new IdentityHashMap<>());

    public PlantFoodGlowSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session, PlantSync plantSync) {
        if (session == null || session.getBoard() == null || plantSync == null) {
            return;
        }
        GameBoard board = session.getBoard();
        List<Plant> alive = new ArrayList<>();
        for (Plant plant : board.getAllPlants()) {
            if (plant == null || !plant.isAlive() || isMint(plant.getName())) {
                continue;
            }
            alive.add(plant);
        }
        Set<Plant> aliveSet = Collections.newSetFromMap(new IdentityHashMap<>());
        aliveSet.addAll(alive);
        float scale = PlantFoodFxClips.scale(layout);
        for (Plant plant : alive) {
            if (plant.isUsingPlantFood()) {
                PamActor glow = glows.get(plant);
                if (glow == null) {
                    glow = spawnGlow(plant, scale);
                    glows.put(plant, glow);
                }
                offPlaying.remove(plant);
                updateGlow(plant, glow, plantSync, session);
                continue;
            }
            PamActor glow = glows.get(plant);
            if (glow != null && !offPlaying.contains(plant)) {
                startOff(plant, glow, scale, plantSync, session);
            } else if (glow != null) {
                updateGlow(plant, glow, plantSync, session);
            }
        }
        Iterator<Map.Entry<Plant, PamActor>> iterator = glows.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Plant, PamActor> entry = iterator.next();
            Plant plant = entry.getKey();
            if (!aliveSet.contains(plant)) {
                entry.getValue().remove();
                iterator.remove();
                offPlaying.remove(plant);
            }
        }
    }

    public void clear() {
        for (PamActor glow : glows.values()) {
            glow.remove();
        }
        glows.clear();
        offPlaying.clear();
    }

    private PamActor spawnGlow(Plant plant, float scale) {
        PamActor glow = assets.pamActor();
        glow.setTouchable(Touchable.disabled);
        glow.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        glow.setDrawOffset(PlantFoodFxClips.OFFSET_X, PlantFoodFxClips.OFFSET_Y);
        layer.addActor(glow);
        glow.loadPamSync(PlantFoodFxClips.PATH);
        glow.playThen(
                PlantFoodFxClips.PATH,
                PlantFoodFxClips.ON,
                scale,
                PlantFoodFxClips.LOOP,
                true,
                null);
        return glow;
    }

    private void startOff(Plant plant, PamActor glow, float scale, PlantSync plantSync, GameSession session) {
        offPlaying.add(plant);
        PamActor plantActor = plantSync.actorFor(plant);
        glow.setUserObject(glowSortKey(plant, plantActor, session.getBoard(), PlantFoodFxClips.OFF));
        glow.playOnce(PlantFoodFxClips.PATH, PlantFoodFxClips.OFF, scale, () -> removeGlow(plant));
    }

    private void removeGlow(Plant plant) {
        PamActor glow = glows.remove(plant);
        offPlaying.remove(plant);
        if (glow != null) {
            glow.remove();
        }
    }

    private void updateGlow(Plant plant, PamActor glow, PlantSync plantSync, GameSession session) {
        PamActor plantActor = plantSync.actorFor(plant);
        if (plantActor != null) {
            glow.setSize(plantActor.getWidth(), plantActor.getHeight());
            glow.setPosition(plantActor.getX(), plantActor.getY());
            glow.setVisible(plantActor.isVisible());
        } else {
            Vector2 center = layout.cellCenter(plant.getCol(), plant.getRow());
            glow.setSize(layout.tileWidth(), layout.tileHeight());
            glow.setPosition(center.x - glow.getWidth() / 2f, center.y - glow.getHeight() / 2f);
            glow.setVisible(!plant.isCatTransformed() && !isOctopusCovered(plant, session));
        }
        glow.setUserObject(glowSortKey(plant, plantActor, session.getBoard(), glow.clipName()));
    }

    private static int glowSortKey(Plant plant, PamActor plantActor, GameBoard board, String clipName) {
        int plantSort = plantSortKey(plant, plantActor, board);
        if (PlantFoodFxClips.drawsInFront(clipName)) {
            return plantSort + PlantFoodFxClips.SORT_FRONT_OFFSET;
        }
        return plantSort + PlantFoodFxClips.SORT_BEHIND_OFFSET;
    }

    private static int plantSortKey(Plant plant, PamActor plantActor, GameBoard board) {
        if (plantActor != null && plantActor.getUserObject() instanceof Integer sort) {
            return sort;
        }
        int depth = 0;
        if (board != null) {
            if (board.getOverlayPlantAt(plant.getCol(), plant.getRow()) == plant) {
                depth = 1;
            } else if ("Lily Pad".equals(plant.getName())
                    && board.getGroundPlantAt(plant.getCol(), plant.getRow()) == plant) {
                depth = -1;
            }
        }
        return plant.getRow() * 8 + depth;
    }

    private static boolean isOctopusCovered(Plant plant, GameSession session) {
        if (plant == null || session == null) {
            return false;
        }
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (covering != null
                    && covering.isAlive()
                    && covering.getCoveredPlant() == plant
                    && covering.getType() == PlantCovering.Type.OCTOPUS
                    && !covering.isHeld()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMint(String name) {
        return name != null && name.toLowerCase().contains("mint");
    }
}
