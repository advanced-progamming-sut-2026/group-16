package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;


public final class ThemedCheckBox {
    private static final String[] OFF_CANDIDATES = {
            "image_ui_mainmenu_checkbox_disabled",
            "image_ui_almanac_checkbox_disabled",
            "checkbox_off",
            "image_ui_generic_radio_up"
    };
    private static final String[] ON_CANDIDATES = {
            "image_ui_mainmenu_checkbox_enabled",
            "image_ui_almanac_checkbox_enabled",
            "checkbox_on",
            "image_ui_generic_radio_down"
    };

    private ThemedCheckBox() {
    }

    public static CheckBox create(Skin skin, String label) {
        CheckBox checkBox = new CheckBox(label, buildStyle(skin));
        checkBox.getLabelCell().padLeft(10);
        checkBox.getLabel().setColor(PanelLabels.panelText(skin));
        return checkBox;
    }

    public static CheckBox.CheckBoxStyle buildStyle(Skin skin) {
        CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle();
        style.checkboxOff = requireDrawable(skin, OFF_CANDIDATES);
        style.checkboxOn = requireDrawable(skin, ON_CANDIDATES);
        style.checkboxOver = style.checkboxOff;
        style.checkboxOnOver = style.checkboxOn;
        style.font = resolveFont(skin);
        style.fontColor = PanelLabels.panelText(skin);
        return style;
    }

    private static Drawable requireDrawable(Skin skin, String... names) {
        for (String name : names) {
            Drawable drawable = findDrawable(skin, name);
            if (drawable != null) {
                return drawable;
            }
        }
        throw new IllegalStateException(
                "pvz-skin is missing checkbox drawables. Tried: " + String.join(", ", names));
    }


    private static Drawable findDrawable(Skin skin, String name) {
        if (skin.has(name, Drawable.class)) {
            return skin.getDrawable(name);
        }
        if (skin.has(name, TextureRegion.class)) {
            return new TextureRegionDrawable(skin.getRegion(name));
        }
        TextureAtlas atlas = skin.getAtlas();
        if (atlas != null) {
            TextureRegion region = atlas.findRegion(name);
            if (region != null) {
                return new TextureRegionDrawable(region);
            }
        }
        try {
            return skin.getDrawable(name);
        } catch (GdxRuntimeException ignored) {
            return null;
        }
    }

    private static BitmapFont resolveFont(Skin skin) {
        if (skin.has("medium", Label.LabelStyle.class)) {
            BitmapFont font = skin.get("medium", Label.LabelStyle.class).font;
            if (font != null) {
                return font;
            }
        }
        if (skin.has("FBUSV8C5EI_1", BitmapFont.class)) {
            return skin.getFont("FBUSV8C5EI_1");
        }
        return skin.get(BitmapFont.class);
    }
}
