package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public final class SunSync {
    public static final String SUN_PATH = "768/INITIAL/EFFECTS/SUN/SUN.PAM";

    private static final Color RADIOACTIVE = new Color(0.72f, 0.42f, 1f, 1f);

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Consumer<Sun> onCollect;
    private final ActorRegistry<Sun, PamActor> suns = new ActorRegistry<>();
    private float tickFraction;

    public SunSync(GameAssets assets, LawnLayout layout, Group layer, Consumer<Sun> onCollect) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
        this.onCollect = onCollect;
    }

    public void sync(GameSession session, float tickFraction) {
        this.tickFraction = Math.max(0f, Math.min(1f, tickFraction));
        if (session == null) {
            return;
        }
        List<Sun> live = new ArrayList<>();
        for (Sun sun : session.getSunItems()) {
            if (sun != null && !sun.isExpired()) {
                live.add(sun);
            }
        }
        suns.sync(live, this::spawn, this::update, PamActor::remove);
    }

    public void clear() {
        suns.clear(PamActor::remove);
    }

    private PamActor spawn(Sun sun) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.enabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (onCollect != null) {
                    onCollect.accept(sun);
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (onCollect != null) {
                    onCollect.accept(sun);
                }
                return true;
            }
        });
        layer.addActor(actor);
        return actor;
    }

    private void update(Sun sun, PamActor actor) {
        float size = sun.getType() == SunType.SPECIAL ? 110f : 80f;
        actor.setSize(size, size);
        Vector2 cell = layout.cellCenter(sun.getCol(), sun.getRow());
        actor.setPosition(cell.x - size / 2f, fallY(sun, cell.y) - size / 2f);
        actor.setClip(SUN_PATH, clipOf(sun.getType()), sun.getType() == SunType.SPECIAL ? 0.72f : 0.55f, true);
        actor.setTint(sun.getType() == SunType.RADIOACTIVE ? RADIOACTIVE : Color.WHITE);
        actor.setUserObject(sun.getRow());
    }

    private float fallY(Sun sun, float groundY) {
        if (!sun.isFalling()) {
            return groundY;
        }
        float remaining = sun.getFallTicksRemaining() - tickFraction;
        float t = Math.max(0f, remaining) / Sun.FALL_TICKS;
        float skyY = layout.originY() + layout.rows() * layout.tileHeight() + 180f;
        return groundY + (skyY - groundY) * t;
    }

    private static String clipOf(SunType type) {
        if (type == SunType.SPECIAL) {
            return "red";
        }
        if (type == SunType.RADIOACTIVE) {
            return "blue";
        }
        return "animation";
    }
}
