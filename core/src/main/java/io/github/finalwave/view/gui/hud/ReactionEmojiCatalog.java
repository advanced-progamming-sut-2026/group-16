package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.network.match.MatchReactions;

import java.util.HashMap;
import java.util.Map;

public final class ReactionEmojiCatalog {
    private static final String[] PNG_FILES = {
            "EMOJI/heart.png",
            "EMOJI/fire.png",
            "EMOJI/skull.png",
            "EMOJI/biceps.png",
    };

    private static final String[] ASCII_FALLBACK = {"<3", "!!", "x_x", "+)"};

    private static final Map<String, Texture> TEXTURES = new HashMap<>();

    private ReactionEmojiCatalog() {
    }

    public static Actor iconFor(int index, float size) {
        int safe = clamp(index);
        Texture texture = textureFor(safe);
        if (texture != null) {
            Image image = new Image(new TextureRegionDrawable(texture));
            image.setScaling(Scaling.fit);
            image.setSize(size, size);
            image.setTouchable(Touchable.disabled);
            return sizedActor(image, size);
        }
        Label label = ReactionUiFonts.emojiLabel(MatchReactions.faces()[safe]);
        if (label.getStyle().font == null || label.getText().toString().contains("?")) {
            label = ReactionUiFonts.fallbackLabel(ASCII_FALLBACK[safe]);
        }
        label.setFontScale(size / 48f);
        label.setTouchable(Touchable.disabled);
        return sizedActor(label, size);
    }

    public static void dispose() {
        for (Texture texture : TEXTURES.values()) {
            texture.dispose();
        }
        TEXTURES.clear();
        ReactionUiFonts.dispose();
    }

    private static Actor sizedActor(Actor actor, float size) {
        Container<Actor> container = new Container<>(actor);
        container.size(size, size);
        container.setTouchable(Touchable.disabled);
        return container;
    }

    private static Texture textureFor(int index) {
        String path = PNG_FILES[clamp(index)];
        Texture cached = TEXTURES.get(path);
        if (cached != null) {
            return cached;
        }
        FileHandle file = resolveFile(path);
        if (file == null || !file.exists()) {
            return null;
        }
        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TEXTURES.put(path, texture);
        return texture;
    }

    private static FileHandle resolveFile(String path) {
        if (Gdx.files == null) {
            return null;
        }
        FileHandle internal = Gdx.files.internal(path);
        if (internal.exists()) {
            return internal;
        }
        FileHandle local = Gdx.files.local(path);
        if (local.exists()) {
            return local;
        }
        return null;
    }

    private static int clamp(int index) {
        int max = Math.min(PNG_FILES.length, MatchReactions.emojiCount());
        if (max <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(max - 1, index));
    }
}
