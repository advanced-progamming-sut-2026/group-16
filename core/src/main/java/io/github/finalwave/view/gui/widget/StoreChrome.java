package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;


public final class StoreChrome {
    public static final float PANEL_PAD_LEFT = 32f;
    public static final float PANEL_PAD_RIGHT = 32f;
    public static final float PANEL_PAD_TOP = 28f;
    public static final float PANEL_PAD_BOTTOM = 32f;

    private static final int GEM_LEFT = 52;
    private static final int GEM_RIGHT = 18;
    private static final int GEM_VERT = 16;
    private static final int COIN_EDGE = 16;
    private static final int COIN_VERT = 12;
    private static final int RIBBON_SIDE = 28;
    private static final int RIBBON_CAP = 10;
    private static final int TAB_BODY_LEFT = 12;
    private static final int TAB_BODY_RIGHT = 12;
    private static final int TAB_BODY_TOP = 12;
    private static final int TAB_BODY_BOTTOM = 2;
    private static final int PANEL_PATCH = 128;
    private static final int PANEL_SPLIT = 44;
    private static final float PANEL_RADIUS = 36f;
    private static final float PANEL_RIM = 16f;
    private static final Color WOOD_FILL = rgb(172, 112, 66);
    private static final Color WOOD_RIM = rgb(145, 98, 56);
    private static final Color WOOD_HIGHLIGHT = rgb(232, 180, 104);
    private static final Color WOOD_OUTER = rgb(22, 14, 8);
    private static final Color WOOD_INNER = rgb(48, 32, 18);

    private static Texture panelTexture;
    private static NinePatchDrawable panelDrawable;
    private static Texture purpleUpTexture;
    private static Texture purpleDownTexture;
    private static NinePatchDrawable purpleUpDrawable;
    private static NinePatchDrawable purpleDownDrawable;
    private static Texture brownUpTexture;
    private static Texture brownDownTexture;
    private static NinePatchDrawable brownUpDrawable;
    private static NinePatchDrawable brownDownDrawable;
    private static Texture volumeTrackTexture;
    private static NinePatchDrawable volumeTrackDrawable;

    private static final int BUTTON_PATCH = 64;
    private static final int BUTTON_SPLIT = 22;
    private static final float BUTTON_RADIUS = 18f;
    private static final float BUTTON_RIM = 5f;
    private static final Color PURPLE_FILL = rgb(162, 74, 220);
    private static final Color PURPLE_FILL_DOWN = rgb(126, 48, 178);
    private static final Color PURPLE_RIM = rgb(242, 226, 255);
    private static final Color PURPLE_OUTER = rgb(58, 22, 92);
    private static final Color PURPLE_SHINE = rgb(214, 168, 255);
    private static final Color BROWN_FILL = rgb(176, 82, 48);
    private static final Color BROWN_FILL_DOWN = rgb(138, 54, 30);
    private static final Color BROWN_RIM = rgb(248, 238, 214);
    private static final Color BROWN_OUTER = rgb(62, 28, 16);
    private static final Color BROWN_SHINE = rgb(214, 126, 86);
    private static final int TRACK_PATCH = 48;
    private static final int TRACK_SPLIT = 20;
    private static final float TRACK_RADIUS = 16f;
    private static final Color TRACK_FILL = rgb(52, 122, 48);
    private static final Color TRACK_RIM = rgb(28, 72, 28);
    private static final Color TRACK_SHINE = rgb(86, 168, 72);

    private StoreChrome() {
    }

    public static Drawable panel() {
        if (panelDrawable == null) {
            panelDrawable = new NinePatchDrawable(buildPanelPatch());
        }
        return panelDrawable;
    }

    public static void dispose() {
        if (panelTexture != null) {
            panelTexture.dispose();
            panelTexture = null;
            panelDrawable = null;
        }
        if (purpleUpTexture != null) {
            purpleUpTexture.dispose();
            purpleUpTexture = null;
            purpleUpDrawable = null;
        }
        if (purpleDownTexture != null) {
            purpleDownTexture.dispose();
            purpleDownTexture = null;
            purpleDownDrawable = null;
        }
        if (brownUpTexture != null) {
            brownUpTexture.dispose();
            brownUpTexture = null;
            brownUpDrawable = null;
        }
        if (brownDownTexture != null) {
            brownDownTexture.dispose();
            brownDownTexture = null;
            brownDownDrawable = null;
        }
        if (volumeTrackTexture != null) {
            volumeTrackTexture.dispose();
            volumeTrackTexture = null;
            volumeTrackDrawable = null;
        }
    }

