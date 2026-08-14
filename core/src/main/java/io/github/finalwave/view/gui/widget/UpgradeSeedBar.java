package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;


public final class UpgradeSeedBar extends Group {
    private static final Color TRACK = Color.valueOf("1E1E1E");
    private static final Color FILL = Color.valueOf("46D12A");
    private static Texture pixel;

    private final Image track;
    private final Image fill;
    private final Label fraction;
    private float progress;

    public UpgradeSeedBar(Skin skin) {
        setTouchable(Touchable.disabled);
        track = new Image(pixelDrawable());
        track.setColor(TRACK);
        fill = new Image(pixelDrawable());
        fill.setColor(FILL);
        String style = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        fraction = new Label("", skin, style);
        fraction.setAlignment(Align.center);
        fraction.setColor(Color.WHITE);
        fraction.setFontScale(0.7f);
        addActor(track);
        addActor(fill);
        addActor(fraction);
    }

    public void bind(float progress, String text) {
        this.progress = MathUtils.clamp(progress, 0f, 1f);
        fraction.setText(text == null ? "" : text);
        layoutPieces();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        layoutPieces();
    }

    private void layoutPieces() {
        float width = getWidth();
        float height = getHeight();
        track.setBounds(0f, 0f, width, height);
        fill.setBounds(0f, 0f, width * progress, height);
        fraction.setBounds(0f, 0f, width, height);
    }

    private static TextureRegionDrawable pixelDrawable() {
        return new TextureRegionDrawable(new TextureRegion(pixel()));
    }

    private static Texture pixel() {
        if (pixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            pixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return pixel;
    }
}
