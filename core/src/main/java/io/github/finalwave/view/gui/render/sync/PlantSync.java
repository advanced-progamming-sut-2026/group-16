package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.HitFlashTracker;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.List;


public final class PlantSync {
    private static final Color ICE_TINT = new Color(0.55f, 0.9f, 1f, 1f);
    private static final Color DISABLED = new Color(0.62f, 0.62f, 0.62f, 1f);

    private final GameAssets assets;
    private final LawnLayout layout;
    private final PlantClips clips;
    private final Group layer;
    private final ActorRegistry<Plant, PamActor> plants = new ActorRegistry<>();
    private final ActorRegistry<Plant, PamActor> iceBlocks = new ActorRegistry<>();
    private final ActorRegistry<PlantCovering, PamActor> octopi = new ActorRegistry<>();
    private final ActorRegistry<Plant, PamActor> sheep = new ActorRegistry<>();
    private final HitFlashTracker<Plant> plantHits = new HitFlashTracker<>();
    private final HitFlashTracker<Plant> iceHits = new HitFlashTracker<>();
    private final HitFlashTracker<PlantCovering> octopusHits = new HitFlashTracker<>();
    private final PlantShotTracker shots = new PlantShotTracker();

    public PlantSync(GameAssets assets, LawnLayout layout, PlantClips clips, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.clips = clips;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        List<Plant> live = new ArrayList<>();
        List<Plant> frozen = new ArrayList<>();
        for (Plant plant : board.getAllPlants()) {
            if (plant == null || !plant.isAlive()) {
                continue;
            }
            live.add(plant);
            if (freezeLevel(plant, session) >= 2) {
                frozen.add(plant);
            }
        }
        Iterable<Projectile> projectiles = session.getProjectileSystem() == null
                ? List.of()
                : session.getProjectileSystem().getProjectiles();
        shots.observe(projectiles);
        List<PlantCovering> octopusCoverings = liveOctopi(session);
        List<Plant> sheeped = liveSheep(live);
        plants.sync(live, this::spawnPlant, (plant, actor) -> updatePlant(plant, actor, board, session), PamActor::remove);
        iceBlocks.sync(frozen, this::spawnIce, (plant, actor) -> updateIce(plant, actor, session), PamActor::remove);
        octopi.sync(octopusCoverings, this::spawnOctopus, this::updateOctopus, PamActor::remove);
        sheep.sync(sheeped, this::spawnSheep, this::updateSheep, PamActor::remove);
        plantHits.retain(live);
        iceHits.retain(frozen);
        octopusHits.retain(octopusCoverings);
        shots.retain(live, projectiles);
    }

    public void clear() {
        plants.clear(PamActor::remove);
        iceBlocks.clear(PamActor::remove);
        octopi.clear(PamActor::remove);
        sheep.clear(PamActor::remove);
        plantHits.clear();
        iceHits.clear();
        octopusHits.clear();
        shots.clear();
    }

