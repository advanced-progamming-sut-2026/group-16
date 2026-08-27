package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.SlipperyTile;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class SlipperyTileSync {
    public static final String UP_PATH = "768/FULL/EFFECTS/TILESLIDER_ICEAGE_UP/TILESLIDER_ICEAGE_UP.PAM";
    public static final String DOWN_PATH = "768/FULL/EFFECTS/TILESLIDER_ICEAGE_DOWN/TILESLIDER_ICEAGE_DOWN.PAM";
    private static final String IDLE = "idle";
    private static final float SCALE = 1.0f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<String, PamActor> actors = new HashMap<>();

    public SlipperyTileSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        Map<String, SlipperyTile> live = new HashMap<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (!(tile instanceof SlipperyTile slippery)) {
                    continue;
                }
                String cell = key(col, row);
                live.put(cell, slippery);
                PamActor actor = actors.get(cell);
                if (actor == null) {
                    actor = spawn(slippery);
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

    private PamActor spawn(SlipperyTile slippery) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.setClip(pathOf(slippery), IDLE, SCALE, true);
        layer.addActor(actor);
        return actor;
    }

    private void layout(PamActor actor, int col, int row) {
        Vector2 center = layout.cellCenter(col, row);
        float width = layout.tileWidth() * 1.12f;
        float height = layout.tileHeight() * 1.12f;
        actor.setSize(width, height);
        actor.setPosition(center.x - width / 2f, center.y - height / 2f);
        actor.setUserObject(row);
    }

    private static String pathOf(SlipperyTile slippery) {
        if (slippery.getDirection() == SlipperyTile.SlipDirection.DOWN) {
            return DOWN_PATH;
        }
        return UP_PATH;
    }

    private static String key(int col, int row) {
        return col + ":" + row;
    }
}
