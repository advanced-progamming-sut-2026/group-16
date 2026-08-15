package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.widget.PvzButtons;


public final class SpeedButton {
    private final Image icon;
    private final Actor root;
    private final GameAssets assets;

    public SpeedButton(GameAssets assets, Runnable onClick) {
        this.assets = assets;
        icon = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.SPEED_2X)));
        icon.setScaling(Scaling.fit);
        icon.setFillParent(true);
        Stack stack = new Stack();
        stack.setSize(84f, 84f);
        stack.add(icon);
        PvzButtons.animate(stack, 1.1f, 0.9f, onClick);
        root = stack;
    }

    public Actor actor() {
        return root;
    }

    public void setSpeed(int speed) {
        TextureRegion region = assets.region(speed > 1 ? LawnAssetIds.SPEED_2X_SELECTED : LawnAssetIds.SPEED_2X);
        icon.setDrawable(new TextureRegionDrawable(region));
    }
}
