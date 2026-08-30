package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.ability.TangleKelpGrabMark;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.TangleKelpClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.List;


public final class TangleKelpGrabSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;

    public TangleKelpGrabSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getTangleKelpGrabSystem() == null) {
            return;
        }
        List<TangleKelpGrabMark> marks = session.getTangleKelpGrabSystem().drainGrabMarks();
        for (TangleKelpGrabMark mark : marks) {
            if (mark == null) {
                continue;
            }
            spawnGrab(mark.row(), mark.col());
        }
    }

    public void clear() {
        layer.clearChildren();
    }

    private void spawnGrab(int row, int col) {
        var attack = TangleKelpClips.remoteAttack();
        PamActor fx = assets.pamActor();
        fx.setTouchable(Touchable.disabled);
        fx.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        fx.setSize(layout.tileWidth(), layout.tileHeight());
        Vector2 center = layout.cellCenter(col, row);
        fx.setPosition(center.x - fx.getWidth() / 2f, center.y - fx.getHeight() / 2f);
        layer.addActor(fx);
        fx.loadPamSync(attack.path());
        float scale = LawnLayout.PLANT_SCALE;
        fx.playOnce(attack.path(), attack.clip(), scale, fx::remove);
    }
}
