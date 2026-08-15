package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.view.gui.assets.ChapterLawnArt;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import pvz.libpvz.textures.ResourceIndex;


public final class ChapterBackground {
    private final TextureRegion left;
    private final TextureRegion main;
    private final TextureRegion right;
    private final ChapterLawnArt art;
    private float scale = 1f;
    private float mainX;
    private float worldHeight;

    public ChapterBackground(GameAssets assets, ChapterId chapterId) {
        this.main = assets.region(LawnAssetIds.chapterMain(chapterId));
        this.left = optionalRegion(assets, LawnAssetIds.chapterLeft(chapterId));
        this.right = optionalRegion(assets, LawnAssetIds.chapterRight(chapterId));
        this.art = ChapterLawnArt.of(chapterId);
    }

    public void layoutFor(float worldWidth, float worldHeight, int cols) {
        this.worldHeight = worldHeight;
        this.scale = main == null ? 1f : worldHeight / mainHeight();
        this.mainX = worldWidth / 2f - art.centerX(cols) * scale;
    }

    public LawnLayout lawnLayout(int rows, int cols) {
        return new LawnLayout(
                mainX + art.lawnX() * scale,
                art.lawnY() * scale,
                art.tileWidth() * scale,
                art.tileHeight() * scale,
                Math.max(1, rows),
                Math.max(1, cols));
    }

    public void draw(Batch batch, Viewport viewport) {
        if (main == null) {
            return;
        }
        float height = viewport == null ? worldHeight : viewport.getWorldHeight();
        float mainWidth = main.getRegionWidth() * scale;
        if (left != null) {
            float sideWidth = sideWidth(left, height);
            batch.draw(left, mainX - sideWidth, 0f, sideWidth, height);
        }
        batch.draw(main, mainX, 0f, mainWidth, mainHeight() * scale);
        if (right != null) {
            batch.draw(right, mainX + mainWidth, 0f, sideWidth(right, height), height);
        }
    }

    private float mainHeight() {
        return main == null ? 1f : Math.max(1f, main.getRegionHeight());
    }

    private static float sideWidth(TextureRegion side, float worldHeight) {
        return side.getRegionWidth() * (worldHeight / Math.max(1f, side.getRegionHeight()));
    }

    private static TextureRegion optionalRegion(GameAssets assets, String imageId) {
        ResourceIndex index = assets.resourceIndex();
        if (index == null || index.image(imageId) == null) {
            return null;
        }
        return assets.region(imageId);
    }
}
