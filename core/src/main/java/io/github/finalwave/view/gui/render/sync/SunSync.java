package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


public final class SunSync {
    public static final String SUN_PATH = "768/INITIAL/EFFECTS/SUN/SUN.PAM";

    private static final Color RADIOACTIVE = new Color(0.72f, 0.42f, 1f, 1f);
    private static final float MIN_FLIGHT_SECONDS = 0.5f;
    private static final float MAX_FLIGHT_SECONDS = 0.9f;
    private static final float FLIGHT_SPEED = 1550f;
    private static final float END_SCALE = 0.38f;
    private static final float ATTRACT_END_SCALE = 0.12f;
    private static final float ATTRACT_MIN_SECONDS = 0.38f;
    private static final float ATTRACT_MAX_SECONDS = 0.72f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Predicate<Sun> onCollect;
    private final ActorRegistry<Sun, PamActor> suns = new ActorRegistry<>();
    private final Set<PamActor> flying = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Flight> flights = new ArrayList<>();
    private final Vector2 hudLocal = new Vector2();
    private float tickFraction;
    private GameSession session;
    private Supplier<Vector2> hudStageTarget;
    private IntConsumer onDeferred;
    private IntConsumer onArrived;
    private Runnable onAborted;

    public SunSync(GameAssets assets, LawnLayout layout, Group layer, Predicate<Sun> onCollect) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
        this.onCollect = onCollect;
    }

    public void setHudStageTarget(Supplier<Vector2> hudStageTarget) {
        this.hudStageTarget = hudStageTarget;
    }

    public void setOnDeferred(IntConsumer onDeferred) {
        this.onDeferred = onDeferred;
    }

    public void setOnArrived(IntConsumer onArrived) {
        this.onArrived = onArrived;
    }

    public void setOnAborted(Runnable onAborted) {
        this.onAborted = onAborted;
    }

    public void sync(GameSession session, float tickFraction) {
        this.tickFraction = Math.max(0f, Math.min(1f, tickFraction));
        this.session = session;
        if (session == null) {
            return;
        }
        List<Sun> live = new ArrayList<>();
        for (Sun sun : session.getSunItems()) {
            if (sun != null && !sun.isExpired()) {
                live.add(sun);
            }
        }
        suns.sync(live, this::spawn, this::update, this::despawn);
    }

    public void tickFlights(float delta) {
        if (flights.isEmpty() || delta <= 0f) {
            return;
        }
        Iterator<Flight> iterator = flights.iterator();
        while (iterator.hasNext()) {
            Flight flight = iterator.next();
            flight.elapsed += delta;
            float raw = Math.min(1f, flight.elapsed / flight.duration);
            boolean attract = flight.attracted != null;
            float t = attract ? Interpolation.pow4In.apply(raw) : Interpolation.pow3In.apply(raw);
            Vector2 dest = attract ? staffPoint(flight.attracted) : hudLocalPoint();
            float endX = dest == null ? flight.startX : dest.x;
            float endY = dest == null ? flight.startY : dest.y;
            float scale = MathUtils.lerp(1f, attract ? ATTRACT_END_SCALE : END_SCALE, t);
            float width = flight.startWidth * scale;
            float height = flight.startHeight * scale;
            float startCx = flight.startX + flight.startWidth * 0.5f;
            float startCy = flight.startY + flight.startHeight * 0.5f;
            float cx = MathUtils.lerp(startCx, endX, t);
            float cy = MathUtils.lerp(startCy, endY, t);
            if (attract) {
                cy += (1f - t) * t * 2f * Math.max(12f, flight.startHeight * 0.22f);
            }
            PamActor actor = flight.actor;
            actor.setSize(width, height);
            actor.setPosition(cx - width * 0.5f, cy - height * 0.5f);
            actor.setRotation(0f);
            actor.getColor().a = MathUtils.lerp(1f, attract ? 0.35f : 0.55f, t);
            if (raw >= 1f) {
                finishFlight(flight);
                iterator.remove();
            }
        }
    }

    public void clear() {
        boolean aborted = !flights.isEmpty();
        for (Flight flight : flights) {
            flight.actor.remove();
        }
        flights.clear();
        flying.clear();
        suns.clear(PamActor::remove);
        if (aborted && onAborted != null) {
            onAborted.run();
        }
    }

    private PamActor spawn(Sun sun) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.enabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                collect(sun, actor);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                collect(sun, actor);
                return true;
            }
        });
        layer.addActor(actor);
        return actor;
    }

    private void update(Sun sun, PamActor actor) {
        if (flying.contains(actor)) {
            return;
        }
        if (sun.isAttracted()) {
            startAttract(sun, actor);
            return;
        }
        float size = sun.getType() == SunType.SPECIAL ? 110f : 80f;
        actor.setSize(size, size);
        Vector2 cell = layout.cellCenter(sun.getCol(), sun.getRow());
        actor.setPosition(cell.x - size / 2f, fallY(sun, cell.y) - size / 2f);
        actor.setClip(SUN_PATH, clipOf(sun.getType()), sun.getType() == SunType.SPECIAL ? 0.72f : 0.55f, true);
        actor.setTint(sun.getType() == SunType.RADIOACTIVE ? RADIOACTIVE : Color.WHITE);
        actor.setUserObject(sun.getRow());
    }

    private void despawn(PamActor actor) {
        if (!flying.contains(actor)) {
            actor.remove();
        }
    }

    private void collect(Sun sun, PamActor actor) {
        if (sun == null || actor == null || flying.contains(actor) || sun.isAttracted()) {
            return;
        }
        boolean deferCredit = sun.getType() != SunType.RADIOACTIVE || !sun.isFalling();
        int value = deferCredit ? Math.max(0, sun.getValue()) : 0;
        if (onCollect == null || !onCollect.test(sun)) {
            return;
        }
        flying.add(actor);
        actor.setTouchable(Touchable.disabled);
        actor.toFront();
        if (value > 0 && onDeferred != null) {
            onDeferred.accept(value);
        }
        startFlight(actor, value, null);
    }

    private void startAttract(Sun sun, PamActor actor) {
        flying.add(actor);
        actor.setTouchable(Touchable.disabled);
        actor.toFront();
        startFlight(actor, 0, sun);
    }

    private void startFlight(PamActor actor, int value, Sun attracted) {
        Vector2 dest = attracted == null ? hudLocalPoint() : staffPoint(attracted);
        if (dest == null) {
            finishFlight(new Flight(actor, actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(),
                    0.05f, value, attracted));
            return;
        }
        float startCx = actor.getX() + actor.getWidth() * 0.5f;
        float startCy = actor.getY() + actor.getHeight() * 0.5f;
        float dist = Vector2.dst(startCx, startCy, dest.x, dest.y);
        float duration = MathUtils.clamp(dist / FLIGHT_SPEED, MIN_FLIGHT_SECONDS, MAX_FLIGHT_SECONDS);
        if (attracted != null) {
            duration = MathUtils.clamp(dist / (FLIGHT_SPEED * 0.72f), ATTRACT_MIN_SECONDS, ATTRACT_MAX_SECONDS);
        }
        flights.add(new Flight(actor, actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(),
                duration, value, attracted));
    }

    private void finishFlight(Flight flight) {
        flying.remove(flight.actor);
        flight.actor.setRotation(0f);
        flight.actor.remove();
        if (flight.attracted != null) {
            String thiefId = flight.attracted.attractZombieId();
            flight.attracted.finishAttract();
            concealRaStaffSun(thiefId);
            return;
        }
        if (onArrived != null) {
            onArrived.accept(flight.value);
        }
    }

    private Vector2 hudLocalPoint() {
        if (hudStageTarget == null || layer.getStage() == null) {
            return null;
        }
        Vector2 stagePoint = hudStageTarget.get();
        if (stagePoint == null) {
            return null;
        }
        hudLocal.set(stagePoint);
        layer.stageToLocalCoordinates(hudLocal);
        return hudLocal;
    }

    private Vector2 staffPoint(Sun sun) {
        if (sun == null || session == null) {
            return null;
        }
        String id = sun.attractZombieId();
        if (id == null) {
            return null;
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !id.equals(zombie.getId())) {
                continue;
            }
            float towardHouse = zombie.isHypnotized() || zombie.isMovingRight()
                    ? layout.tileWidth() * 0.34f
                    : -layout.tileWidth() * 0.34f;
            hudLocal.set(layout.worldX(zombie.getX()) + towardHouse,
                    layout.worldYForRow(zombie.getRow())
                            + layout.tileHeight() * (LawnLayout.ZOMBIE_ANCHOR_Y + 0.48f));
            return hudLocal;
        }
        return null;
    }

    private void concealRaStaffSun(String zombieId) {
        if (zombieId == null || session == null) {
            return;
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie != null && zombieId.equals(zombie.getId())) {
                zombie.concealStaffSun();
                return;
            }
        }
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

    private static final class Flight {
        private final PamActor actor;
        private final float startX;
        private final float startY;
        private final float startWidth;
        private final float startHeight;
        private final float duration;
        private final int value;
        private final Sun attracted;
        private float elapsed;

        private Flight(PamActor actor, float startX, float startY, float startWidth, float startHeight,
                       float duration, int value, Sun attracted) {
            this.actor = actor;
            this.startX = startX;
            this.startY = startY;
            this.startWidth = startWidth;
            this.startHeight = startHeight;
            this.duration = Math.max(0.05f, duration);
            this.value = Math.max(0, value);
            this.attracted = attracted;
        }
    }
}
