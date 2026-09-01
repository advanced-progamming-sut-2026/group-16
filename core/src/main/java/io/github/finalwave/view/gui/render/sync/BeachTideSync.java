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
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class BeachTideSync {
    public static final String WATER_SQUARE_PATH = "768/FULL/BACKGROUNDS/WATER_SQUARE/WATER_SQUARE.PAM";
    public static final String WAVE_UPPERLAYER_PATH = "768/FULL/BACKGROUNDS/WAVE_UPPERLAYER/WAVE_UPPERLAYER.PAM";
    public static final String WATER_TIDE_LINE_PATH = "768/FULL/BACKGROUNDS/WATER_TIDE_LINE/WATER_TIDE_LINE.PAM";
    public static final String WATER_FOAM_PATH = "768/FULL/EFFECTS/WATER_FOAM/WATER_FOAM.PAM";

    private static final String WATER_SQUARE_CLIP = "Water";
    private static final String WAVE_CLIP = "water";
    private static final String TIDE_LINE_CLIP = "idle";
    private static final String FOAM_CLIP = "water_foam_right";

    private static final float WATER_SQUARE_NATIVE_W = 175f;
    private static final float WATER_SQUARE_NATIVE_H = 210f;
    private static final float FOAM_NATIVE_W = 264f;
    private static final float FOAM_NATIVE_H = 180f;
    private static final float DESIGN_TILE_W = 81.73f;
    private static final float SAND_INSET = 0.06f;
    private static final Color LOW_BEACH_WATER_TINT = new Color(0.72f, 0.90f, 0.82f, 1f);

    private static final float WAVE_DESIGN_SCALE = 0.75f;
    private static final float TIDE_LINE_DESIGN_SCALE = 0.7f;
    private static final float WAVE_LERP = 0.05f;
    private static final float WAVE_ORIGIN_COLS = 5f;
    private static final float TIDE_LINE_X_NUDGE = -15f;
    private static final float TIDE_LINE_Y_NUDGE = 40f;
    private static final int WAVE_SORT = 900;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group environmentLayer;
    private final Group highlightLayer;
    private final Texture sandTexture;
    private final TextureRegionDrawable sandDrawable;
    private final Map<String, PamActor> waterSquares = new HashMap<>();
    private final Map<String, Image> sandShelves = new HashMap<>();
    private final Map<String, PamActor> foamActors = new HashMap<>();
    private PamActor wave;
    private PamActor tideLine;
    private float waveX = Float.NaN;
    private boolean disposed;

    public BeachTideSync(GameAssets assets, LawnLayout layout, Group environmentLayer, Group highlightLayer) {
        this.assets = assets;
        this.layout = layout;
        this.environmentLayer = environmentLayer;
        this.highlightLayer = highlightLayer;
        sandTexture = createSandShelfTexture();
        sandDrawable = new TextureRegionDrawable(new TextureRegion(sandTexture));
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
        int fromCol = leftmostWaterColumn(board);
        if (fromCol < 0) {
            hide();
            return;
        }
        syncWaterSquares(board);
        syncLowBeachMarks(board);
        layoutWave(fromCol);
        layoutMaxTideLine(board, rules);
    }

    public void clear() {
        hide();
        if (!disposed) {
            sandTexture.dispose();
            disposed = true;
        }
    }

    private void syncWaterSquares(GameBoard board) {
        Map<String, int[]> live = new HashMap<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (tile == null || !tile.isWater()) {
                    continue;
                }
                live.put(key(col, row), new int[]{col, row});
            }
        }
        Iterator<Map.Entry<String, PamActor>> iterator = waterSquares.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PamActor> entry = iterator.next();
            if (!live.containsKey(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
        for (Map.Entry<String, int[]> entry : live.entrySet()) {
            PamActor actor = waterSquares.get(entry.getKey());
            if (actor == null) {
                actor = spawn(environmentLayer);
                waterSquares.put(entry.getKey(), actor);
            }
            int col = entry.getValue()[0];
            int row = entry.getValue()[1];
            boolean lowBeach = board.getTile(col, row) instanceof LowBeachTile;
            layoutWaterSquare(actor, col, row, lowBeach);
        }
    }

    private void syncLowBeachMarks(GameBoard board) {
        Map<String, LowBeachTile> live = new HashMap<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (tile instanceof LowBeachTile lowBeach) {
                    live.put(key(col, row), lowBeach);
                }
            }
        }
        prune(sandShelves, live);
        prune(foamActors, live);
        for (Map.Entry<String, LowBeachTile> entry : live.entrySet()) {
            String cell = entry.getKey();
            LowBeachTile tile = entry.getValue();
            int colon = cell.indexOf(':');
            int col = Integer.parseInt(cell.substring(0, colon));
            int row = Integer.parseInt(cell.substring(colon + 1));
            Image shelf = sandShelves.get(cell);
            if (shelf == null) {
                shelf = new Image(sandDrawable);
                shelf.setTouchable(Touchable.disabled);
                // Above water squares so the wet-sand pad is actually visible.
                highlightLayer.addActor(shelf);
                sandShelves.put(cell, shelf);
            }
            layoutSandShelf(shelf, col, row);
            if (tile.isFlooded()) {
                PamActor foam = foamActors.get(cell);
                if (foam == null) {
                    foam = spawn(highlightLayer);
                    foamActors.put(cell, foam);
                }
                layoutFoam(foam, col, row);
            } else {
                PamActor foam = foamActors.remove(cell);
                if (foam != null) {
                    foam.remove();
                }
            }
        }
    }

    private void layoutSandShelf(Image shelf, int col, int row) {
        Vector2 origin = layout.cellOrigin(col, row);
        float insetX = layout.tileWidth() * SAND_INSET;
        float insetY = layout.tileHeight() * SAND_INSET;
        shelf.setBounds(
                origin.x + insetX,
                origin.y + insetY,
                layout.tileWidth() - insetX * 2f,
                layout.tileHeight() - insetY * 2f);
        shelf.setVisible(true);
        shelf.setUserObject(row);
    }

    private void layoutFoam(PamActor foam, int col, int row) {
        // Same grid placement as FireTileSync / SlipperyTileSync: one PAM centered on the cell.
        Vector2 center = layout.cellCenter(col, row);
        float width = layout.tileWidth() * 1.08f;
        float height = layout.tileHeight() * 1.08f;
        foam.setAnchor(0.5f, 0.5f);
        foam.setSize(width, height);
        foam.setPosition(center.x - width * 0.5f, center.y - height * 0.5f);
        foam.setDrawOffset(0f, 0f);
        float scale = Math.min(width / FOAM_NATIVE_W, height / FOAM_NATIVE_H);
        foam.setClip(WATER_FOAM_PATH, FOAM_CLIP, scale, true);
        foam.setTint(Color.WHITE);
        foam.setUserObject(row);
        foam.setVisible(true);
        foam.toFront();
    }

    private void layoutWaterSquare(PamActor actor, int col, int row, boolean lowBeach) {
        // Exact cell box, same convention as other per-tile PAM overlays.
        Vector2 center = layout.cellCenter(col, row);
        float width = layout.tileWidth();
        float height = layout.tileHeight();
        actor.setAnchor(0.5f, 0.5f);
        actor.setSize(width, height);
        actor.setPosition(center.x - width * 0.5f, center.y - height * 0.5f);
        actor.setDrawOffset(0f, 0f);
        float scale = Math.min(width / WATER_SQUARE_NATIVE_W, height / WATER_SQUARE_NATIVE_H);
        actor.setClip(WATER_SQUARE_PATH, WATER_SQUARE_CLIP, scale, true);
        actor.setTint(lowBeach ? LOW_BEACH_WATER_TINT : Color.WHITE);
        actor.setUserObject(row * 8);
        actor.setVisible(true);
    }

    private void layoutWave(int fromCol) {
        if (wave == null) {
            wave = spawn(environmentLayer);
        }
        float targetX = layout.worldX(fromCol + WAVE_ORIGIN_COLS);
        if (Float.isNaN(waveX)) {
            waveX = targetX;
        } else {
            waveX += (targetX - waveX) * WAVE_LERP;
        }
        Rectangle lawn = layout.lawnBounds();
        float centerY = lawn.y + lawn.height * 0.5f;
        wave.setSize(0f, 0f);
        wave.setPosition(waveX, centerY);
        wave.setAnchor(0.5f, 0f);
        wave.setDrawOffset(0f, 0f);
        wave.setClip(WAVE_UPPERLAYER_PATH, WAVE_CLIP, designScale(WAVE_DESIGN_SCALE), true);
        wave.setUserObject(WAVE_SORT);
        wave.setVisible(true);
        wave.toFront();
    }

    private void layoutMaxTideLine(GameBoard board, ChapterRules rules) {
        if (tideLine == null) {
            tideLine = spawn(highlightLayer);
        }
        int maxCount = Math.min(Math.max(0, rules.getMaxTideColumn()), board.getCols());
        int fromCol = board.getCols() - maxCount;
        Rectangle lawn = layout.lawnBounds();
        float bg = backgroundScale();
        float x = layout.worldX(fromCol) + layout.tileWidth() * 0.5f + TIDE_LINE_X_NUDGE * bg;
        float y = lawn.y + lawn.height * 0.5f + TIDE_LINE_Y_NUDGE * bg;
        tideLine.setSize(0f, 0f);
        tideLine.setPosition(x, y);
        tideLine.setAnchor(0.5f, 0f);
        tideLine.setDrawOffset(0f, 0f);
        tideLine.setClip(WATER_TIDE_LINE_PATH, TIDE_LINE_CLIP, designScale(TIDE_LINE_DESIGN_SCALE), true);
        tideLine.setVisible(true);
    }

    private float backgroundScale() {
        return layout.tileWidth() / DESIGN_TILE_W;
    }

    private float designScale(float designSpaceScale) {
        return designSpaceScale * backgroundScale();
    }

    private void hide() {
        for (PamActor actor : waterSquares.values()) {
            actor.remove();
        }
        waterSquares.clear();
        for (Image shelf : sandShelves.values()) {
            shelf.remove();
        }
        sandShelves.clear();
        for (PamActor foam : foamActors.values()) {
            foam.remove();
        }
        foamActors.clear();
        if (wave != null) {
            wave.remove();
            wave = null;
        }
        if (tideLine != null) {
            tideLine.remove();
            tideLine = null;
        }
        waveX = Float.NaN;
    }

    private PamActor spawn(Group layer) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setVisible(false);
        layer.addActor(actor);
        return actor;
    }

    private static <T> void prune(Map<String, T> actors, Map<String, ?> live) {
        Iterator<Map.Entry<String, T>> iterator = actors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, T> entry = iterator.next();
            if (live.containsKey(entry.getKey())) {
                continue;
            }
            Object actor = entry.getValue();
            if (actor instanceof Image image) {
                image.remove();
            } else if (actor instanceof PamActor pam) {
                pam.remove();
            }
            iterator.remove();
        }
    }

    private static Texture createSandShelfTexture() {
        int width = 128;
        int height = 128;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        float cx = width * 0.5f;
        float cy = height * 0.5f;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float nx = (x + 0.5f - cx) / (width * 0.48f);
                float ny = (y + 0.5f - cy) / (height * 0.48f);
                float r = (float) Math.sqrt(nx * nx + ny * ny);
                if (r > 1f) {
                    continue;
                }
                float edge = 1f - r;
                float alpha = 0.22f + 0.38f * edge * edge;
                float grain = 0.5f + 0.5f * (float) Math.sin((x * 0.31f) + (y * 0.27f));
                Color c = new Color(0.91f, 0.80f, 0.52f, alpha);
                c.lerp(new Color(0.70f, 0.58f, 0.34f, alpha), grain * 0.28f);
                pixmap.drawPixel(x, y, Color.rgba8888(c));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
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
