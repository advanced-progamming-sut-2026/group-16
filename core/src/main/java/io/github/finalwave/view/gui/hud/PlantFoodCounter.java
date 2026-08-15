package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.widget.PvzButtons;


public final class PlantFoodCounter extends Table {
    private final Label value;

    public PlantFoodCounter(GameAssets assets, Runnable onClick) {
        Actor button = PvzButtons.iconButton(assets.region(LawnAssetIds.PLANTFOOD_BANK), 84f, 84f, onClick);
        value = new Label("0", assets.skin(), "medium");
        value.setAlignment(Align.center);
        add(button).size(84f, 84f);
        add(value).padLeft(6f);
    }

    public void setCount(int count) {
        value.setText(String.valueOf(Math.max(0, count)));
    }
}
