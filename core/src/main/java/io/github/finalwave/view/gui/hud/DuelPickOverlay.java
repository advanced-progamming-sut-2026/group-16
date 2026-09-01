package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.finalwave.controller.NetworkedIZombieController;
import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.view.api.minigame.DuelPickController;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.widget.PvzButtons;

import java.util.List;

public final class DuelPickOverlay extends Table {

    private static final float START_WIDTH = 280f;
    private static final float START_HEIGHT = 76f;

    private final Texture dimTexture;
    private final AdventurePickPanel pickPanel;

    public DuelPickOverlay(GameAssets assets, DuelPickController controller) {
        setFillParent(true);
        setTouchable(Touchable.enabled);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();
        dimTexture = new Texture(pixmap);
        pixmap.dispose();

        Skin skin = assets.skin();
        Image dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTexture)));
        dimmer.setFillParent(true);
        dimmer.setColor(1f, 1f, 1f, 0.55f);
        dimmer.setTouchable(Touchable.enabled);

        pickPanel = new AdventurePickPanel(assets, controller, bindingsFor(controller));

        TextButton ready = PvzButtons.textButton("LET'S ROCK!", skin, "green_small", controller::submitPicks);
        Table corner = new Table();
        corner.setTouchable(Touchable.childrenOnly);
        corner.bottom().right();
        corner.add(ready).size(START_WIDTH, START_HEIGHT).padRight(32f).padBottom(32f);

        Table screenHost = new Table();
        screenHost.setFillParent(true);
        screenHost.add(pickPanel).grow();

        Stack layers = new Stack();
        layers.setFillParent(true);
        layers.add(dimmer);
        layers.add(screenHost);
        layers.add(corner);
        addActor(layers);
    }

    @Override
    public boolean remove() {
        boolean removed = super.remove();
        if (dimTexture != null) {
            dimTexture.dispose();
        }
        return removed;
    }

    public void updateChrome() {
        if (pickPanel != null) {
            pickPanel.updateChrome();
        }
    }

    private static AdventurePickBindings bindingsFor(DuelPickController controller) {
        if (controller instanceof NetworkedIZombieController networked) {
            return new AdventurePickBindings() {
                @Override
                public CollectionPlantEntry plantEntry(String name) {
                    return networked.plantEntry(name);
                }

                @Override
                public CollectionPlantDetail plantDetail(String name) {
                    return networked.plantDetail(name);
                }

                @Override
                public int plantCost(String name) {
                    return networked.plantCost(name);
                }

                @Override
                public boolean plantSelectable(String name) {
                    return networked.plantSelectable(name);
                }

                @Override
                public boolean plantBoosted(String name) {
                    return networked.plantBoosted(name);
                }

                @Override
                public List<String> previewLaneNames() {
                    return networked.previewLaneNames();
                }
            };
        }
        return new AdventurePickBindings() {
        };
    }
}
