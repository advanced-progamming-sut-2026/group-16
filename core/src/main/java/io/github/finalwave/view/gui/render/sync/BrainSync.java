package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.render.LawnLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class BrainSync {
    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final Map<Integer, Image> brains = new HashMap<>();

    public BrainSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null || !session.isIZombieActive() || session.getBoard() == null) {
            clearLive();
            return;
        }
        int rows = session.getBoard().getRows();
        for (int row = 0; row < rows; row++) {
            if (session.isIZombieBrainEaten(row)) {
                Image existing = brains.remove(row);
                if (existing != null) {
                    existing.remove();
                }
                continue;
            }
            Image brain = brains.get(row);
            if (brain == null) {
                brain = spawn();
                brains.put(row, brain);
            }
            layoutBrain(brain, row);
        }
        Iterator<Map.Entry<Integer, Image>> iterator = brains.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Image> entry = iterator.next();
            if (entry.getKey() >= rows) {
                entry.getValue().remove();
                iterator.remove();
            }
        }
    }

    public void clear() {
        clearLive();
    }

    private void clearLive() {
        for (Image brain : brains.values()) {
            brain.remove();
        }
        brains.clear();
    }

    private Image spawn() {
        Image brain = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.BRAIN)));
        brain.setTouchable(Touchable.disabled);
        brain.setScaling(Scaling.fit);
        layer.addActor(brain);
        return brain;
    }

    private void layoutBrain(Image brain, int row) {
        float width = layout.tileWidth() * 0.72f;
        float height = layout.tileHeight() * 0.55f;
        float x = layout.mowerCenterX() - width / 2f;
        float y = layout.worldYForRow(row) + (layout.tileHeight() - height) / 2f;
        brain.setSize(width, height);
        brain.setPosition(x, y);
        brain.setVisible(true);
    }
}
