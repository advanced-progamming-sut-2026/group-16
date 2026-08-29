package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.clip.ProjectileClips;
import io.github.finalwave.view.gui.render.clip.ZombieClips;
import io.github.finalwave.view.gui.render.sync.ArcadeObstacleSync;
import io.github.finalwave.view.gui.render.sync.PianoObstacleSync;
import io.github.finalwave.view.gui.render.sync.BeghouledPlantSync;
import io.github.finalwave.view.gui.render.sync.BossFxSync;
import io.github.finalwave.view.gui.render.sync.BowlingLineSync;
import io.github.finalwave.view.gui.render.sync.BowlingNutSync;
import io.github.finalwave.view.gui.render.sync.BrainSync;
import io.github.finalwave.view.gui.render.sync.DeadLineSync;
import io.github.finalwave.view.gui.render.sync.FireTileSync;
import io.github.finalwave.view.gui.render.sync.GraveSync;
import io.github.finalwave.view.gui.render.sync.GooPuddleSync;
import io.github.finalwave.view.gui.render.sync.GroundSeedPacketSync;
import io.github.finalwave.view.gui.render.sync.IceTileSync;
import io.github.finalwave.view.gui.render.sync.LawnBurstSync;
import io.github.finalwave.view.gui.render.sync.MowerSync;
import io.github.finalwave.view.gui.render.sync.PlantSync;
import io.github.finalwave.view.gui.render.sync.ProjectileSync;
import io.github.finalwave.view.gui.render.sync.ProtectTileSync;
import io.github.finalwave.view.gui.render.sync.SandstormSync;
import io.github.finalwave.view.gui.render.sync.SunSync;
import io.github.finalwave.view.gui.render.sync.VaseSync;
import io.github.finalwave.view.gui.render.sync.ZombieSync;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Comparator;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.BiConsumer;


public final class BattlefieldGroup extends WidgetGroup {
    private final Group environmentLayer = new Group();
    private final Group highlightLayer = new Group();
    private final Group mowerLayer = new Group();
    private final Group plantLayer = new Group();
    private final Group packetLayer = new Group();
    private final Group deadlineLayer = new Group();
    private final Group zombieLayer = new Group();
    private final Group projectileLayer = new Group();
    private final Group sunLayer = new Group();
    private final Group fxLayer = new Group();

    private PlantSync plantSync;
    private ZombieSync zombieSync;
    private ProjectileSync projectileSync;
    private SunSync sunSync;
    private MowerSync mowerSync;
    private ProtectTileSync protectTileSync;
    private GraveSync graveSync;
    private FireTileSync fireTileSync;
    private GooPuddleSync gooPuddleSync;
    private IceTileSync iceTileSync;
    private BossFxSync bossFxSync;
    private DeadLineSync deadLineSync;
    private VaseSync vaseSync;
    private GroundSeedPacketSync packetSync;
    private BowlingNutSync bowlingNutSync;
    private BowlingLineSync bowlingLineSync;
    private BrainSync brainSync;
    private ArcadeObstacleSync arcadeObstacleSync;
    private PianoObstacleSync pianoObstacleSync;
    private BeghouledPlantSync beghouledPlantSync;
    private LawnBurstSync lawnBurstSync;
    private SandstormSync sandstormSync;
    private Predicate<Sun> sunCollector;

    public BattlefieldGroup() {
        setFillParent(true);
        setTouchable(Touchable.enabled);
        environmentLayer.setTouchable(Touchable.disabled);
        highlightLayer.setTouchable(Touchable.disabled);
        mowerLayer.setTouchable(Touchable.disabled);
        plantLayer.setTouchable(Touchable.disabled);
        packetLayer.setTouchable(Touchable.disabled);
        deadlineLayer.setTouchable(Touchable.disabled);
        zombieLayer.setTouchable(Touchable.disabled);
        projectileLayer.setTouchable(Touchable.disabled);
        fxLayer.setTouchable(Touchable.disabled);
        addActor(environmentLayer);
        addActor(highlightLayer);
        addActor(mowerLayer);
        addActor(plantLayer);
        addActor(packetLayer);
        addActor(deadlineLayer);
        addActor(zombieLayer);
        addActor(projectileLayer);
        addActor(sunLayer);
        addActor(fxLayer);
    }

