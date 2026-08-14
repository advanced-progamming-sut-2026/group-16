package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;


public final class LawnHighlights implements Disposable {
    private final Texture white;
    private final Image rowBar;
    private final Image colBar;

    public LawnHighlights(Group layer) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        white = new Texture(pixmap);
        pixmap.dispose();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(white));
        rowBar = bar(drawable);
        colBar = bar(drawable);
        layer.addActor(rowBar);
        layer.addActor(colBar);
        hide();
    }

    public void show(LawnLayout layout, int col, int row) {
        if (layout == null || col < 0 || row < 0) {
            hide();
            return;
        }
        Rectangle lawn = layout.lawnBounds();
        rowBar.setBounds(lawn.x, layout.worldYForRow(row), lawn.width, layout.tileHeight());
        colBar.setBounds(
                layout.originX() + col * layout.tileWidth(),
                lawn.y,
                layout.tileWidth(),
                lawn.height);
        rowBar.setVisible(true);
        colBar.setVisible(true);
    }

    public void hide() {
        rowBar.setVisible(false);
        colBar.setVisible(false);
    }

    @Override
    public void dispose() {
        white.dispose();
    }

    private static Image bar(TextureRegionDrawable drawable) {
        Image image = new Image(drawable) {
            private final Color previous = new Color();

            @Override
            public void draw(Batch batch, float parentAlpha) {
                previous.set(batch.getColor());
                super.draw(batch, parentAlpha);
                batch.setColor(previous);
            }
        };
        image.setColor(1f, 1f, 1f, 0.22f);
        image.setTouchable(Touchable.disabled);
        return image;
    }
}
