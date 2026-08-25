package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ZombossClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class IceTileSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<String, PamActor> actors = new HashMap<>();

    public IceTileSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        Map<String, IceTile> live = new HashMap<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (!(tile instanceof IceTile ice) || ice.isDestroyed()) {
                    continue;
                }
                String cell = key(col, row);
                live.put(cell, ice);
                PamActor actor = actors.get(cell);
                if (actor == null) {
                    actor = spawn();
                    actors.put(cell, actor);
                }
                layout(actor, col, row);
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
    }

    public void clear() {
        for (PamActor actor : actors.values()) {
            actor.remove();
        }
        actors.clear();
    }

    private PamActor spawn() {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.GRAVE_ANCHOR_Y);
        layer.addActor(actor);
        return actor;
    }

    private void layout(PamActor actor, int col, int row) {
        Vector2 center = layout.cellCenter(col, row);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        actor.setClip(ZombossClips.GLACIER, "idle", LawnLayout.ICE_BLOCK_SCALE, true);
        actor.setUserObject(row);
    }

    private static String key(int col, int row) {
        return col + ":" + row;
    }
}
