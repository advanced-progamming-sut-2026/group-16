package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.clip.ProjectileClips;
import io.github.finalwave.view.gui.render.clip.ZombieClips;
import io.github.finalwave.view.gui.render.sync.DeadLineSync;
import io.github.finalwave.view.gui.render.sync.GraveSync;
import io.github.finalwave.view.gui.render.sync.MowerSync;
import io.github.finalwave.view.gui.render.sync.PlantSync;
import io.github.finalwave.view.gui.render.sync.ProjectileSync;
import io.github.finalwave.view.gui.render.sync.ProtectTileSync;
import io.github.finalwave.view.gui.render.sync.SunSync;
import io.github.finalwave.view.gui.render.sync.ZombieSync;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;


public final class BattlefieldGroup extends WidgetGroup {
    private final Group environmentLayer = new Group();
    private final Group highlightLayer = new Group();
    private final Group mowerLayer = new Group();
    private final Group plantLayer = new Group();
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
    private DeadLineSync deadLineSync;
    private Consumer<Sun> sunCollector;

    public BattlefieldGroup() {
        setFillParent(true);
        setTouchable(Touchable.enabled);
        environmentLayer.setTouchable(Touchable.disabled);
        highlightLayer.setTouchable(Touchable.disabled);
        mowerLayer.setTouchable(Touchable.disabled);
        plantLayer.setTouchable(Touchable.disabled);
        deadlineLayer.setTouchable(Touchable.disabled);
        zombieLayer.setTouchable(Touchable.disabled);
        projectileLayer.setTouchable(Touchable.disabled);
        fxLayer.setTouchable(Touchable.disabled);
        addActor(environmentLayer);
        addActor(highlightLayer);
        addActor(mowerLayer);
        addActor(plantLayer);
        addActor(deadlineLayer);
        addActor(zombieLayer);
        addActor(projectileLayer);
        addActor(sunLayer);
        addActor(fxLayer);
    }

    public void bind(GameAssets assets, LawnLayout layout, EntityAnimationCatalog catalog) {
        clearBattlefield();
        if (assets == null || layout == null || catalog == null) {
            plantSync = null;
            zombieSync = null;
            projectileSync = null;
            sunSync = null;
            mowerSync = null;
            protectTileSync = null;
            graveSync = null;
            deadLineSync = null;
            return;
        }
        plantSync = new PlantSync(assets, layout, new PlantClips(catalog), plantLayer);
        zombieSync = new ZombieSync(assets, layout, new ZombieClips(catalog), zombieLayer);
        projectileSync = new ProjectileSync(assets, layout, new ProjectileClips(), zombieLayer);
        sunSync = new SunSync(assets, layout, sunLayer, this::collectSun);
        mowerSync = new MowerSync(assets, layout, mowerLayer);
        protectTileSync = new ProtectTileSync(layout, environmentLayer);
        graveSync = new GraveSync(assets, layout, environmentLayer);
        deadLineSync = new DeadLineSync(assets, layout, deadlineLayer);
    }

    public void setSunCollector(Consumer<Sun> sunCollector) {
        this.sunCollector = sunCollector;
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
        if (deadLineSync != null) {
            deadLineSync.sync(session);
        }
        if (plantSync != null) {
            plantSync.sync(session);
            sortByRow(plantLayer, BattlefieldGroup::sortKey);
        }
        if (zombieSync != null) {
            zombieSync.sync(session, tickFraction);
        }
        if (projectileSync != null) {
            projectileSync.sync(session, tickFraction);
        }
        if (zombieSync != null || projectileSync != null) {
            sortByRow(zombieLayer, BattlefieldGroup::sortKey);
        }
        if (mowerSync != null) {
            mowerSync.sync(session, tickFraction);
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
        if (deadLineSync != null) {
            deadLineSync.clear();
        }
        environmentLayer.clearChildren();
        highlightLayer.clearChildren();
        mowerLayer.clearChildren();
        plantLayer.clearChildren();
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

    private void collectSun(Sun sun) {
        if (sunCollector != null) {
            sunCollector.accept(sun);
        }
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
