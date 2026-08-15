package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.widget.PvzButtons;


public final class PauseButton {
    private PauseButton() {
    }

    public static Actor create(GameAssets assets, Runnable onClick) {
        return PvzButtons.iconButton(assets.region(LawnAssetIds.PAUSE), 84f, 84f, onClick);
    }
}
