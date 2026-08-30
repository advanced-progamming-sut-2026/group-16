package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.ability.IcebergFlashMark;

import java.util.List;


public final class IcebergFlashOverlay extends Image implements Disposable {
    private static final float PEAK_ALPHA = 0.42f;
    private static final float FADE_SECONDS = 0.55f;
    private static final Color FLASH = new Color(0.35f, 0.72f, 1f, PEAK_ALPHA);

    private final Texture white;
    private float elapsed;
    private boolean flashing;

    public IcebergFlashOverlay() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        white = new Texture(pixmap);
        pixmap.dispose();
        setDrawable(new TextureRegionDrawable(new TextureRegion(white)));
        setTouchable(Touchable.disabled);
        setVisible(false);
        getColor().set(FLASH);
    }

    public void sync(GameSession session) {
        if (session == null || session.getIcebergFlashSystem() == null) {
            return;
        }
        List<IcebergFlashMark> marks = session.getIcebergFlashSystem().drainFlashMarks();
        if (!marks.isEmpty()) {
            flashing = true;
            elapsed = 0f;
            getColor().a = PEAK_ALPHA;
            setVisible(true);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!flashing) {
            return;
        }
        elapsed += delta;
        float t = Math.min(1f, elapsed / FADE_SECONDS);
        getColor().a = PEAK_ALPHA * (1f - t);
        if (t >= 1f) {
            flashing = false;
            setVisible(false);
            getColor().a = 0f;
        }
    }

    public void fitStage(float width, float height) {
        setSize(width, height);
        setPosition(0f, 0f, Align.bottomLeft);
    }

    @Override
    public void dispose() {
        if (white != null) {
            white.dispose();
        }
    }
}
