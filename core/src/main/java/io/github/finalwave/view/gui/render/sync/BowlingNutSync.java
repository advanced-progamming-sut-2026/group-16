package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.bowling.BowlingNut;
import io.github.finalwave.model.minigame.bowling.BowlingNutSystem;
import io.github.finalwave.model.minigame.bowling.BowlingNutType;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


public final class BowlingNutSync {
    public static final String WALLNUT_PAM = "768/INITIAL/PLANT/WALLNUT/WALLNUT.PAM";
    public static final String EXPLODEONUT_PAM = "768/INITIAL/PLANT/EXPLODEONUT/EXPLODEONUT.PAM";
    public static final String TALLNUT_PAM = "768/FULL/PLANT/TALLNUT/TALLNUT.PAM";
    public static final String IDLE_CLIP = "idle";

    private static final Color EXPLOSIVE_TINT = new Color(1f, 0.55f, 0.42f, 1f);
    private static final float ROLL_RADIUS_FACTOR = 0.45f;
    private static final float ROLL_PIVOT_Y = 32f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final ActorRegistry<BowlingNut, PamActor> nuts = new ActorRegistry<>();
    private final Map<PamActor, Roll> rolls = new IdentityHashMap<>();
    private float tickFraction;

    public BowlingNutSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session, float tickFraction) {
        this.tickFraction = Math.max(0f, Math.min(1f, tickFraction));
        if (session == null || !session.isWalnutBowlingActive() || session.getBowlingNutSystem() == null) {
            clear();
            return;
        }
        List<BowlingNut> live = session.getBowlingNutSystem().getNuts();
        nuts.sync(live, this::spawn, this::update, this::despawn);
    }

    public void clear() {
        nuts.clear(this::despawn);
        rolls.clear();
    }

    private PamActor spawn(BowlingNut nut) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.setRotateOffset(0f, ROLL_PIVOT_Y);
        actor.setPlaying(false);
        layer.addActor(actor);
        rolls.put(actor, new Roll());
        return actor;
    }

    private void despawn(PamActor actor) {
        rolls.remove(actor);
        actor.remove();
    }

    private void update(BowlingNut nut, PamActor actor) {
        float width = layout.tileWidth();
        float height = layout.tileHeight();
        double displayX = displayX(nut);
        double displayRow = displayRow(nut);
        float worldX = layout.worldX(displayX);
        float worldY = layout.originY() + (layout.rows() - 1f - (float) displayRow) * height;
        actor.setSize(width, height);
        actor.setPosition(worldX - width / 2f, worldY);
        actor.setClip(pamPath(nut.getType()), IDLE_CLIP, scale(nut.getType()), true);
        actor.setTint(nut.getType() == BowlingNutType.EXPLOSIVE ? EXPLOSIVE_TINT : Color.WHITE);
        actor.setUserObject((int) Math.round(displayRow));
        actor.setPlaying(false);
        actor.setRotateOffset(0f, ROLL_PIVOT_Y);
        applyRoll(actor, nut.getType(), displayX, displayRow);
    }

    private void applyRoll(PamActor actor, BowlingNutType type, double displayX, double displayRow) {
        Roll roll = rolls.get(actor);
        if (roll == null) {
            roll = new Roll();
            rolls.put(actor, roll);
        }
        if (roll.started) {
            float dx = (float) (displayX - roll.lastX) * layout.tileWidth();
            float dy = (float) (displayRow - roll.lastRow) * layout.tileHeight();
            roll.distance += (float) Math.hypot(dx, dy);
        }
        roll.lastX = displayX;
        roll.lastRow = displayRow;
        roll.started = true;
        float radius = layout.tileWidth() * ROLL_RADIUS_FACTOR * scale(type);
        if (radius <= 0f) {
            actor.setRotation(0f);
            return;
        }
        actor.setRotation(-roll.distance / radius * MathUtils.radiansToDegrees);
    }

    private double displayX(BowlingNut nut) {
        if (tickFraction <= 0f) {
            return nut.getX();
        }
        return nut.getX() + Math.cos(nut.getAngleRadians()) * BowlingNutSystem.SPEED * tickFraction;
    }

    private double displayRow(BowlingNut nut) {
        if (tickFraction <= 0f) {
            return nut.getRow();
        }
        return nut.getRow() + Math.sin(nut.getAngleRadians()) * BowlingNutSystem.SPEED * tickFraction;
    }

    private static String pamPath(BowlingNutType type) {
        return switch (type) {
            case EXPLOSIVE -> EXPLODEONUT_PAM;
            case GIANT -> TALLNUT_PAM;
            case STANDARD -> WALLNUT_PAM;
        };
    }

    private static float scale(BowlingNutType type) {
        if (type == BowlingNutType.GIANT) {
            return LawnLayout.GIANT_WALLNUT_SCALE;
        }
        return LawnLayout.PLANT_SCALE;
    }

    private static final class Roll {
        private double lastX;
        private double lastRow;
        private float distance;
        private boolean started;
    }
}
