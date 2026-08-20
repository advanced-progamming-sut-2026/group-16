package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.GraveClips;
import io.github.finalwave.view.gui.widget.HitFlashTracker;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public final class GraveSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<String, PamActor> actors = new HashMap<>();
    private final HitFlashTracker<GraveTile> hits = new HitFlashTracker<>();

    public GraveSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        ChapterId chapterId = ChapterId.fromName(session.getChapterId());
        Map<String, GraveTile> live = new HashMap<>();
        List<GraveTile> tiles = new ArrayList<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (!(tile instanceof GraveTile grave) || grave.isDestroyed()) {
                    continue;
                }
                String cell = key(col, row);
                live.put(cell, grave);
                tiles.add(grave);
                PamActor actor = actors.get(cell);
                if (actor == null) {
                    actor = spawn();
                    actors.put(cell, actor);
                }
                update(grave, actor, col, row, chapterId);
            }
        }
        Iterator<Map.Entry<String, PamActor>> iterator = actors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PamActor> entry = iterator.next();
            if (!live.containsKey(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
        hits.retain(tiles);
    }

    public void clear() {
        for (PamActor actor : actors.values()) {
            actor.remove();
        }
        actors.clear();
        hits.clear();
    }

    private PamActor spawn() {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.GRAVE_ANCHOR_Y);
        layer.addActor(actor);
        return actor;
    }

    private void update(GraveTile grave, PamActor actor, int col, int row, ChapterId chapterId) {
        Vector2 center = layout.cellCenter(col, row);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        String path = GraveClips.pathFor(chapterId, grave.getLoot());
        String clip = GraveClips.clipFor(grave.getHealth(), grave.getMaxHealth());
        actor.setClip(path, clip, LawnLayout.GRAVE_SCALE, true);
        actor.setUserObject(row);
        hits.observe(grave, grave.getHealth(), actor);
    }

    private static String key(int col, int row) {
        return col + ":" + row;
    }
}
