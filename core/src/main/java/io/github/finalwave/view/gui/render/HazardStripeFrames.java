package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;


public final class HazardStripeFrames implements Disposable {
    public static final float BLEED = 3f;

    private static final int SIZE = 160;
    private static final int STRIPE = 18;
    private static final float RADIUS = 10f;
    private static final Color YELLOW = new Color(1f, 0.86f, 0.08f, 0.96f);
    private static final Color BLACK = new Color(0.05f, 0.05f, 0.04f, 0.96f);

    private final Texture texture;
    private final TextureRegionDrawable drawable;

    public HazardStripeFrames() {
        texture = createTexture();
        drawable = new TextureRegionDrawable(new TextureRegion(texture));
    }

    public TextureRegionDrawable drawable() {
        return drawable;
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

    private static Texture createTexture() {
        Pixmap pixmap = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (!insideRounded(x + 0.5f, y + 0.5f, 0f, 0f, SIZE, SIZE, RADIUS)) {
                    continue;
                }
                boolean yellow = ((x + y) / STRIPE) % 2 == 0;
                pixmap.drawPixel(x, y, Color.rgba8888(yellow ? YELLOW : BLACK));
            }
        }
        Texture created = new Texture(pixmap);
        created.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return created;
    }

    private static boolean insideRounded(
            float x,
            float y,
            float left,
            float top,
            float width,
            float height,
            float radius) {
        float right = left + width;
        float bottom = top + height;
        if (x < left || x >= right || y < top || y >= bottom) {
            return false;
        }
        float corner = Math.min(radius, Math.min(width, height) * 0.5f);
        float cx;
        float cy;
        if (x < left + corner && y < top + corner) {
            cx = left + corner;
            cy = top + corner;
        } else if (x >= right - corner && y < top + corner) {
            cx = right - corner;
            cy = top + corner;
        } else if (x < left + corner && y >= bottom - corner) {
            cx = left + corner;
            cy = bottom - corner;
        } else if (x >= right - corner && y >= bottom - corner) {
            cx = right - corner;
            cy = bottom - corner;
        } else {
            return true;
        }
        float dx = x - cx;
        float dy = y - cy;
        return dx * dx + dy * dy <= corner * corner;
    }
}