    public void bind(GameAssets assets, LawnLayout layout, EntityAnimationCatalog catalog) {
        if (beghouledPlantSync != null) {
            beghouledPlantSync.dispose();
            beghouledPlantSync = null;
        }
        clearBattlefield();
        if (assets == null || layout == null || catalog == null) {
            plantSync = null;
            zombieSync = null;
            projectileSync = null;
            sunSync = null;
            mowerSync = null;
            protectTileSync = null;
            graveSync = null;
            fireTileSync = null;
            gooPuddleSync = null;
            iceTileSync = null;
            bossFxSync = null;
            deadLineSync = null;
            vaseSync = null;
            packetSync = null;
            bowlingNutSync = null;
            bowlingLineSync = null;
            brainSync = null;
            arcadeObstacleSync = null;
            pianoObstacleSync = null;
            lawnBurstSync = null;
            sandstormSync = null;
            return;
        }
        ProjectileClips projectileClips = new ProjectileClips();
        plantSync = new PlantSync(assets, layout, new PlantClips(catalog), plantLayer);
        zombieSync = new ZombieSync(assets, layout, new ZombieClips(catalog), new PlantClips(catalog), zombieLayer);
        projectileSync = new ProjectileSync(assets, layout, projectileClips, projectileLayer);
        sunSync = new SunSync(assets, layout, sunLayer, this::collectSun);
        mowerSync = new MowerSync(assets, layout, mowerLayer);
        protectTileSync = new ProtectTileSync(layout, environmentLayer);
        graveSync = new GraveSync(assets, layout, environmentLayer);
        fireTileSync = new FireTileSync(assets, layout, environmentLayer);
        gooPuddleSync = new GooPuddleSync(assets, layout, projectileClips, environmentLayer);
        iceTileSync = new IceTileSync(assets, layout, environmentLayer);
        bossFxSync = new BossFxSync(assets, layout, fxLayer);
        deadLineSync = new DeadLineSync(assets, layout, deadlineLayer);
        vaseSync = new VaseSync(assets, layout, environmentLayer);
        packetSync = new GroundSeedPacketSync(assets, layout, packetLayer);
        bowlingNutSync = new BowlingNutSync(assets, layout, plantLayer);
        bowlingLineSync = new BowlingLineSync(layout, highlightLayer);
        brainSync = new BrainSync(assets, layout, mowerLayer);
        arcadeObstacleSync = new ArcadeObstacleSync(assets, layout, zombieLayer);
        pianoObstacleSync = new PianoObstacleSync(assets, layout, zombieLayer);
        beghouledPlantSync = new BeghouledPlantSync(assets, layout, new PlantClips(catalog), plantLayer, this);
        lawnBurstSync = new LawnBurstSync(assets, layout, fxLayer);
        sandstormSync = new SandstormSync(assets, layout, environmentLayer, fxLayer);
    }

    public void setShakeListener(BiConsumer<Float, Float> listener) {
        if (lawnBurstSync != null) {
            lawnBurstSync.setOnShake(listener);
        }
        if (zombieSync != null) {
            zombieSync.setSmashShake(listener);
        }
    }

    public void setBeghouledController(BeghouledController controller, BooleanSupplier blocked) {
        if (beghouledPlantSync != null) {
            beghouledPlantSync.setController(controller, blocked);
        }
    }

    public boolean beghouledBusy() {
        return beghouledPlantSync != null && beghouledPlantSync.isBusy();
    }

    public void setSunCollector(Predicate<Sun> sunCollector) {
        this.sunCollector = sunCollector;
    }

    public void setSunHudTarget(Supplier<Vector2> hudStageTarget) {
        if (sunSync != null) {
            sunSync.setHudStageTarget(hudStageTarget);
        }
    }

    public void setSunDeferred(IntConsumer onDeferred) {
        if (sunSync != null) {
            sunSync.setOnDeferred(onDeferred);
        }
    }

