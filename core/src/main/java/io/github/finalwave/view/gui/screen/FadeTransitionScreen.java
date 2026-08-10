package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.finalwave.PvzGame;


public final class FadeTransitionScreen extends ScreenAdapter {
    private static final float FADE_OUT_SECONDS = 0.22f;
    private static final float FADE_IN_SECONDS = 0.28f;

    private final PvzGame game;
    private final Screen next;
    private final Runnable onFinished;

    private SpriteBatch batch;
    private Texture snapshotTexture;
    private TextureRegion snapshotRegion;
    private Texture whitePixel;
    private float elapsed;
    private boolean showingNext;
    private boolean finished;

    public FadeTransitionScreen(PvzGame game, TextureRegion snapshot, Screen next, Runnable onFinished) {
        this.game = game;
        this.next = next;
        this.onFinished = onFinished;
        this.snapshotRegion = snapshot;
        if (snapshot != null) {
            this.snapshotTexture = snapshot.getTexture();
        }
    }


    public static TextureRegion captureFramebuffer() {
        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        TextureRegion region = new TextureRegion(texture);
        region.flip(false, true);
        return region;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        elapsed = 0f;
        showingNext = false;
        finished = false;
    }

    @Override
    public void render(float delta) {
        if (finished) {
            return;
        }
        elapsed += delta;

        if (!showingNext) {
            float t = Math.min(1f, elapsed / FADE_OUT_SECONDS);
            drawSnapshotWithBlack(t);
            if (t >= 1f) {
                showingNext = true;
                elapsed = 0f;
                disposeSnapshotOnly();
                if (next != null) {
                    next.show();
                    next.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
            }
            return;
        }

        if (next != null) {
            next.render(delta);
        } else {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        float t = Math.min(1f, elapsed / FADE_IN_SECONDS);
        drawBlack(1f - t);

        if (t >= 1f) {
            finish();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (showingNext && next != null) {
            next.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        disposeSnapshotOnly();
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }

    private void finish() {
        if (finished) {
            return;
        }
        finished = true;
        Screen destination = next;
        Runnable done = onFinished;
        dispose();
        if (done != null) {
            done.run();
        }

        game.installScreen(destination);
    }

    private void drawSnapshotWithBlack(float blackAlpha) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (batch == null) {
            return;
        }
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        if (snapshotRegion != null) {
            batch.setColor(Color.WHITE);
            batch.draw(snapshotRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.setColor(0f, 0f, 0f, clamp01(blackAlpha));
        batch.draw(whitePixel(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawBlack(float blackAlpha) {
        if (batch == null || blackAlpha <= 0.001f) {
            return;
        }
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        batch.setColor(0f, 0f, 0f, clamp01(blackAlpha));
        batch.draw(whitePixel(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private Texture whitePixel() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return whitePixel;
    }

    private void disposeSnapshotOnly() {
        if (snapshotTexture != null) {
            snapshotTexture.dispose();
            snapshotTexture = null;
            snapshotRegion = null;
        }
    }

    private static float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }
}
