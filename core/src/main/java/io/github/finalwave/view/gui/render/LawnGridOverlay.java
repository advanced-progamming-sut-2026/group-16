package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;


public final class LawnGridOverlay implements Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer();

    public void draw(Viewport viewport, LawnLayout layout) {
        if (layout == null) {
            return;
        }
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.RED);
        float originX = layout.originX();
        float originY = layout.originY();
        float width = layout.cols() * layout.tileWidth();
        float height = layout.rows() * layout.tileHeight();
        for (int col = 0; col <= layout.cols(); col++) {
            float x = originX + col * layout.tileWidth();
            shapes.line(x, originY, x, originY + height);
        }
        for (int row = 0; row <= layout.rows(); row++) {
            float y = originY + row * layout.tileHeight();
            shapes.line(originX, y, originX + width, y);
        }
        shapes.setColor(Color.CYAN);
        float mowerX = layout.mowerCenterX() - layout.tileWidth() / 2f;
        for (int row = 0; row < layout.rows(); row++) {
            shapes.rect(mowerX, layout.worldYForRow(row), layout.tileWidth(), layout.tileHeight());
        }
        shapes.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