    public void setSunArrived(IntConsumer onArrived) {
        if (sunSync != null) {
            sunSync.setOnArrived(onArrived);
        }
    }

    public void setSunFlightsAborted(Runnable onAborted) {
        if (sunSync != null) {
            sunSync.setOnAborted(onAborted);
        }
    }

    public void tickSunFlights(float delta) {
        if (sunSync != null) {
            sunSync.tickFlights(delta);
        }
    }

    public void sync(GameSession session) {
        sync(session, 0f);
    }

    public void sync(GameSession session, float tickFraction) {
        if (session == null) {
            return;
        }
        if (protectTileSync != null) {
            protectTileSync.sync(session);
        }
        if (graveSync != null) {
            graveSync.sync(session);
            sortByRow(environmentLayer, BattlefieldGroup::sortKey);
        }
        if (fireTileSync != null) {
            fireTileSync.sync(session);
        }
        if (gooPuddleSync != null) {
            gooPuddleSync.sync(session);
        }
        if (iceTileSync != null) {
            iceTileSync.sync(session);
        }
        if (bossFxSync != null) {
            bossFxSync.sync(session);
        }
        if (lawnBurstSync != null) {
            lawnBurstSync.sync(session);
        }
        if (vaseSync != null) {
            vaseSync.sync(session);
        }
        if (packetSync != null) {
            packetSync.sync(session);
        }
        if (bowlingLineSync != null) {
            bowlingLineSync.sync(session);
        }
        if (deadLineSync != null) {
            deadLineSync.sync(session);
        }
        if (session.isBeghouledActive()) {
            if (plantSync != null) {
                plantSync.clear();
            }
            if (beghouledPlantSync != null) {
                beghouledPlantSync.sync(session);
            }
        } else {
            if (beghouledPlantSync != null) {
                beghouledPlantSync.clear();
            }
            if (plantSync != null) {
                plantSync.sync(session);
            }
        }
        if (bowlingNutSync != null) {
            bowlingNutSync.sync(session, tickFraction);
        }
        boolean skipPlantSort = session.isBeghouledActive()
                && beghouledPlantSync != null
                && beghouledPlantSync.holdsSwapOverlap();
        if (!skipPlantSort && (plantSync != null || bowlingNutSync != null || beghouledPlantSync != null)) {
            sortByRow(plantLayer, BattlefieldGroup::sortKey);
        }
        if (zombieSync != null) {
            zombieSync.sync(session, tickFraction);
        }
        if (sandstormSync != null) {
            sandstormSync.sync(session);
        }
        if (arcadeObstacleSync != null) {
            arcadeObstacleSync.sync(session);
        }
        if (pianoObstacleSync != null) {
            pianoObstacleSync.sync(session);
        }
        if (projectileSync != null) {
            projectileSync.sync(session, tickFraction);
            sortByRow(projectileLayer, BattlefieldGroup::sortKey);
        }
        if (zombieSync != null || arcadeObstacleSync != null || pianoObstacleSync != null) {
            sortByRow(zombieLayer, BattlefieldGroup::sortKey);
        }
        if (mowerSync != null) {
            mowerSync.sync(session, tickFraction);
        }
        if (brainSync != null) {
            brainSync.sync(session);
        }
        if (sunSync != null) {
            sunSync.sync(session, tickFraction);
        }
    }

    public void setPlaying(boolean playing) {
        setPlaying(playing, playing);
    }

    public void setPlaying(boolean unitsPlaying, boolean environmentPlaying) {
        setPlaying(environmentLayer, environmentPlaying);
        setPlaying(deadlineLayer, environmentPlaying);
        setPlaying(mowerLayer, unitsPlaying);
        setPlaying(plantLayer, unitsPlaying);
        setPlaying(zombieLayer, unitsPlaying);
        setPlaying(projectileLayer, unitsPlaying);
        setPlaying(sunLayer, unitsPlaying);
        setPlaying(fxLayer, unitsPlaying);
    }

    public void setPlantLayerPlaying(boolean playing) {
        setPlaying(plantLayer, playing);
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        setPlaybackSpeed(playbackSpeed, playbackSpeed);
    }