    public static Drawable card(TextureRegion region) {
        return new TextureRegionDrawable(region);
    }

    public static Drawable previewGlow(TextureRegion region) {
        return new TextureRegionDrawable(region);
    }

    public static Drawable ribbon(TextureRegion region) {
        return patch(region, RIBBON_SIDE, RIBBON_SIDE, RIBBON_CAP, RIBBON_CAP);
    }

    public static Drawable gemButton(TextureRegion region) {
        return patch(region, GEM_LEFT, GEM_RIGHT, GEM_VERT, GEM_VERT);
    }

    public static Drawable coinButton(TextureRegion region) {
        return patch(region, COIN_EDGE, COIN_EDGE, COIN_VERT, COIN_VERT);
    }

    public static Drawable purpleButton() {
        if (purpleUpDrawable == null) {
            purpleUpTexture = buttonTexture(PURPLE_FILL, PURPLE_SHINE, PURPLE_RIM, PURPLE_OUTER);
            purpleUpDrawable = new NinePatchDrawable(new NinePatch(
                    purpleUpTexture, BUTTON_SPLIT, BUTTON_SPLIT, BUTTON_SPLIT, BUTTON_SPLIT));
        }
        return purpleUpDrawable;
    }

    public static Drawable purpleButtonDown() {
        if (purpleDownDrawable == null) {
            purpleDownTexture = buttonTexture(PURPLE_FILL_DOWN, PURPLE_FILL, PURPLE_RIM, PURPLE_OUTER);
            purpleDownDrawable = new NinePatchDrawable(new NinePatch(
                    purpleDownTexture, BUTTON_SPLIT, BUTTON_SPLIT, BUTTON_SPLIT, BUTTON_SPLIT));
        }
        return purpleDownDrawable;
    }

    public static Drawable brownButton() {
        if (brownUpDrawable == null) {
            brownUpTexture = buttonTexture(BROWN_FILL, BROWN_SHINE, BROWN_RIM, BROWN_OUTER);
            brownUpDrawable = new NinePatchDrawable(new NinePatch(
                    brownUpTexture, BUTTON_SPLIT, BUTTON_SPLIT, BUTTON_SPLIT, BUTTON_SPLIT));
        }
        return brownUpDrawable;
    }

    public static Drawable brownButtonDown() {
        if (brownDownDrawable == null) {
            brownDownTexture = buttonTexture(BROWN_FILL_DOWN, BROWN_FILL, BROWN_RIM, BROWN_OUTER);
            brownDownDrawable = new NinePatchDrawable(new NinePatch(
                    brownDownTexture, BUTTON_SPLIT, BUTTON_SPLIT, BUTTON_SPLIT, BUTTON_SPLIT));
        }
        return brownDownDrawable;
    }

    public static Drawable volumeTrack() {
        if (volumeTrackDrawable == null) {
            volumeTrackTexture = capsuleTexture(TRACK_FILL, TRACK_SHINE, TRACK_RIM);
            volumeTrackDrawable = new NinePatchDrawable(new NinePatch(
                    volumeTrackTexture, TRACK_SPLIT, TRACK_SPLIT, TRACK_SPLIT, TRACK_SPLIT));
            volumeTrackDrawable.setMinHeight(28f);
            volumeTrackDrawable.setMinWidth(0f);
            volumeTrackDrawable.setLeftWidth(0f);
            volumeTrackDrawable.setRightWidth(0f);
            volumeTrackDrawable.setTopHeight(0f);
            volumeTrackDrawable.setBottomHeight(0f);
        }
        return volumeTrackDrawable;
    }

    public static Drawable tabBody(TextureRegion region) {
        return patch(region, TAB_BODY_LEFT, TAB_BODY_RIGHT, TAB_BODY_TOP, TAB_BODY_BOTTOM);
    }

    public static Drawable disabledButton(TextureRegion region) {
        return patch(region, COIN_EDGE, COIN_EDGE, COIN_VERT, COIN_VERT);
    }

