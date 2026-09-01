package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.NecromancyTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.view.gui.render.LawnLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class NecromancyTileSync {
    private static final Color MARK_COLOR = new Color(0.52f, 0.18f, 0.72f, 0.34f);

    private final LawnLayout layout;
    private final Group layer;
    private final TextureRegionDrawable drawable;
    private final Texture white;
    private final Map<String, Image> marks = new HashMap<>();
    private boolean disposed;

    public NecromancyTileSync(LawnLayout layout, Group layer) {
        this.layout = layout;
        this.layer = layer;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        white = new Texture(pixmap);
        pixmap.dispose();
        drawable = new TextureRegionDrawable(new TextureRegion(white));
    }

    public void sync(GameSession session) {
        if (disposed || session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        Map<String, int[]> live = new HashMap<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (tile instanceof NecromancyTile necromancy && !necromancy.isDestroyed()) {
                    live.put(key(col, row), new int[]{col, row});
                }
            }
        }
        Iterator<Map.Entry<String, Image>> iterator = marks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Image> entry = iterator.next();
            if (!live.containsKey(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
        for (Map.Entry<String, int[]> entry : live.entrySet()) {
            Image mark = marks.get(entry.getKey());
            if (mark == null) {
                mark = new Image(drawable);
                mark.setTouchable(Touchable.disabled);
                mark.setColor(MARK_COLOR);
                layer.addActor(mark);
                marks.put(entry.getKey(), mark);
            }
            layoutMark(mark, entry.getValue()[0], entry.getValue()[1]);
        }
    }

    public void clear() {
        for (Image mark : marks.values()) {
            mark.remove();
        }
        marks.clear();
        if (!disposed) {
            white.dispose();
            disposed = true;
        }
    }

    private void layoutMark(Image mark, int col, int row) {
        Vector2 center = layout.cellCenter(col, row);
        float width = layout.tileWidth() * 0.92f;
        float height = layout.tileHeight() * 0.28f;
        mark.setSize(width, height);
        mark.setPosition(center.x - width * 0.5f, center.y - height * 0.62f);
        mark.setUserObject(row);
    }

    private static String key(int col, int row) {
        return col + ":" + row;
    }
}
