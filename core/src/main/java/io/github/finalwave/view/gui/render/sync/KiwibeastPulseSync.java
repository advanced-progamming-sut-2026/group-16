package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.ability.KiwibeastPulseMark;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.KiwibeastClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.List;


public final class KiwibeastPulseSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;

    public KiwibeastPulseSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getKiwibeastPulseSystem() == null) {
            return;
        }
        List<KiwibeastPulseMark> marks = session.getKiwibeastPulseSystem().drainPulseMarks();
        for (KiwibeastPulseMark mark : marks) {
            if (mark == null) {
                continue;
            }
            var pulse = mark.plantFood() ? KiwibeastClips.plantFoodPulse() : KiwibeastClips.attackPulse();
            spawnFx(mark.plantCol(), mark.plantRow(), pulse);
            var hit = KiwibeastClips.tileHit();
            for (KiwibeastPulseMark.HitTile tile : mark.hits()) {
                if (tile != null) {
                    spawnFx(tile.col(), tile.row(), hit);
                }
            }
        }
    }

    public void clear() {
        layer.clearChildren();
    }

    private void spawnFx(int col, int row, EntityAnimationCatalog.ClipSpec spec) {
        PamActor fx = assets.pamActor();
        fx.setTouchable(Touchable.disabled);
        fx.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        fx.setSize(layout.tileWidth(), layout.tileHeight());
        Vector2 center = layout.cellCenter(col, row);
        fx.setPosition(center.x - fx.getWidth() / 2f, center.y - fx.getHeight() / 2f);
        layer.addActor(fx);
        fx.loadPamSync(spec.path());
        fx.playOnce(spec.path(), spec.clip(), LawnLayout.PLANT_SCALE, fx::remove);
    }
}
