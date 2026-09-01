package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.widget.PvzButtons;


public final class ChatIconButton {
    private ChatIconButton() {
    }

    public static Actor create(GameAssets assets, Runnable onClick) {
        return PvzButtons.iconButton(assets.region(LawnAssetIds.SPEECH_BUBBLE), 84f, 84f, onClick);
    }
}
