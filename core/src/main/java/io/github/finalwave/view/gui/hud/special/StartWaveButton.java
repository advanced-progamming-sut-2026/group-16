package io.github.finalwave.view.gui.hud.special;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.widget.PriceButton;


public final class StartWaveButton extends Table {
    private static final float WIDTH = 268f;
    private static final float HEIGHT = 64f;

    private final Actor button;
    private boolean shown;

    public StartWaveButton(GameAssets assets, Runnable onStart) {
        button = PriceButton.labeled(
                assets.skin(),
                "LET'S ROCK!",
                WIDTH,
                HEIGHT,
                patch(assets, LawnAssetIds.PURPLE_BUTTON),
                patch(assets, LawnAssetIds.PURPLE_BUTTON_DOWN),
                onStart);
        add(button).size(WIDTH, HEIGHT);
        setVisible(false);
        shown = false;
    }

    public void refresh(GameSession session) {
        boolean active = session != null
                && session.isPrepPhaseActive()
                && !session.isSandboxPractice();
        if (active != shown) {
            shown = active;
            setVisible(active);
            invalidateHierarchy();
        }
    }

    @Override
    public float getPrefWidth() {
        return isVisible() ? WIDTH : 0f;
    }

    @Override
    public float getPrefHeight() {
        return isVisible() ? HEIGHT : 0f;
    }

    private static NinePatchDrawable patch(GameAssets assets, String imageId) {
        return new NinePatchDrawable(new NinePatch(assets.region(imageId), 28, 28, 16, 16));
    }
}
