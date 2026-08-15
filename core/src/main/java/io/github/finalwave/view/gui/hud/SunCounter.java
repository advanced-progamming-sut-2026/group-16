package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;


public final class SunCounter extends Table {
    private final Label value;

    public SunCounter(GameAssets assets) {
        Image icon = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.SUN_ICON)));
        icon.setScaling(Scaling.fit);
        value = new Label("0", assets.skin(), "medium");
        value.setAlignment(Align.left);
        add(icon).size(64f, 64f).padRight(8f);
        add(value);
    }

    public void setAmount(int amount) {
        value.setText(String.valueOf(Math.max(0, amount)));
    }
}