    private static NinePatch buildPanelPatch() {
        Pixmap pixmap = new Pixmap(PANEL_PATCH, PANEL_PATCH, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float cx = (PANEL_PATCH - 1) * 0.5f;
        float cy = (PANEL_PATCH - 1) * 0.5f;
        float half = PANEL_PATCH * 0.5f - 1f;
        Color mix = new Color();
        for (int y = 0; y < PANEL_PATCH; y++) {
            for (int x = 0; x < PANEL_PATCH; x++) {
                float sdf = roundedBox(x + 0.5f, y + 0.5f, cx, cy, half, half, PANEL_RADIUS);
                float cover = smooth(0.75f, -0.75f, sdf);
                if (cover <= 0.004f) {
                    pixmap.drawPixel(x, y, 0);
                    continue;
                }
                float depth = -sdf;
                Color tone;
                if (depth < 1.3f) {
                    tone = WOOD_OUTER;
                } else if (depth < PANEL_RIM) {
                    float along = (depth - 1.3f) / (PANEL_RIM - 1.3f);
                    mix.set(WOOD_OUTER).lerp(WOOD_RIM, along);
                    float topGlow = MathUtils.clamp((0.42f - y / (float) PANEL_PATCH) / 0.42f, 0f, 1f);
                    mix.lerp(WOOD_HIGHLIGHT, topGlow * 0.55f);
                    tone = mix;
                } else if (depth < PANEL_RIM + 1.6f) {
                    tone = WOOD_INNER;
                } else {
                    tone = WOOD_FILL;
                }
                mix.set(tone);
                mix.a = cover;
                pixmap.drawPixel(x, y, Color.rgba8888(mix));
            }
        }
        panelTexture = new Texture(pixmap);
        panelTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return new NinePatch(panelTexture, PANEL_SPLIT, PANEL_SPLIT, PANEL_SPLIT, PANEL_SPLIT);
    }

    private static Texture buttonTexture(Color fill, Color shine, Color rim, Color outer) {
        Pixmap pixmap = new Pixmap(BUTTON_PATCH, BUTTON_PATCH, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float cx = (BUTTON_PATCH - 1) * 0.5f;
        float cy = (BUTTON_PATCH - 1) * 0.5f;
        float half = BUTTON_PATCH * 0.5f - 1f;
        Color mix = new Color();
        for (int y = 0; y < BUTTON_PATCH; y++) {
            for (int x = 0; x < BUTTON_PATCH; x++) {
                float sdf = roundedBox(x + 0.5f, y + 0.5f, cx, cy, half, half, BUTTON_RADIUS);
                float cover = smooth(0.75f, -0.75f, sdf);
                if (cover <= 0.004f) {
                    pixmap.drawPixel(x, y, 0);
                    continue;
                }
                float depth = -sdf;
                Color tone;
                if (depth < 1.2f) {
                    tone = outer;
                } else if (depth < BUTTON_RIM) {
                    mix.set(outer).lerp(rim, (depth - 1.2f) / (BUTTON_RIM - 1.2f));
                    tone = mix;
                } else {
                    float topGlow = MathUtils.clamp((0.38f - y / (float) BUTTON_PATCH) / 0.38f, 0f, 1f);
                    mix.set(fill).lerp(shine, topGlow * 0.42f);
                    tone = mix;
                }
                mix.set(tone);
                mix.a = cover;
                pixmap.drawPixel(x, y, Color.rgba8888(mix));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private static Texture capsuleTexture(Color fill, Color shine, Color rim) {
        Pixmap pixmap = new Pixmap(TRACK_PATCH, TRACK_PATCH, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float cx = (TRACK_PATCH - 1) * 0.5f;
        float cy = (TRACK_PATCH - 1) * 0.5f;
        float half = TRACK_PATCH * 0.5f - 1f;
        Color mix = new Color();
        for (int y = 0; y < TRACK_PATCH; y++) {
            for (int x = 0; x < TRACK_PATCH; x++) {
                float sdf = roundedBox(x + 0.5f, y + 0.5f, cx, cy, half, half, TRACK_RADIUS);
                float cover = smooth(0.75f, -0.75f, sdf);
                if (cover <= 0.004f) {
                    pixmap.drawPixel(x, y, 0);
                    continue;
                }
                float depth = -sdf;
                Color tone;
                if (depth < 1.35f) {
                    tone = rim;
                } else {
                    float topGlow = MathUtils.clamp((0.45f - y / (float) TRACK_PATCH) / 0.45f, 0f, 1f);
                    mix.set(fill).lerp(shine, topGlow * 0.42f);
                    tone = mix;
                }
                mix.set(tone);
                mix.a = cover;
                pixmap.drawPixel(x, y, Color.rgba8888(mix));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
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

    private static Color rgb(int r, int g, int b) {
        return new Color(r / 255f, g / 255f, b / 255f, 1f);
    }

    private static Drawable patch(TextureRegion region, int left, int right, int top, int bottom) {
        if (region.getRegionWidth() <= left + right || region.getRegionHeight() <= top + bottom) {
            return new TextureRegionDrawable(region);
        }
        return new NinePatchDrawable(new NinePatch(region, left, right, top, bottom));
    }
}
