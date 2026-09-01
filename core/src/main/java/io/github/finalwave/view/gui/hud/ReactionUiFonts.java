package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public final class ReactionUiFonts {
    private static Label.LabelStyle emojiStyle;
    private static Label.LabelStyle fallbackStyle;
    private static FreeTypeFontGenerator generator;

    private ReactionUiFonts() {
    }

    public static Label emojiLabel(String glyph) {
        Label label = new Label(glyph == null ? "" : glyph, emojiStyle());
        label.setAlignment(com.badlogic.gdx.utils.Align.center);
        return label;
    }

    public static Label fallbackLabel(String text) {
        Label label = new Label(text == null ? "" : text, fallbackStyle());
        label.setAlignment(com.badlogic.gdx.utils.Align.center);
        return label;
    }

    public static Label.LabelStyle emojiStyle() {
        if (emojiStyle == null) {
            BitmapFont font = loadEmojiFont();
            emojiStyle = new Label.LabelStyle(font, Color.WHITE);
        }
        return emojiStyle;
    }

    public static Label.LabelStyle fallbackStyle() {
        if (fallbackStyle == null) {
            fallbackStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        }
        return fallbackStyle;
    }

    public static void dispose() {
        if (generator != null) {
            generator.dispose();
            generator = null;
        }
        if (emojiStyle != null && emojiStyle.font != null) {
            emojiStyle.font.dispose();
            emojiStyle = null;
        }
        if (fallbackStyle != null && fallbackStyle.font != null) {
            fallbackStyle.font.dispose();
            fallbackStyle = null;
        }
    }

    private static BitmapFont loadEmojiFont() {
        FileHandle fontFile = resolveEmojiFontFile();
        if (fontFile != null && fontFile.exists()) {
            try {
                generator = new FreeTypeFontGenerator(fontFile);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 48;
                parameter.characters = "❤️🔥💀💪";
                parameter.color = Color.WHITE;
                parameter.borderWidth = 0f;
                return generator.generateFont(parameter);
            } catch (RuntimeException ignored) {
                if (generator != null) {
                    generator.dispose();
                    generator = null;
                }
            }
        }
        return new BitmapFont();
    }

    private static FileHandle resolveEmojiFontFile() {
        if (Gdx.files == null) {
            return null;
        }
        String[] candidates = {
                System.getenv("WINDIR") + "/Fonts/seguiemj.ttf",
                System.getenv("WINDIR") + "/Fonts/segoeuiemoji.ttf",
                "/System/Library/Fonts/Apple Color Emoji.ttc",
                "/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf",
        };
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            FileHandle file = Gdx.files.absolute(candidate);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }
}
