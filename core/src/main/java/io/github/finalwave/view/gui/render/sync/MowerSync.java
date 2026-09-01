package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LawnMower;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;


public final class MowerSync {
    private static final String EGYPT = "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";
    private static final String ICE = "768/FULL/MOWERS/MOWER_ICEAGE/MOWER_ICEAGE.PAM";
    private static final String BEACH = "768/FULL/MOWERS/MOWER_BEACH/MOWER_BEACH.PAM";
    private static final String DARK = "768/FULL/MOWERS/MOWER_DARK/MOWER_DARK.PAM";
    private static final String IDLE = "idle";
    private static final String ATTACK = "attack";

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final ActorRegistry<LawnMower, PamActor> mowers = new ActorRegistry<>();
    private final Set<LawnMower> mowing = Collections.newSetFromMap(new IdentityHashMap<>());
    private String path = EGYPT;
    private float tickFraction;

    public MowerSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        sync(session, 0f);
    }

    public void sync(GameSession session, float tickFraction) {
        this.tickFraction = Math.max(0f, Math.min(1f, tickFraction));
        if (session == null) {
            return;
        }
        if (session.isIZombieActive()) {
            mowers.sync(List.of(), this::spawn, this::update, PamActor::remove);
            return;
        }
        path = pathFor(ChapterId.fromName(session.getChapterId()));
        List<LawnMower> live = new ArrayList<>();
        for (LawnMower mower : session.getLawnMowers()) {
            if (mower != null && (!mower.isUsed() || mower.isActive())) {
                live.add(mower);
            }
        }
        for (LawnMower mower : live) {
            if (mower.isActive() && mowing.add(mower)) {
                assets.audio().playMower();
            } else if (!mower.isActive()) {
                mowing.remove(mower);
            }
        }
        mowing.retainAll(live);
        mowers.sync(live, this::spawn, this::update, PamActor::remove);
    }

    public void clear() {
        mowing.clear();
        mowers.clear(PamActor::remove);
    }

    private PamActor spawn(LawnMower mower) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.MOWER_ANCHOR_Y);
        layer.addActor(actor);
        return actor;
    }

    private void update(LawnMower mower, PamActor actor) {
        float width = layout.tileWidth();
        float height = layout.tileHeight();
        float centerX = layout.worldX(displayX(mower));
        float y = layout.worldYForRow(mower.getRow());
        actor.setSize(width, height);
        actor.setPosition(centerX - width / 2f, y);
        String clip = mower.isActive() ? ATTACK : IDLE;
        if (!clip.equals(actor.clipName())) {
            actor.setClip(path, clip, LawnLayout.MOWER_SCALE, true);
        }
        actor.setUserObject(mower.getRow());
        actor.setVisible(!mower.isUsed() || mower.isActive());
    }

    private double displayX(LawnMower mower) {
        double modelX = mower.getX();
        if (!mower.isActive() || tickFraction <= 0f) {
            return modelX;
        }
        return modelX + LawnMower.SPEED * tickFraction;
    }

    private static String pathFor(ChapterId chapterId) {
        if (chapterId == null) {
            return EGYPT;
        }
        return switch (chapterId) {
            case ANCIENT_EGYPT -> EGYPT;
            case FROSTBITE_CAVES -> ICE;
            case BIG_WAVE_BEACH -> BEACH;
            case DARK_AGES -> DARK;
        };
    }
}
