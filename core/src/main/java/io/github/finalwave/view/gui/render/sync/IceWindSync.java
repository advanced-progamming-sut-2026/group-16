package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ZombossClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashSet;
import java.util.Set;

public final class IceWindSync {
    private static final float WIND_SCALE = 1.35f;
    private static final float WIND_SECONDS = 2.5f;
    private static final float WIND_WIDTH_TILES = 3.6f;
    private static final String CLIP = "animation";

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Set<Integer> shown = new HashSet<>();

    public IceWindSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        int rows = session.getBoard().getRows();
        for (int row = 0; row < rows; row++) {
            boolean active = session.isRowEffectActive(row, GameSession.ROW_EFFECT_ICE_WIND);
            if (active) {
                if (shown.add(row)) {
                    spawnWind(row);
                }
            } else {
                shown.remove(row);
            }
        }
        shown.removeIf(row -> row < 0 || row >= rows);
    }

    public void clear() {
        shown.clear();
    }

    private void spawnWind(int row) {
        Vector2 right = layout.cellCenter(Math.max(0, layout.cols() - 1), row);
        Vector2 left = layout.cellCenter(0, row);
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.setSize(layout.tileWidth() * WIND_WIDTH_TILES, layout.tileHeight() * 1.5f);
        actor.setClip(ZombossClips.CHILL_WIND, CLIP, WIND_SCALE, true);
        float y = right.y - actor.getHeight() / 2f;
        float startX = right.x + layout.tileWidth();
        float endX = left.x - layout.tileWidth() * 2f;
        actor.setPosition(startX - actor.getWidth() / 2f, y);
        actor.setUserObject(row);
        layer.addActor(actor);
        actor.addAction(Actions.sequence(
                Actions.moveTo(endX - actor.getWidth() / 2f, y, WIND_SECONDS, Interpolation.linear),
                Actions.run(actor::remove)));
    }
}
