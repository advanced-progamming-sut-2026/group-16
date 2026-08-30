package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class IceShroomFxSync {
    public static final String TILE_PATH =
            "768/FULL/EFFECTS/ICESHROOM_TILE_FX/ICESHROOM_TILE_FX.PAM";
    public static final String MELEE_PATH =
            "768/FULL/EFFECTS/ICESHROOM_MELEE_ATTACK/ICESHROOM_MELEE_ATTACK.PAM";
    private static final String TILE_LOOP = "animation_loop";
    private static final String MELEE_CLIP = "animation";
    private static final float TILE_SCALE = 1.0f;
    private static final float MELEE_SCALE = 1.05f;
    private static final int AURA_RADIUS = 1;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<String, PamActor> tileActors = new HashMap<>();
    private final Map<Plant, PamActor> meleeActors = new HashMap<>();

    public IceShroomFxSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getBoard() == null) {
            return;
        }
        GameBoard board = session.getBoard();
        Set<String> liveTiles = new HashSet<>();
        Set<Plant> liveMelee = new HashSet<>();
        for (Plant plant : board.getAllPlants()) {
            if (plant == null || !plant.isAlive() || !"Ice-shroom".equals(plant.getName())) {
                continue;
            }
            int centerCol = plant.getCol();
            int centerRow = plant.getRow();
            for (int row = centerRow - AURA_RADIUS; row <= centerRow + AURA_RADIUS; row++) {
                for (int col = centerCol - AURA_RADIUS; col <= centerCol + AURA_RADIUS; col++) {
                    if (!board.inBounds(col, row)) {
                        continue;
                    }
                    String key = key(col, row);
                    liveTiles.add(key);
                    PamActor actor = tileActors.get(key);
                    if (actor == null) {
                        actor = spawnTile();
                        tileActors.put(key, actor);
                    }
                    layoutTile(actor, col, row);
                }
            }
            if (plant.getIceShroomAttackTicks() > 0) {
                liveMelee.add(plant);
                PamActor melee = meleeActors.get(plant);
                if (melee == null) {
                    melee = spawnMelee();
                    meleeActors.put(plant, melee);
                }
                layoutMelee(melee, plant.getCol(), plant.getRow());
            }
        }
        Iterator<Map.Entry<String, PamActor>> tileIterator = tileActors.entrySet().iterator();
        while (tileIterator.hasNext()) {
            Map.Entry<String, PamActor> entry = tileIterator.next();
            if (!liveTiles.contains(entry.getKey())) {
                entry.getValue().remove();
                tileIterator.remove();
            }
        }
        Iterator<Map.Entry<Plant, PamActor>> meleeIterator = meleeActors.entrySet().iterator();
        while (meleeIterator.hasNext()) {
            Map.Entry<Plant, PamActor> entry = meleeIterator.next();
            if (!liveMelee.contains(entry.getKey())) {
                entry.getValue().remove();
                meleeIterator.remove();
            }
        }
    }

    public void clear() {
        for (PamActor actor : tileActors.values()) {
            actor.remove();
        }
        tileActors.clear();
        for (PamActor actor : meleeActors.values()) {
            actor.remove();
        }
        meleeActors.clear();
    }

    private PamActor spawnTile() {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, 0.5f);
        actor.setClip(TILE_PATH, TILE_LOOP, TILE_SCALE, true);
        layer.addActor(actor);
        return actor;
    }

    private PamActor spawnMelee() {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.setClip(MELEE_PATH, MELEE_CLIP, MELEE_SCALE, false);
        layer.addActor(actor);
        return actor;
    }

    private void layoutTile(PamActor actor, int col, int row) {
        Vector2 center = layout.cellCenter(col, row);
        float width = layout.tileWidth() * 1.08f;
        float height = layout.tileHeight() * 1.08f;
        actor.setSize(width, height);
        actor.setPosition(center.x - width / 2f, center.y - height / 2f);
        actor.setUserObject(row);
    }

    private void layoutMelee(PamActor actor, int col, int row) {
        Vector2 center = layout.cellCenter(col, row);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        actor.setUserObject(row * 8 + 1);
    }

    private static String key(int col, int row) {
        return col + ":" + row;
    }
}
