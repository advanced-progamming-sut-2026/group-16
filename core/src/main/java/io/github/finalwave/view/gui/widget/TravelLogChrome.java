package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import java.util.ArrayList;
import java.util.List;

public final class TravelLogChrome {
    public static final Color TIMER_YELLOW = Color.valueOf("FFD640");
    public static final Color TITLE_BROWN = Color.valueOf("5C3018");
    public static final Color CARD_TITLE = Color.valueOf("FFD640");

    private static final int PATCH = 64;
    private static final int SPLIT = 22;
    private static final List<Texture> textures = new ArrayList<>();

    private static Drawable dimBackdrop;
    private static Drawable claimBanner;
    private static Drawable badge;
    private static Drawable minigamePanel;
    private static Drawable minigameCard;
    private static Drawable rewardStrip;

    private TravelLogChrome() {
    }

    public static Drawable dimBackdrop() {
        if (dimBackdrop == null) {
            dimBackdrop = rounded(new Color(0f, 0f, 0f, 0.66f), 0f);
        }
        return dimBackdrop;
    }

    public static Drawable claimBanner() {
        if (claimBanner == null) {
            claimBanner = bordered(
                    Color.valueOf("FFF2A1"),
                    Color.valueOf("D4A827"),
                    12f,
                    3f);
        }
        return claimBanner;
    }

    public static Drawable badge() {
        if (badge == null) {
            badge = bordered(
                    Color.valueOf("D8322C"),
                    Color.valueOf("FFFFFF"),
                    18f,
                    2f);
        }
        return badge;
    }

    public static Drawable minigamePanel() {
        if (minigamePanel == null) {
            minigamePanel = rounded(Color.valueOf("4A2818"), 14f);
        }
        return minigamePanel;
    }

    public static Drawable minigameCard() {
        if (minigameCard == null) {
            minigameCard = bordered(
                    new Color(45f / 255f, 33f / 255f, 28f / 255f, 0.76f),
                    Color.valueOf("8E6748"),
                    12f,
                    3f);
        }
        return minigameCard;
    }

    public static Drawable rewardStrip() {
        if (rewardStrip == null) {
            rewardStrip = rounded(new Color(0f, 0f, 0f, 0.58f), 8f);
        }
        return rewardStrip;
    }

    public static void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
        textures.clear();
        dimBackdrop = null;
        claimBanner = null;
        badge = null;
        minigamePanel = null;
        minigameCard = null;
        rewardStrip = null;
    }

    private static Drawable rounded(Color fill, float radius) {
        return build(fill, fill, radius, 0f);
    }

    private static Drawable bordered(Color fill, Color border, float radius, float borderWidth) {
        return build(fill, border, radius, borderWidth);
    }

    private static Drawable build(Color fill, Color border, float radius, float borderWidth) {
        Pixmap pixmap = new Pixmap(PATCH, PATCH, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float cx = (PATCH - 1) * 0.5f;
        float cy = (PATCH - 1) * 0.5f;
        float half = PATCH * 0.5f - 1f;
        Color pixel = new Color();
        for (int y = 0; y < PATCH; y++) {
            for (int x = 0; x < PATCH; x++) {
                float sdf = roundedBox(x + 0.5f, y + 0.5f, cx, cy, half, half, radius);
                float cover = smooth(0.75f, -0.75f, sdf);
                if (cover <= 0.004f) {
                    pixmap.drawPixel(x, y, 0);
                    continue;
                }
                pixel.set(sdf > -borderWidth ? border : fill);
                pixel.a *= cover;
                pixmap.drawPixel(x, y, Color.rgba8888(pixel));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        textures.add(texture);
        return new NinePatchDrawable(new NinePatch(texture, SPLIT, SPLIT, SPLIT, SPLIT));
    }

    private static float roundedBox(float x, float y, float cx, float cy, float halfW, float halfH, float radius) {
        float dx = Math.abs(x - cx) - (halfW - radius);
        float dy = Math.abs(y - cy) - (halfH - radius);
        float ox = Math.max(dx, 0f);
        float oy = Math.max(dy, 0f);
        return (float) Math.sqrt(ox * ox + oy * oy) + Math.min(Math.max(dx, dy), 0f) - radius;
    }

    private static float smooth(float edge0, float edge1, float value) {
        float t = MathUtils.clamp((value - edge0) / (edge1 - edge0), 0f, 1f);
        return t * t * (3f - 2f * t);
    }
}
