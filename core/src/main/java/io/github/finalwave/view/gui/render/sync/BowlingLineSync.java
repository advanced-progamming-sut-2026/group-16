package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.view.gui.render.LawnLayout;


public final class BowlingLineSync {
    private static final float LINE_WIDTH = 6f;
    private static final Color LINE_COLOR = new Color(0.92f, 0.12f, 0.12f, 0.78f);

    private final LawnLayout layout;
    private final Group layer;
    private final Texture white;
    private final Image line;
    private boolean disposed;

    public BowlingLineSync(LawnLayout layout, Group layer) {
        this.layout = layout;
        this.layer = layer;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        white = new Texture(pixmap);
        pixmap.dispose();
        line = new Image(new TextureRegionDrawable(new TextureRegion(white)));
        line.setColor(LINE_COLOR);
        line.setTouchable(Touchable.disabled);
        line.setVisible(false);
        layer.addActor(line);
    }

    public void sync(GameSession session) {
        if (disposed || session == null || !session.isWalnutBowlingActive()) {
            if (line != null) {
                line.setVisible(false);
            }
            return;
        }
        Rectangle lawn = layout.lawnBounds();
        float x = layout.originX() + (session.getWalnutBowlingRedLineColumn() + 1) * layout.tileWidth();
        line.setBounds(x - LINE_WIDTH / 2f, lawn.y, LINE_WIDTH, lawn.height);
        line.setVisible(true);
    }

    public void clear() {
        if (line != null) {
            line.remove();
        }
        if (!disposed) {
            white.dispose();
            disposed = true;
        }
    }
}
