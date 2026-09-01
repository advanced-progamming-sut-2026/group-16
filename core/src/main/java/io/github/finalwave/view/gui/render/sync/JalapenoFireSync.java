package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.ability.JalapenoFireMark;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.JalapenoClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.List;


public final class JalapenoFireSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;

    public JalapenoFireSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || session.getJalapenoFireSystem() == null) {
            return;
        }
        List<JalapenoFireMark> marks = session.getJalapenoFireSystem().drainFireMarks();
        for (JalapenoFireMark mark : marks) {
            if (mark == null) {
                continue;
            }
            spawnFire(mark.row(), mark.col());
        }
    }

    public void clear() {
        layer.clearChildren();
    }

    private void spawnFire(int row, int col) {
        var idle = JalapenoClips.fireIdle();
        var idle2 = JalapenoClips.fireIdle2();
        var idle3 = JalapenoClips.fireIdle3();
        PamActor fx = assets.pamActor();
        fx.setTouchable(Touchable.disabled);
        fx.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        fx.setSize(layout.tileWidth(), layout.tileHeight());
        Vector2 center = layout.cellCenter(col, row);
        fx.setPosition(center.x - fx.getWidth() / 2f, center.y - fx.getHeight() / 2f);
        layer.addActor(fx);
        fx.loadPamSync(idle.path());
        float scale = LawnLayout.PLANT_SCALE;
        fx.playOnce(idle.path(), idle.clip(), scale, () -> fx.playOnce(
                idle2.path(), idle2.clip(), scale, () -> fx.playOnce(
                        idle3.path(), idle3.clip(), scale, fx::remove)));
        assets.audio().playFirePea();
    }
}
