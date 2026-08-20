package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.ArcadeObstacle;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.HitFlashTracker;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.List;


public final class ArcadeObstacleSync {
    public static final String PAM_PATH = "768/FULL/EFFECTS/80S_ARCADE_CABINET/80S_ARCADE_CABINET.PAM";
    public static final String IDLE_CLIP = "idle";

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final ActorRegistry<ArcadeObstacle, PamActor> cabinets = new ActorRegistry<>();
    private final HitFlashTracker<ArcadeObstacle> hits = new HitFlashTracker<>();

    public ArcadeObstacleSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null) {
            return;
        }
        List<ArcadeObstacle> live = new ArrayList<>();
        for (ArcadeObstacle obstacle : session.getArcadeObstacles()) {
            if (obstacle != null && obstacle.isAlive()) {
                live.add(obstacle);
            }
        }
        cabinets.sync(live, this::spawn, this::update, PamActor::remove);
        hits.retain(live);
    }

    public void clear() {
        cabinets.clear(PamActor::remove);
        hits.clear();
    }

    private PamActor spawn(ArcadeObstacle obstacle) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.ZOMBIE_ANCHOR_Y);
        layer.addActor(actor);
        return actor;
    }

    private void update(ArcadeObstacle obstacle, PamActor actor) {
        float worldX = layout.worldX(obstacle.getX());
        float worldY = layout.worldYForRow(obstacle.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY);
        actor.setClip(PAM_PATH, IDLE_CLIP, LawnLayout.ZOMBIE_SCALE, true);
        actor.setUserObject(obstacle.getRow() * 8 + 1);
        hits.observe(obstacle, obstacle.getHealth(), actor);
    }
}
