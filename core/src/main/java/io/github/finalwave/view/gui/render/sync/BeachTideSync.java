package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.ChapterRules;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.LowBeachTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.view.gui.render.LawnLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class BeachTideSync {
    private static final float LINE_WIDTH = 6f;
    private static final float SAND_HEIGHT = 0.22f;
    private static final Color WATER_COLOR = new Color(0.16f, 0.48f, 0.86f, 0.45f);
    private static final Color SAND_COLOR = new Color(0.86f, 0.70f, 0.38f, 0.92f);
    private static final Color LINE_COLOR = new Color(0.82f, 0.96f, 0.90f, 0.88f);

    private final LawnLayout layout;
    private final Group environmentLayer;
    private final Group highlightLayer;
    private final Texture white;
    private final TextureRegionDrawable drawable;
    private final Image water;
    private final Image line;
    private final Map<String, Image> sandMarks = new HashMap<>();
    private boolean disposed;

    public BeachTideSync(LawnLayout layout, Group environmentLayer, Group highlightLayer) {
        this.layout = layout;
        this.environmentLayer = environmentLayer;
        this.highlightLayer = highlightLayer;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        white = new Texture(pixmap);
        pixmap.dispose();
        drawable = new TextureRegionDrawable(new TextureRegion(white));
        water = tinted(WATER_COLOR);
        environmentLayer.addActor(water);
        line = tinted(LINE_COLOR);
        highlightLayer.addActor(line);
    }

    public void sync(GameSession session) {
        if (disposed) {
            return;
        }
        ChapterRules rules = beachRules(session);
        if (session == null || session.getBoard() == null || rules == null || !rules.hasWaterColumns()) {
            hide();
            return;
        }
        GameBoard board = session.getBoard();
        layoutWater(board);
        syncSandMarks(board);
        layoutMaxTideLine(board, rules);
        water.toFront();
    }

    public void clear() {
        hide();
        water.remove();
        line.remove();
        if (!disposed) {
            white.dispose();
            disposed = true;
        }
    }

    private void layoutWater(GameBoard board) {
        int fromCol = leftmostWaterColumn(board);
        if (fromCol < 0) {
            water.setVisible(false);
            return;
        }
        Rectangle lawn = layout.lawnBounds();
        float left = layout.cellOrigin(fromCol, 0).x;
        water.setBounds(left, lawn.y, lawn.x + lawn.width - left, lawn.height);
        water.setVisible(true);
    }

    private void syncSandMarks(GameBoard board) {
        Map<String, int[]> live = new HashMap<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (!(tile instanceof LowBeachTile)) {
                    continue;
                }
                live.put(key(col, row), new int[]{col, row});
            }
        }
        Iterator<Map.Entry<String, Image>> iterator = sandMarks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Image> entry = iterator.next();
            if (!live.containsKey(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
        for (Map.Entry<String, int[]> entry : live.entrySet()) {
            Image mark = sandMarks.get(entry.getKey());
            if (mark == null) {
                mark = tinted(SAND_COLOR);
                environmentLayer.addActor(mark);
                sandMarks.put(entry.getKey(), mark);
            }
            layoutSand(mark, entry.getValue()[0], entry.getValue()[1]);
        }
    }

    private void layoutSand(Image mark, int col, int row) {
        Vector2 origin = layout.cellOrigin(col, row);
        float height = layout.tileHeight() * SAND_HEIGHT;
        mark.setBounds(origin.x, origin.y, layout.tileWidth(), height);
        mark.setVisible(true);
        mark.setUserObject(row);
    }

    private void layoutMaxTideLine(GameBoard board, ChapterRules rules) {
        int maxCount = Math.min(Math.max(0, rules.getMaxTideColumn()), board.getCols());
        int fromCol = board.getCols() - maxCount;
        Rectangle lawn = layout.lawnBounds();
        float x = layout.worldX(fromCol);
        line.setBounds(x - LINE_WIDTH / 2f, lawn.y, LINE_WIDTH, lawn.height);
        line.setVisible(true);
    }

    private void hide() {
        water.setVisible(false);
        line.setVisible(false);
        for (Image mark : sandMarks.values()) {
            mark.remove();
        }
        sandMarks.clear();
    }

    private Image tinted(Color color) {
        Image image = new Image(drawable);
        image.setColor(color);
        image.setTouchable(Touchable.disabled);
        image.setVisible(false);
        return image;
    }

    private static int leftmostWaterColumn(GameBoard board) {
        int fromCol = -1;
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (tile == null || !tile.isWater()) {
                    continue;
                }
                if (fromCol < 0 || col < fromCol) {
                    fromCol = col;
                }
            }
        }
        return fromCol;
    }

    private static ChapterRules beachRules(GameSession session) {
        if (session == null) {
            return null;
        }
        ChapterId id = ChapterId.fromName(session.getChapterId());
        if (id != ChapterId.BIG_WAVE_BEACH) {
            return null;
        }
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(id);
        return chapter == null ? null : chapter.getRules();
    }

    private static String key(int col, int row) {
        return col + ":" + row;
    }
}
