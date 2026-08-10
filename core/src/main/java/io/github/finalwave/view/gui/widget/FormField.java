package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;


public final class FormField {
    private final Label label;
    private final TextField field;

    public FormField(Skin skin, String hint) {
        this(skin, hint, false);
    }

    public FormField(Skin skin, String hint, boolean password) {
        TextField.TextFieldStyle style = buildWoodStyle(skin);
        field = new TextField("", style);
        field.setMessageText(hint);
        field.setAlignment(Align.center);
        if (password) {
            field.setPasswordMode(true);
            field.setPasswordCharacter('*');
        }
        label = new Label(hint, skin, "secondary");
    }

    public TextField field() {
        return field;
    }

    public Label label() {
        return label;
    }

    public String text() {
        return field.getText() == null ? "" : field.getText().trim();
    }

    public void clear() {
        field.setText("");
    }

    public static TextField.TextFieldStyle buildWoodStyle(Skin skin) {
        TextField.TextFieldStyle base = skin.get(TextField.TextFieldStyle.class);
        TextField.TextFieldStyle style = new TextField.TextFieldStyle(base);
        if (skin.has("FBUSV8C5EI_1", com.badlogic.gdx.graphics.g2d.BitmapFont.class)) {
            style.font = skin.getFont("FBUSV8C5EI_1");
            style.messageFont = style.font;
        }
        if (skin.has("bundle_reward_multiplier", Label.LabelStyle.class)) {
            Drawable wood = skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background;
            if (wood != null) {
                style.background = wood;
                style.focusedBackground = wood;
            }
        }
        return style;
    }
}
