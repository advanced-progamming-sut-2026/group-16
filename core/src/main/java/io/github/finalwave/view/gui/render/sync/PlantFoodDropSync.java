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
import io.github.finalwave.model.item.PlantFoodDrop;
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


public final class PlantFoodDropSync {
    public static final String PICKUP_PATH =
            "768/INITIAL/EFFECTS/PLANTFOOD_PICKUP/PLANTFOOD_PICKUP.PAM";
    private static final String IDLE_CLIP = "animation";
    private static final float SIZE = 88f;
    private static final float END_SCALE = 0.35f;
    private static final float FLIGHT_SPEED = 1450f;
    private static final float MIN_FLIGHT_SECONDS = 0.45f;
    private static final float MAX_FLIGHT_SECONDS = 0.85f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Predicate<PlantFoodDrop> onCollect;
    private final ActorRegistry<PlantFoodDrop, PamActor> drops = new ActorRegistry<>();
    private final Set<PamActor> flying = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Flight> flights = new ArrayList<>();
    private final Vector2 hudLocal = new Vector2();
    private Supplier<Vector2> hudStageTarget;
    private IntConsumer onArrived;
    private Runnable onAborted;

    public PlantFoodDropSync(GameAssets assets, LawnLayout layout, Group layer,
                             Predicate<PlantFoodDrop> onCollect) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
        this.onCollect = onCollect;
    }

    public void setHudStageTarget(Supplier<Vector2> hudStageTarget) {
        this.hudStageTarget = hudStageTarget;
    }

    public void setOnArrived(IntConsumer onArrived) {
        this.onArrived = onArrived;
    }

    public void setOnAborted(Runnable onAborted) {
        this.onAborted = onAborted;
    }

    public void sync(GameSession session) {
        if (session == null) {
            return;
        }
        List<PlantFoodDrop> live = new ArrayList<>();
        for (PlantFoodDrop drop : session.getPlantFoodDrops()) {
            if (drop != null && !drop.isConsumed()) {
                live.add(drop);
            }
        }
        drops.sync(live, this::spawn, this::update, this::despawn);
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
            float t = Interpolation.pow3In.apply(raw);
            Vector2 dest = hudLocalPoint();
            float endX = dest == null ? flight.startX : dest.x;
            float endY = dest == null ? flight.startY : dest.y;
            float scale = MathUtils.lerp(1f, END_SCALE, t);
            float width = flight.startWidth * scale;
            float height = flight.startHeight * scale;
            float startCx = flight.startX + flight.startWidth * 0.5f;
            float startCy = flight.startY + flight.startHeight * 0.5f;
            float cx = MathUtils.lerp(startCx, endX, t);
            float cy = MathUtils.lerp(startCy, endY, t);
            PamActor actor = flight.actor;
            actor.setSize(width, height);
            actor.setPosition(cx - width * 0.5f, cy - height * 0.5f);
            actor.getColor().a = MathUtils.lerp(1f, 0.5f, t);
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
        drops.clear(PamActor::remove);
        if (aborted && onAborted != null) {
            onAborted.run();
        }
    }

    private PamActor spawn(PlantFoodDrop drop) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.enabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                collect(drop, actor);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                collect(drop, actor);
                return true;
            }
        });
        layer.addActor(actor);
        return actor;
    }

    private void update(PlantFoodDrop drop, PamActor actor) {
        if (flying.contains(actor)) {
            return;
        }
        float worldX = layout.worldX(drop.getWorldX());
        float worldY = layout.worldYForRow(drop.getRow())
                + layout.tileHeight() * (LawnLayout.ZOMBIE_ANCHOR_Y + 0.05f);
        actor.setSize(SIZE, SIZE);
        actor.setPosition(worldX - SIZE * 0.5f, worldY - SIZE * 0.5f);
        actor.setClip(PICKUP_PATH, IDLE_CLIP, 0.62f, true);
        actor.setTint(Color.WHITE);
        actor.setUserObject(drop.getRow());
    }

    private void despawn(PamActor actor) {
        if (!flying.contains(actor)) {
            actor.remove();
        }
    }

    private void collect(PlantFoodDrop drop, PamActor actor) {
        if (drop == null || actor == null || flying.contains(actor) || drop.isConsumed()) {
            return;
        }
        if (onCollect == null || !onCollect.test(drop)) {
            return;
        }
        flying.add(actor);
        actor.setTouchable(Touchable.disabled);
        actor.toFront();
        startFlight(actor);
    }

    private void startFlight(PamActor actor) {
        Vector2 dest = hudLocalPoint();
        if (dest == null) {
            finishFlight(new Flight(actor, actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(), 0.05f));
            return;
        }
        float startCx = actor.getX() + actor.getWidth() * 0.5f;
        float startCy = actor.getY() + actor.getHeight() * 0.5f;
        float dist = Vector2.dst(startCx, startCy, dest.x, dest.y);
        float duration = MathUtils.clamp(dist / FLIGHT_SPEED, MIN_FLIGHT_SECONDS, MAX_FLIGHT_SECONDS);
        flights.add(new Flight(actor, actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(), duration));
    }

    private void finishFlight(Flight flight) {
        flying.remove(flight.actor);
        flight.actor.remove();
        if (onArrived != null) {
            onArrived.accept(1);
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

    private static final class Flight {
        private final PamActor actor;
        private final float startX;
        private final float startY;
        private final float startWidth;
        private final float startHeight;
        private final float duration;
        private float elapsed;

        private Flight(PamActor actor, float startX, float startY, float startWidth, float startHeight,
                       float duration) {
            this.actor = actor;
            this.startX = startX;
            this.startY = startY;
            this.startWidth = startWidth;
            this.startHeight = startHeight;
            this.duration = duration;
        }
    }
}
