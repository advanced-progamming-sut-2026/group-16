package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class DeadLineSync {
    public static final String PAM_PATH = "768/INITIAL/EFFECTS/STAR_OBJECTIVE_FLOWER/STAR_OBJECTIVE_FLOWER.PAM";

    private static final String IDLE = "idle";
    private static final String ZOMBIES = "zombies";
    private static final String ZOMBIES_IDLE = "zombies_idle";
    private static final String FAIL = "fail";
    private static final String FAIL_IDLE = "fail_idle";
    private static final String WIN = "win";
    private static final String WIN_IDLE = "win_idle";
    private static final float SCALE = 1.15f;
    private static final double WARN_DISTANCE = 2.5;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<Integer, PamActor> flowers = new HashMap<>();

    public DeadLineSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public static float resultHoldSeconds(MatchResult result) {
        if (result == MatchResult.LOST) {
            return 2.1f;
        }
        if (result == MatchResult.WON) {
            return 2.5f;
        }
        return 0f;
    }

    public void sync(GameSession session) {
        if (session == null || !session.isDeadLineActive()) {
            clearFlowers();
            return;
        }
        int column = session.getDeadLineColumn();
        Iterator<Map.Entry<Integer, PamActor>> iterator = flowers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, PamActor> entry = iterator.next();
            if (entry.getKey() < 0 || entry.getKey() >= layout.rows()) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
        for (int row = 0; row < layout.rows(); row++) {
            PamActor actor = flowers.get(row);
            if (actor == null) {
                actor = spawn();
                flowers.put(row, actor);
            }
            layoutFlower(actor, column, row);
            applyClip(actor, session, column, row);
        }
    }

    public void clear() {
        clearFlowers();
    }

    private void clearFlowers() {
        for (PamActor actor : flowers.values()) {
            actor.remove();
        }
        flowers.clear();
    }

    private PamActor spawn() {
        assets.pamPlayer().loadSync(PAM_PATH);
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.setClip(PAM_PATH, IDLE, SCALE, true);
        layer.addActor(actor);
        return actor;
    }

    private void layoutFlower(PamActor actor, int column, int row) {
        float width = layout.tileWidth();
        float height = layout.tileHeight();
        float lineX = layout.worldX(column);
        actor.setSize(width, height);
        actor.setPosition(lineX - width / 2f, layout.worldYForRow(row));
        actor.setUserObject(row);
    }

    private void applyClip(PamActor actor, GameSession session, int column, int row) {
        MatchResult result = session.getMatchResult();
        String current = actor.clipName();
        if (result == MatchResult.LOST) {
            playIntroThen(actor, current, FAIL, FAIL_IDLE);
            return;
        }
        if (result == MatchResult.WON) {
            playIntroThen(actor, current, WIN, WIN_IDLE);
            return;
        }
        if (zombieApproaching(session, column, row)) {
            playIntroThen(actor, current, ZOMBIES, ZOMBIES_IDLE);
            return;
        }
        if (!IDLE.equals(current)) {
            actor.setClip(PAM_PATH, IDLE, SCALE, true);
        }
    }

    private void playIntroThen(PamActor actor, String current, String intro, String hold) {
        if (intro.equals(current) || hold.equals(current)) {
            return;
        }
        actor.playThen(PAM_PATH, intro, SCALE, hold, true, null);
    }

    private static boolean zombieApproaching(GameSession session, int column, int row) {
        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive() || zombie.getRow() != row) {
                continue;
            }
            double x = zombie.getX();
            if (x > column && x <= column + WARN_DISTANCE) {
                return true;
            }
        }
        return false;
    }
}
