package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.CraterFadeMark;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ScorchedEarthClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class ScorchedEarthSync {
    private enum Phase {
        ACTIVE,
        FADING
    }

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<String, PamActor> actors = new HashMap<>();
    private final Map<String, Phase> phases = new HashMap<>();

    public ScorchedEarthSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        Set<String> liveCraters = new HashSet<>();
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Tile tile = board.getTile(col, row);
                if (tile != null && tile.isCrater()) {
                    liveCraters.add(key(col, row));
                    ensureActive(col, row);
                }
            }
        }
        if (session.getCraterSystem() != null) {
            List<CraterFadeMark> fades = session.getCraterSystem().drainFadeMarks();
            for (CraterFadeMark mark : fades) {
                beginFade(mark.col(), mark.row());
            }
        }
        Iterator<Map.Entry<String, PamActor>> iterator = actors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PamActor> entry = iterator.next();
            String cell = entry.getKey();
            Phase phase = phases.getOrDefault(cell, Phase.ACTIVE);
            if (phase == Phase.FADING) {
                continue;
            }
            if (!liveCraters.contains(cell)) {
                entry.getValue().remove();
                iterator.remove();
                phases.remove(cell);
            }
        }
    }

    public void clear() {
        for (PamActor actor : actors.values()) {
            actor.remove();
        }
        actors.clear();
        phases.clear();
    }

    private void ensureActive(int col, int row) {
        String cell = key(col, row);
        if (phases.getOrDefault(cell, Phase.ACTIVE) == Phase.FADING) {
            return;
        }
        PamActor actor = actors.get(cell);
        if (actor != null) {
            return;
        }
        spawnAppear(col, row);
    }

    private void beginFade(int col, int row) {
        String cell = key(col, row);
        PamActor actor = actors.get(cell);
        if (actor == null) {
            return;
        }
        phases.put(cell, Phase.FADING);
        float scale = LawnLayout.SCORCHED_EARTH_SCALE;
        var exit = ScorchedEarthClips.tileExit();
        actor.playOnce(exit.path(), exit.clip(), scale, () -> {
            actor.remove();
            actors.remove(cell);
            phases.remove(cell);
        });
    }

    private void spawnAppear(int col, int row) {
        String cell = key(col, row);
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        Vector2 center = layout.cellCenter(col, row);
        float yOffset = LawnLayout.SCORCHED_EARTH_Y_OFFSET;
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f + yOffset);
        layer.addActor(actor);
        actors.put(cell, actor);
        phases.put(cell, Phase.ACTIVE);
        float scale = LawnLayout.SCORCHED_EARTH_SCALE;
        var appear = col == 0 ? ScorchedEarthClips.edgeAppear() : ScorchedEarthClips.tileAppear();
        var idle = col == 0 ? ScorchedEarthClips.edgeIdle() : ScorchedEarthClips.tileIdle();
        actor.loadPamSync(appear.path());
        actor.playOnce(appear.path(), appear.clip(), scale,
                () -> actor.setClip(idle.path(), idle.clip(), scale, true));
    }

    private static String key(int col, int row) {
        return col + "," + row;
    }
}
