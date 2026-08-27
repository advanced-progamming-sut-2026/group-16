package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.PianoObstacle;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.HitFlashTracker;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.List;


public final class PianoObstacleSync {
    public static final String PAM_PATH = "768/FULL/ZOMBIE/PIANO/PIANO.PAM";
    public static final String IDLE_CLIP = "idle";
    public static final String PLAY_CLIP = "play";

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final ActorRegistry<PianoObstacle, PamActor> pianos = new ActorRegistry<>();
    private final HitFlashTracker<PianoObstacle> hits = new HitFlashTracker<>();

    public PianoObstacleSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null) {
            return;
        }
        List<PianoObstacle> live = new ArrayList<>();
        for (PianoObstacle obstacle : session.getPianoObstacles()) {
            if (obstacle != null && obstacle.isAlive()) {
                live.add(obstacle);
            }
        }
        pianos.sync(live, this::spawn, this::update, PamActor::remove);
        hits.retain(live);
    }

    public void clear() {
        pianos.clear(PamActor::remove);
        hits.clear();
    }

    private PamActor spawn(PianoObstacle obstacle) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.ZOMBIE_ANCHOR_Y);
        layer.addActor(actor);
        return actor;
    }

    private void update(PianoObstacle obstacle, PamActor actor) {
        float worldX = layout.worldX(obstacle.getX());
        float worldY = layout.worldYForRow(obstacle.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY);
        actor.setDrawOffset(layout.tileWidth() * 0.22f, 0f);
        String clip = obstacle.isPlaying() ? PLAY_CLIP : IDLE_CLIP;
        actor.setClip(PAM_PATH, clip, LawnLayout.ZOMBIE_SCALE, true);
        actor.setUserObject(obstacle.getRow() * 8 - 1);
        hits.observe(obstacle, obstacle.getHealth(), actor);
    }
}