    private PamActor spawnPlant(Plant plant) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        layer.addActor(actor);
        return actor;
    }

    private PamActor spawnIce(Plant plant) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.playThen(PlantClips.ICE_BLOCK_PATH, PlantClips.ICE_BLOCK_START_CLIP,
                LawnLayout.ICE_BLOCK_SCALE, PlantClips.ICE_BLOCK_CLIP, true, null);
        layer.addActor(actor);
        return actor;
    }

    private void updatePlant(Plant plant, PamActor actor, GameBoard board, GameSession session) {
        Vector2 center = layout.cellCenter(plant.getCol(), plant.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        boolean justFired = shots.consume(plant);
        List<Zombie> zombies = session.getZombies();
        EntityAnimationCatalog.ClipSpec spec = PlantVisualState.clip(
                plant, clips, justFired, zombies, actor.clipName());
        EntityAnimationCatalog.ClipSpec idle = PlantVisualState.idle(plant, clips);
        float scale = clips.scale(plant.getName());
        boolean playingOneShot = PlantVisualState.isOneShotClip(actor.clipName());
        if (PlantVisualState.isOneShot(plant, spec) && !playingOneShot) {
            String followUp = followUpClip(plant, spec, idle, zombies);
            actor.playThen(spec.path(), spec.clip(), scale, followUp, true, null);
        } else if (!playingOneShot) {
            actor.setClip(spec.path(), spec.clip(), scale, true);
        }
        actor.setUserObject(sortKey(plant, board, 0));
        int freeze = freezeLevel(plant, session);
        boolean covered = isOctopusCovered(plant, session);
        actor.setVisible(!plant.isCatTransformed() && !covered);
        if (plant.isDisabled() || plant.isCatTransformed()) {
            actor.setTint(DISABLED);
        } else if (freeze == 1) {
            actor.setTint(ICE_TINT);
        } else {
            actor.setTint(Color.WHITE);
        }
        plantHits.observe(plant, plant.getHealth(), actor);
    }

    private void updateIce(Plant plant, PamActor actor, GameSession session) {
        Vector2 center = layout.cellCenter(plant.getCol(), plant.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        if (actor.hasFollowUp()) {
            actor.setDrawScale(LawnLayout.ICE_BLOCK_SCALE);
        } else {
            actor.setClip(PlantClips.ICE_BLOCK_PATH, PlantClips.ICE_BLOCK_CLIP, LawnLayout.ICE_BLOCK_SCALE, true);
        }
        actor.setUserObject(sortKey(plant, null, 2));
        iceHits.observe(plant, iceHealth(plant, session), actor);
    }

    private PamActor spawnOctopus(PlantCovering covering) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.setClip(PlantClips.OCTOPUS_PATH, PlantClips.OCTOPUS_FLY_CLIP, LawnLayout.PLANT_SCALE, false);
        layer.addActor(actor);
        return actor;
    }

    private void updateOctopus(PlantCovering covering, PamActor actor) {
        Plant plant = covering.getCoveredPlant();
        float worldX = layout.worldX(covering.displayX());
        float worldY = layout.worldYForRow(covering.displayY());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(worldX - actor.getWidth() / 2f, worldY);
        if (covering.isInFlight()) {
            if (!PlantClips.OCTOPUS_FLY_CLIP.equals(actor.clipName())) {
                actor.setClip(PlantClips.OCTOPUS_PATH, PlantClips.OCTOPUS_FLY_CLIP,
                        LawnLayout.PLANT_SCALE, false);
            }
            actor.setDrawScale(LawnLayout.PLANT_SCALE);
        } else if (PlantClips.OCTOPUS_IDLE_CLIP.equals(actor.clipName())
                || PlantClips.OCTOPUS_LAND_CLIP.equals(actor.clipName())
                || actor.hasFollowUp()) {
            actor.setDrawScale(LawnLayout.PLANT_SCALE);
        } else {
            actor.playThen(PlantClips.OCTOPUS_PATH, PlantClips.OCTOPUS_LAND_CLIP,
                    LawnLayout.PLANT_SCALE, PlantClips.OCTOPUS_IDLE_CLIP, true, null);
        }
        actor.setUserObject(sortKey(plant, null, 3));
        octopusHits.observe(covering, covering.getHealth(), actor);
    }

    private PamActor spawnSheep(Plant plant) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.playThen(PlantClips.SHEEP_PATH, PlantClips.SHEEP_INTRO_CLIP, LawnLayout.PLANT_SCALE,
                PlantClips.SHEEP_IDLE_CLIP, true, null);
        layer.addActor(actor);
        return actor;
    }

    private void updateSheep(Plant plant, PamActor actor) {
        Vector2 center = layout.cellCenter(plant.getCol(), plant.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        if (actor.hasFollowUp()) {
            actor.setDrawScale(LawnLayout.PLANT_SCALE);
        } else {
            actor.setClip(PlantClips.SHEEP_PATH, PlantClips.SHEEP_IDLE_CLIP, LawnLayout.PLANT_SCALE, true);
        }
        actor.setUserObject(sortKey(plant, null, 3));
    }

    private static List<Plant> liveSheep(List<Plant> live) {
        List<Plant> sheeped = new ArrayList<>();
        for (Plant plant : live) {
            if (plant.isCatTransformed()) {
                sheeped.add(plant);
            }
        }
        return sheeped;
    }

    private static List<PlantCovering> liveOctopi(GameSession session) {
        List<PlantCovering> live = new ArrayList<>();
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (covering != null
                    && covering.isAlive()
                    && covering.getType() == PlantCovering.Type.OCTOPUS
                    && covering.getCoveredPlant() != null
                    && covering.getCoveredPlant().isAlive()
                    && !covering.isHeld()) {
                live.add(covering);
            }
        }
        return live;
    }

    private static String followUpClip(
            Plant plant, EntityAnimationCatalog.ClipSpec spec, EntityAnimationCatalog.ClipSpec idle,
            List<Zombie> zombies) {
        String clip = spec == null ? null : spec.clip();
        if ("down".equals(clip) || "down_attack".equals(clip)) {
            return PlantVisualState.cactusDown(plant, zombies) ? "down_idle" : idle.clip();
        }
        if (plant.isUsingPlantFood()) {
            if ("plantfood_on".equals(clip)) {
                if ("Rotobaga".equals(plant.getName())) {
                    return idle.clip();
                }
                if ("Snow Pea".equals(plant.getName()) || "Bowling Bulb".equals(plant.getName())) {
                    return plantFoodFollowUp(plant);
                }
                return "plantfood";
            }
            if ("plantfood_start".equals(clip)) {
                return "plantfood_loop";
            }
            if ("plantfood_end".equals(clip)) {
                return idle.clip();
            }
            if ("Sun-shroom".equals(plant.getName()) && clip != null && clip.startsWith("plantfood_stage")) {
                return "idle_stage" + plant.pamStage();
            }
            if ("plantfood_off".equals(clip) || "plantfood2".equals(clip)) {
                return idle.clip();
            }
            if ("Cactus".equals(plant.getName()) && "plantfood".equals(clip)) {
                return "idle_plantfood";
            }
            if ("Fire Peashooter".equals(plant.getName()) && "plantfood".equals(clip)) {
                return "plantfood_loop";
            }
            if ("Fire Peashooter".equals(plant.getName()) && "plantfood_end".equals(clip)) {
                return idle.clip();
            }
            if ("Puff-shroom".equals(plant.getName()) && "plantfood_on".equals(clip)) {
                return "plantfood";
            }
            if ("Sea-shroom".equals(plant.getName()) && "pf".equals(clip)) {
                return idle.clip();
            }
        }
        return idle.clip();
    }

    private static String plantFoodFollowUp(Plant plant) {
        if ("Bowling Bulb".equals(plant.getName())) {
            return "plantfood_idle";
        }
        return "plantfood";
    }

    private static int iceHealth(Plant plant, GameSession session) {
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (covering != null
                    && covering.isAlive()
                    && covering.getCoveredPlant() == plant
                    && covering.getType() == PlantCovering.Type.HUNTER_ICE) {
                return covering.getHealth();
            }
        }
        return Integer.MAX_VALUE;
    }

    private static int freezeLevel(Plant plant, GameSession session) {
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (covering != null
                    && covering.isAlive()
                    && covering.getCoveredPlant() == plant
                    && covering.getType() == PlantCovering.Type.HUNTER_ICE) {
                return 2;
            }
        }
        if (plant.getHostileIceStacks(null) >= 1) {
            return 1;
        }
        return 0;
    }

    private static boolean isOctopusCovered(Plant plant, GameSession session) {
        for (PlantCovering covering : session.getPlantCoverings()) {
            if (covering != null
                    && covering.isAlive()
                    && covering.getCoveredPlant() == plant
                    && covering.getType() == PlantCovering.Type.OCTOPUS
                    && !covering.isHeld()) {
                return true;
            }
        }
        return false;
    }

    private static int sortKey(Plant plant, GameBoard board, int extraDepth) {
        int depth = extraDepth;
        if (extraDepth == 0 && board != null && board.getOverlayPlantAt(plant.getCol(), plant.getRow()) == plant) {
            depth = 1;
        }
        return plant.getRow() * 8 + depth;
    }
}
