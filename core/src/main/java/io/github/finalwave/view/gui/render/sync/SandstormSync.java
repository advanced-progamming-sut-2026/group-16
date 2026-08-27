package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;


public final class SandstormSync {
    public static final String REAR_PATH = "768/INITIAL/EFFECTS/SANDSTORM_REAR/SANDSTORM_REAR.PAM";
    public static final String TOP_PATH = "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";
    private static final String INTRO = "intro";
    private static final String OUTRO = "outro";
    private static final float SCALE = 1.1f;
    private static final float WIDTH_TILES = 1.2f;
    private static final float HEIGHT_TILES = 2.2f;
    private static final float ANCHOR_Y = 0.22f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group rearLayer;
    private final Group topLayer;
    private final Set<Zombie> shown = Collections.newSetFromMap(new IdentityHashMap<>());

    public SandstormSync(GameAssets assets, LawnLayout layout, Group rearLayer, Group topLayer) {
        this.assets = assets;
        this.layout = layout;
        this.rearLayer = rearLayer;
        this.topLayer = topLayer;
    }

    public void sync(GameSession session) {
        if (session == null) {
            return;
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive() || !zombie.isSandstormSpawn()) {
                continue;
            }
            if (!shown.add(zombie)) {
                continue;
            }
            burst(zombie);
        }
        prune(session);
    }

    public void clear() {
        shown.clear();
    }

    private void burst(Zombie zombie) {
        int col = Math.max(0, (int) Math.floor(zombie.getX()));
        int row = zombie.getRow();
        Vector2 center = layout.cellCenter(col, row);
        play(rearLayer, REAR_PATH, center, row);
        play(topLayer, TOP_PATH, center, row);
    }

    private void play(Group layer, String path, Vector2 center, int row) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, ANCHOR_Y);
        float width = layout.tileWidth() * WIDTH_TILES;
        float height = layout.tileHeight() * HEIGHT_TILES;
        actor.setSize(width, height);
        actor.setPosition(center.x - width / 2f, center.y - height * ANCHOR_Y);
        actor.setUserObject(row);
        layer.addActor(actor);
        actor.playOnce(path, INTRO, SCALE, () -> actor.playOnce(path, OUTRO, SCALE, actor::remove));
    }

    private void prune(GameSession session) {
        Iterator<Zombie> iterator = shown.iterator();
        while (iterator.hasNext()) {
            Zombie zombie = iterator.next();
            if (zombie == null || !zombie.isAlive() || !session.getZombies().contains(zombie)) {
                iterator.remove();
            }
        }
    }
}
