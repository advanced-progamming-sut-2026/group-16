package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;


public final class PanelLabels {
    private PanelLabels() {
    }


    public static Label title(Skin skin, String text) {
        Label label = new Label(text, skin, "big");
        label.setColor(panelText(skin));
        return label;
    }


    public static Label body(Skin skin, String text) {
        Label label = new Label(text, skin, "medium");
        label.setColor(panelText(skin));
        label.setWrap(true);
        return label;
    }


    public static Color panelText(Skin skin) {
        if (skin.has("DarkBrown", Color.class)) {
            return skin.getColor("DarkBrown").cpy();
        }
        if (skin.has("black", Color.class)) {
            return skin.getColor("black").cpy();
        }
        return Color.BLACK.cpy();
    }
}
