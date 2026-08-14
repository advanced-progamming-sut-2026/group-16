package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


public record LawnLayout(
        float originX,
        float originY,
        float tileWidth,
        float tileHeight,
        int rows,
        int cols
) {
    public static final float PLANT_SCALE = 0.40f;
    public static final float GIANT_WALLNUT_SCALE = 0.58f;
    public static final float ZOMBIE_SCALE = 1.05f;
    public static final float ICE_BLOCK_SCALE = 0.42f;
    public static final float PLANT_ANCHOR_Y = 0.42f;
    public static final float ZOMBIE_ANCHOR_Y = 1.0f;
    public static final float MOWER_SCALE = 0.42f;
    public static final float MOWER_ANCHOR_Y = 0.42f;
    public static final float MOWER_MODEL_X = -0.5f;

    public float mowerCenterX() {
        return worldX(MOWER_MODEL_X);
    }

    public Vector2 cellCenter(int col, int row) {
        return new Vector2(
                originX + col * tileWidth + tileWidth / 2f,
                worldYForRow(row) + tileHeight / 2f);
    }

    public Vector2 cellOrigin(int col, int row) {
        return new Vector2(originX + col * tileWidth, worldYForRow(row));
    }

    public float worldX(double modelX) {
        return originX + (float) modelX * tileWidth;
    }

    public float worldYForRow(int row) {
        return originY + (rows - 1 - row) * tileHeight;
    }

    public int colAt(float worldX) {
        if (!inLawnX(worldX)) {
            return -1;
        }
        int col = (int) Math.floor((worldX - originX) / tileWidth);
        if (col < 0 || col >= cols) {
            return -1;
        }
        return col;
    }

    public int rowAt(float worldY) {
        if (!inLawnY(worldY)) {
            return -1;
        }
        int fromBottom = (int) Math.floor((worldY - originY) / tileHeight);
        int row = rows - 1 - fromBottom;
        if (row < 0 || row >= rows) {
            return -1;
        }
        return row;
    }

    public boolean inLawn(float worldX, float worldY) {
        return inLawnX(worldX) && inLawnY(worldY);
    }

    public Rectangle lawnBounds() {
        return new Rectangle(originX, originY, cols * tileWidth, rows * tileHeight);
    }

    private boolean inLawnX(float worldX) {
        return worldX >= originX && worldX < originX + cols * tileWidth;
    }

    private boolean inLawnY(float worldY) {
        return worldY >= originY && worldY < originY + rows * tileHeight;
    }
}
