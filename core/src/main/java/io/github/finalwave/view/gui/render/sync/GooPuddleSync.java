package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.GooPuddle;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.ProjectileClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class GooPuddleSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final ProjectileClips clips;
    private final Group layer;
    private final Map<String, PamActor> actors = new HashMap<>();

    public GooPuddleSync(GameAssets assets, LawnLayout layout, ProjectileClips clips, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.clips = clips;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null) {
            return;
        }
        Map<String, GooPuddle> live = new HashMap<>();
        for (GooPuddle puddle : session.getGooPuddles()) {
            live.put(key(puddle.col(), puddle.row()), puddle);
            PamActor actor = actors.get(key(puddle.col(), puddle.row()));
            if (actor == null) {
                actor = spawn();
                actors.put(key(puddle.col(), puddle.row()), actor);
            }
            layout(actor, puddle.col(), puddle.row());
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
        actor.setAnchor(0.5f, 0.35f);
        var spec = clips.gooPuddle();
        actor.playThen(spec.path(), "animation", LawnLayout.GRAVE_SCALE, spec.clip(), true, null);
        layer.addActor(actor);
        return actor;
    }

    private void layout(PamActor actor, int col, int row) {
        Vector2 center = layout.cellCenter(col, row);
        float width = layout.tileWidth() * 1.05f;
        float height = layout.tileHeight() * 0.55f;
        actor.setSize(width, height);
        actor.setPosition(center.x - width / 2f, center.y - height * 0.35f);
        actor.setUserObject(row);
    }

    private static String key(int col, int row) {
        return col + ":" + row;
    }
}
