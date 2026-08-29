package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class GraveBusterDirtSync {
    private static final float SCALE = LawnLayout.PLANT_SCALE;
    private static final float DIRT_SCALE = 1.0f;
    private static final String FADE_CLIP = PlantClips.GRAVE_BUSTER_DIRT_FADE;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<Plant, PamActor> actors = new HashMap<>();
    private final Map<String, PamActor> fades = new HashMap<>();
    private final Map<String, Integer> fadeExpiry = new HashMap<>();

    public GraveBusterDirtSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        Map<Plant, Plant> live = new HashMap<>();
        for (Plant plant : board.getAllPlants()) {
            if (plant == null || !plant.isAlive() || !"Grave Buster".equals(plant.getName())
                    || (!plant.isAttacking() && !plant.isGraveBusting())) {
                continue;
            }
            live.put(plant, plant);
            PamActor actor = actors.get(plant);
            if (actor == null) {
                actor = spawn();
                actors.put(plant, actor);
            }
            layout(actor, plant);
        }
        Iterator<Map.Entry<Plant, PamActor>> iterator = actors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Plant, PamActor> entry = iterator.next();
            if (!live.containsKey(entry.getKey())) {
                Plant plant = entry.getKey();
                entry.getValue().remove();
                iterator.remove();
                String key = plant.getCol() + ":" + plant.getRow();
                if (!fades.containsKey(key)) {
                    PamActor fade = spawnFade(plant.getCol(), plant.getRow());
                    fades.put(key, fade);
                    int durationTicks = Math.max(4, (int) Math.round(0.34f * 10f));
                    fadeExpiry.put(key, session.getCurrentTick() + durationTicks);
                }
            }
        }
        int now = session.getCurrentTick();
        Iterator<Map.Entry<String, PamActor>> fadeIt = fades.entrySet().iterator();
        while (fadeIt.hasNext()) {
            Map.Entry<String, PamActor> entry = fadeIt.next();
            String key = entry.getKey();
            Integer expiry = fadeExpiry.get(key);
            if (expiry != null && now >= expiry) {
                entry.getValue().remove();
                fadeIt.remove();
                fadeExpiry.remove(key);
            }
        }
    }

    public void clear() {
        for (PamActor actor : actors.values()) {
            actor.remove();
        }
        actors.clear();
        for (PamActor actor : fades.values()) {
            actor.remove();
        }
        fades.clear();
        fadeExpiry.clear();
    }

    private PamActor spawn() {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.setDrawPart(PlantClips.GRAVE_BUSTER_DIRT_PART);
        actor.setDrawOffset(0f, 0f);
        actor.setClip(PlantClips.GRAVE_BUSTER_PATH, PlantClips.GRAVE_BUSTER_ATTACK, SCALE, true);
        layer.addActor(actor);
        return actor;
    }

    private PamActor spawnFade(int col, int row) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.GRAVE_ANCHOR_Y);
        Vector2 center = layout.cellCenter(col, row);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        actor.setUserObject(row * 8 + 1);
        actor.setClip(PlantClips.GRAVE_BUSTER_DIRT_PATH, FADE_CLIP, DIRT_SCALE, false);
        layer.addActor(actor);
        return actor;
    }

    private void layout(PamActor actor, Plant plant) {
        Vector2 center = layout.cellCenter(plant.getCol(), plant.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        actor.setUserObject(plant.getRow() * 8 + 1);
        actor.setDrawPart(PlantClips.GRAVE_BUSTER_DIRT_PART);
        actor.setDrawOffset(0f, 0f);
        actor.clearGroundClip();
        actor.setVisibility(null);
        actor.setTimeScale(1f);
        String clip = plant.isGraveBusting() ? PlantClips.GRAVE_BUSTER_EAT : PlantClips.GRAVE_BUSTER_ATTACK;
        if (!clip.equals(actor.clipName())) {
            actor.setClip(PlantClips.GRAVE_BUSTER_PATH, clip, SCALE, true);
        } else {
            actor.setDrawScale(SCALE);
        }
    }
}
