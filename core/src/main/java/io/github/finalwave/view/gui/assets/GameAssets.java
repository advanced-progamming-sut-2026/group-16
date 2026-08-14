package io.github.finalwave.view.gui.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import io.github.finalwave.view.gui.widget.StoreChrome;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.ResourceIndex;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;


public final class GameAssets implements Disposable {
    private static final String TAG = "GameAssets";
    private static final String RESOLUTION = "768";

    private final FileHandle root;
    private final TextureBank textures;
    private final PamPlayer pamPlayer;
    private final Skin skin;
    private Texture missingTexture;
    private TextureRegion missingRegion;

    public GameAssets(FileHandle root) {
        this.root = root;
        this.textures = new TextureBank(RESOLUTION, root);
        this.pamPlayer = new PamPlayer(textures, root);
        this.skin = PvzSkin.get();
    }

    public FileHandle root() {
        return root;
    }

    public TextureBank textures() {
        return textures;
    }

    public PamPlayer pamPlayer() {
        return pamPlayer;
    }

    public Skin skin() {
        return skin;
    }

    public ResourceIndex resourceIndex() {
        return textures.getResourceIndex();
    }

    public boolean hasImage(String imageId) {
        return imageId != null && !imageId.isBlank() && resourceIndex().image(imageId) != null;
    }


    public TextureRegion region(String imageId) {
        try {
            TextureRegion region = textures.region(imageId);
            if (region != null) {
                return region;
            }
        } catch (RuntimeException e) {
            Gdx.app.error(TAG, "Failed to load image id: " + imageId, e);
        }
        Gdx.app.error(TAG, "Missing image id: " + imageId);
        return missingRegion();
    }

    public void update() {
        textures.update();
    }

    private TextureRegion missingRegion() {
        if (missingRegion == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 0f, 1f, 1f);
            pixmap.fill();
            missingTexture = new Texture(pixmap);
            pixmap.dispose();
            missingRegion = new TextureRegion(missingTexture);
        }
        return missingRegion;
    }

    @Override
    public void dispose() {
        textures.dispose();
        StoreChrome.dispose();
        if (missingTexture != null) {
            missingTexture.dispose();
            missingTexture = null;
            missingRegion = null;
        }
    }
}