    public void setPlaybackSpeed(float unitSpeed, float environmentSpeed) {
        setPlaybackSpeed(environmentLayer, environmentSpeed);
        setPlaybackSpeed(deadlineLayer, environmentSpeed);
        setPlaybackSpeed(mowerLayer, unitSpeed);
        setPlaybackSpeed(plantLayer, unitSpeed);
        setPlaybackSpeed(zombieLayer, unitSpeed);
        setPlaybackSpeed(projectileLayer, unitSpeed);
        setPlaybackSpeed(sunLayer, unitSpeed);
        setPlaybackSpeed(fxLayer, unitSpeed);
    }

    public void sortByRow(Group layer, ToIntFunction<Actor> rowOf) {
        layer.getChildren().sort(Comparator.comparingInt(rowOf));
    }

    public void clearBattlefield() {
        if (plantSync != null) {
            plantSync.clear();
        }
        if (zombieSync != null) {
            zombieSync.clear();
        }
        if (projectileSync != null) {
            projectileSync.clear();
        }
        if (sunSync != null) {
            sunSync.clear();
        }
        if (mowerSync != null) {
            mowerSync.clear();
        }
        if (protectTileSync != null) {
            protectTileSync.clear();
        }
        if (graveSync != null) {
            graveSync.clear();
        }
        if (fireTileSync != null) {
            fireTileSync.clear();
        }
        if (gooPuddleSync != null) {
            gooPuddleSync.clear();
        }
        if (iceTileSync != null) {
            iceTileSync.clear();
        }
        if (bossFxSync != null) {
            bossFxSync.clear();
        }
        if (lawnBurstSync != null) {
            lawnBurstSync.clear();
        }
        if (sandstormSync != null) {
            sandstormSync.clear();
        }
        if (deadLineSync != null) {
            deadLineSync.clear();
        }
        if (vaseSync != null) {
            vaseSync.clear();
        }
        if (packetSync != null) {
            packetSync.clear();
        }
        if (bowlingNutSync != null) {
            bowlingNutSync.clear();
        }
        if (bowlingLineSync != null) {
            bowlingLineSync.clear();
        }
        if (brainSync != null) {
            brainSync.clear();
        }
        if (arcadeObstacleSync != null) {
            arcadeObstacleSync.clear();
        }
        if (pianoObstacleSync != null) {
            pianoObstacleSync.clear();
        }
        if (beghouledPlantSync != null) {
            beghouledPlantSync.clear();
        }
        environmentLayer.clearChildren();
        highlightLayer.clearChildren();
        mowerLayer.clearChildren();
        plantLayer.clearChildren();
        packetLayer.clearChildren();
        deadlineLayer.clearChildren();
        zombieLayer.clearChildren();
        projectileLayer.clearChildren();
        sunLayer.clearChildren();
        fxLayer.clearChildren();
    }

    public Group environmentLayer() {
        return environmentLayer;
    }

    public Group highlightLayer() {
        return highlightLayer;
    }

    public Group mowerLayer() {
        return mowerLayer;
    }

    public Group plantLayer() {
        return plantLayer;
    }

    public Group packetLayer() {
        return packetLayer;
    }

    public Group zombieLayer() {
        return zombieLayer;
    }

    public Group projectileLayer() {
        return projectileLayer;
    }

    public Group sunLayer() {
        return sunLayer;
    }

    public Group fxLayer() {
        return fxLayer;
    }

    private boolean collectSun(Sun sun) {
        return sunCollector != null && sunCollector.test(sun);
    }

    private static int sortKey(Actor actor) {
        Object key = actor.getUserObject();
        return key instanceof Integer value ? value : 0;
    }

    private static void setPlaying(Group layer, boolean playing) {
        for (Actor actor : layer.getChildren()) {
            if (actor instanceof PamActor pamActor) {
                pamActor.setPlaying(playing);
            }
        }
    }

    private static void setPlaybackSpeed(Group layer, float playbackSpeed) {
        for (Actor actor : layer.getChildren()) {
            if (actor instanceof PamActor pamActor) {
                pamActor.setPlaybackSpeed(playbackSpeed);
            }
        }
    }
}
