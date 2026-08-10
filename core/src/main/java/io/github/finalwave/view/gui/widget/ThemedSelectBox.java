package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;


public final class ThemedSelectBox {
    private ThemedSelectBox() {
    }

    public static <T> SelectBox<T> create(Skin skin) {
        SelectBox<T> selectBox = new SelectBox<>(buildStyle(skin));
        selectBox.setAlignment(Align.center);
        selectBox.getList().setAlignment(Align.left);
        return selectBox;
    }

    public static SelectBox.SelectBoxStyle buildStyle(Skin skin) {
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle();
        style.font = resolveFont(skin);
        style.fontColor = new Color(0.35f, 0.18f, 0.05f, 1f);
        style.disabledFontColor = Color.GRAY.cpy();

        Drawable wood = resolveWoodBackground(skin);
        style.background = wood;
        style.backgroundOpen = wood;
        style.backgroundOver = wood;
        style.backgroundDisabled = wood;

        if (skin.has("default", List.ListStyle.class)) {
            List.ListStyle listStyle = new List.ListStyle(skin.get("default", List.ListStyle.class));
            listStyle.font = style.font;
            listStyle.fontColorSelected = Color.WHITE.cpy();
            listStyle.fontColorUnselected = style.fontColor.cpy();
            if (wood != null) {
                listStyle.background = wood;
            }
            style.listStyle = listStyle;
        } else {
            style.listStyle = new List.ListStyle(style.font, Color.WHITE, style.fontColor, wood);
        }

        if (skin.has("default", ScrollPane.ScrollPaneStyle.class)) {
            style.scrollStyle = skin.get("default", ScrollPane.ScrollPaneStyle.class);
        } else {
            style.scrollStyle = new ScrollPane.ScrollPaneStyle();
        }

        return style;
    }

    private static BitmapFont resolveFont(Skin skin) {


        if (skin.has("FBUSV8C5EI_1", BitmapFont.class)) {
            return skin.getFont("FBUSV8C5EI_1");
        }
        return skin.get(BitmapFont.class);
    }

    private static Drawable resolveWoodBackground(Skin skin) {
        if (skin.has("bundle_reward_multiplier", Label.LabelStyle.class)) {
            Drawable wood = skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background;
            if (wood != null) {
                return wood;
            }
        }
        if (skin.has("default", TextField.TextFieldStyle.class)) {
            Drawable fieldBg = skin.get("default", TextField.TextFieldStyle.class).background;
            if (fieldBg != null) {
                return fieldBg;
            }
        }
        if (skin.has("brown", TextButton.TextButtonStyle.class)) {
            return skin.get("brown", TextButton.TextButtonStyle.class).up;
        }
        return null;
    }
}
