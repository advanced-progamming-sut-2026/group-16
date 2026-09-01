package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.math.Vector2;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.ExplosiveAbility;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.CherryBombClips;
import io.github.finalwave.view.gui.render.clip.PotatoMineClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class ExplosionSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<String, PendingDetonation> pending = new HashMap<>();
    private final Set<String> fxSpawned = new HashSet<>();

    public ExplosionSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session, PlantSync plantSync, float tickFraction) {
        if (session == null || session.getBoard() == null || plantSync == null) {
            return;
        }
        GameBoard board = session.getBoard();
        int currentTick = session.getCurrentTick();
        float tickSeconds = 1f / GameSession.TICKS_PER_SECOND;
        List<Plant> live = new ArrayList<>();
        for (Plant plant : board.getAllPlants()) {
            if (plant != null && plant.isAlive()) {
                live.add(plant);
            }
        }
        for (Plant plant : live) {
            if (!plant.isAttacking()) {
                continue;
            }
            if (!(plant.getAbility() instanceof ExplosiveAbility explosive) || !explosive.isDetonating()) {
                continue;
            }
            DetonationKind kind = detonationKind(plant);
            if (kind == null) {
                continue;
            }
            pending.computeIfAbsent(plant.getId(), id -> new PendingDetonation(
                    plant.getCol(),
                    plant.getRow(),
                    kind,
                    currentTick));
            PamActor actor = plantSync.actorFor(plant);
            if (actor != null && fxSpawned.contains(plant.getId())) {
                actor.setVisible(false);
            }
        }
        Iterator<Map.Entry<String, PendingDetonation>> iterator = pending.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PendingDetonation> entry = iterator.next();
            String plantId = entry.getKey();
            PendingDetonation detonation = entry.getValue();
            float elapsed = (currentTick - detonation.startTick() + tickFraction) * tickSeconds;
            if (elapsed < fxDelaySeconds(detonation.kind())) {
                continue;
            }
            if (!fxSpawned.contains(plantId)) {
                spawnExplosion(detonation);
                fxSpawned.add(plantId);
            }
            iterator.remove();
        }
    }

    public void clear() {
        pending.clear();
        fxSpawned.clear();
        layer.clearChildren();
    }

    private static DetonationKind detonationKind(Plant plant) {
        if (plant.isCherryBomb()) {
            return DetonationKind.CHERRY_BOMB;
        }
        if (plant.isPrimalPotatoMine()) {
            return DetonationKind.PRIMAL_POTATO;
        }
        if (plant.isPotatoMine()) {
            return DetonationKind.POTATO;
        }
        return null;
    }

    private static float fxDelaySeconds(DetonationKind kind) {
        return switch (kind) {
            case CHERRY_BOMB -> CherryBombClips.DETONATION_FX_SECONDS;
            case POTATO, PRIMAL_POTATO -> PotatoMineClips.DETONATION_FX_SECONDS;
        };
    }

    private void spawnExplosion(PendingDetonation detonation) {
        if (detonation.kind() == DetonationKind.CHERRY_BOMB) {
            spawnCherryExplosion(detonation.col(), detonation.row());
            return;
        }
        spawnPotatoExplosion(detonation.col(), detonation.row(), detonation.kind() == DetonationKind.PRIMAL_POTATO);
    }

    private void spawnPotatoExplosion(int col, int row, boolean primal) {
        var spec = PotatoMineClips.explosion(primal);
        spawnFx(col, row, spec.path(), spec.clip());
        assets.audio().playExplosion();
    }

    private void spawnCherryExplosion(int col, int row) {
        var rear = CherryBombClips.explosionRear();
        var top = CherryBombClips.explosionTop();
        spawnFx(col, row, rear.path(), rear.clip());
        spawnFx(col, row, top.path(), top.clip());
        assets.audio().playExplosion();
    }

    private void spawnFx(int col, int row, String pamPath, String clipName) {
        PamActor fx = assets.pamActor();
        fx.setTouchable(Touchable.disabled);
        fx.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        fx.setSize(layout.tileWidth(), layout.tileHeight());
        Vector2 center = layout.cellCenter(col, row);
        fx.setPosition(center.x - fx.getWidth() / 2f, center.y - fx.getHeight() / 2f);
        layer.addActor(fx);
        fx.playOnce(pamPath, clipName, LawnLayout.PLANT_SCALE, fx::remove);
    }

    private enum DetonationKind {
        POTATO,
        PRIMAL_POTATO,
        CHERRY_BOMB
    }

    private record PendingDetonation(int col, int row, DetonationKind kind, int startTick) {
    }
}
