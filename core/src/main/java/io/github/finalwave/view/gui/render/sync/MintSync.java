package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class MintSync {
    private static final String INTRO = "intro";
    private static final String LOOP = "loop";
    private static final String OUTRO = "outro";
    private static final float SCALE = LawnLayout.PLANT_SCALE;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final EntityAnimationCatalog catalog;
    private final Map<String, MintInstance> instances = new HashMap<>();
    private final Map<MintInstance, PamActor> actors = new HashMap<>();

    public MintSync(GameAssets assets, LawnLayout layout, Group layer, EntityAnimationCatalog catalog) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
        this.catalog = catalog;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        Map<String, Plant> currentByTile = new HashMap<>();
        for (Plant plant : board.getAllPlants()) {
            if (plant == null || !plant.isAlive()) {
                continue;
            }
            String name = plant.getName();
            if (name == null || !isMint(name)) {
                continue;
            }
            String key = tileKey(plant.getCol(), plant.getRow());
            currentByTile.put(key, plant);
        }
        for (Map.Entry<String, Plant> entry : currentByTile.entrySet()) {
            String key = entry.getKey();
            Plant plant = entry.getValue();
            MintInstance existing = instances.get(key);
            if (existing != null && existing.plantRef == plant) {
                continue;
            }
            if (existing != null) {
                PamActor old = actors.remove(existing);
                if (old != null) {
                    old.remove();
                }
                instances.remove(key);
            }
            MintInstance created = createInstance(plant, session);
            PamActor actor = spawn(created);
            instances.put(key, created);
            actors.put(created, actor);
            layout(actor, created);
        }
        int now = session.getCurrentTick();
        Iterator<Map.Entry<String, MintInstance>> it = new ArrayList<>(instances.entrySet()).iterator();
        while (it.hasNext()) {
            Map.Entry<String, MintInstance> e = it.next();
            MintInstance instance = e.getValue();
            String key = e.getKey();
            PamActor actor = actors.get(instance);
            if (actor == null) {
                continue;
            }
            boolean plantAlive = currentByTile.containsKey(key) && currentByTile.get(key) == instance.plantRef;
            layout(actor, instance);
            if (instance.outroStartTick < 0) {
                int elapsed = now - instance.startTick;
                if (!plantAlive || elapsed >= instance.loopEndTick) {
                    beginOutro(instance, actor, now);
                }
                continue;
            }
            int outroElapsed = now - instance.outroStartTick;
            if (outroElapsed < instance.outroTicks) {
                continue;
            }
            if (!plantAlive) {
                actor.remove();
                actors.remove(instance);
                it.remove();
                instances.remove(key);
                continue;
            }
            actor.setPlaying(false);
        }
    }

    public void clear() {
        for (PamActor actor : actors.values()) {
            actor.remove();
        }
        actors.clear();
        instances.clear();
    }

    private void beginOutro(MintInstance instance, PamActor actor, int now) {
        instance.outroStartTick = now;
        actor.setTimeScale(1f);
        actor.setPlaying(true);
        actor.setClip(instance.path, OUTRO, SCALE, false);
        actor.setStateTime(0f);
    }

    private MintInstance createInstance(Plant plant, GameSession session) {
        int col = plant.getCol();
        int row = plant.getRow();
        int start = session.getCurrentTick();
        String path = catalog.plantIdle(plant.getName()).path();
        if (path == null || path.isBlank()) {
            path = catalog.plantClip(plant.getName(), INTRO, LOOP, OUTRO).path();
        }
        float introSec = safeDuration(path, INTRO, 1.5f);
        float outroSec = safeDuration(path, OUTRO, 1.5f);
        int introTicks = Math.max(1, (int) Math.round(introSec * GameSession.TICKS_PER_SECOND));
        int outroTicks = Math.max(1, (int) Math.round(outroSec * GameSession.TICKS_PER_SECOND));
        double base = 10.0;
        double ext = 0.0;
        try {
            ext = plant.getStats().specialModifier("DURATION_EXT");
        } catch (RuntimeException ignored) {
        }
        int boostTicks = Math.max(1, (int) Math.ceil((base + ext) * GameSession.TICKS_PER_SECOND));
        MintInstance instance = new MintInstance();
        instance.plantName = plant.getName();
        instance.col = col;
        instance.row = row;
        instance.startTick = start;
        instance.introTicks = introTicks;
        instance.outroTicks = outroTicks;
        instance.loopEndTick = introTicks + boostTicks;
        instance.path = path;
        instance.plantRef = plant;
        instance.outroStartTick = -1;
        return instance;
    }

    private PamActor spawn(MintInstance instance) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.setTimeScale(1f);
        actor.playThen(instance.path, INTRO, SCALE, LOOP, true, null);
        layer.addActor(actor);
        return actor;
    }

    private void layout(PamActor actor, MintInstance instance) {
        Vector2 center = layout.cellCenter(instance.col, instance.row);
        float yOffset = layout.tileHeight() * LawnLayout.MINT_Y_OFFSET;
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f + yOffset);
        actor.setUserObject(instance.row * 8 + 1);
    }

    private float safeDuration(String path, String clip, float fallback) {
        try {
            return assets.pamPlayer().clipDurationSeconds(path, clip);
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static boolean isMint(String name) {
        String lower = name.toLowerCase();
        return lower.equals("enlighten-mint")
                || lower.equals("appease-mint")
                || lower.equals("arma-mint")
                || lower.equals("bombard-mint")
                || lower.equals("enforce-mint")
                || lower.equals("reinforce-mint")
                || lower.equals("enchant-mint");
    }

    private static String tileKey(int col, int row) {
        return col + ":" + row;
    }

    private static final class MintInstance {
        String plantName;
        int col;
        int row;
        int startTick;
        int introTicks;
        int outroTicks;
        int loopEndTick;
        int outroStartTick = -1;
        String path;
        Plant plantRef;
    }
}
