package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.finalwave.PvzGame;
import io.github.finalwave.view.gui.assets.AdventureAssetIds;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import pvz.libpvz.textures.ResourceIndex;

import java.util.LinkedHashSet;
import java.util.Set;


public final class BootScreen extends ScreenAdapter {
    private static final String TAG = "BootScreen";

    private final PvzGame game;
    private final GameAssets assets;
    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final SpriteBatch batch;
    private final Stage stage;
    private final Image backgroundImage;
    private final ProgressBar progressBar;
    private final Label statusLabel;

    private final Set<String> atlasIds = new LinkedHashSet<>();
    private int completed;
    private boolean started;
    private boolean finished;

    public BootScreen(PvzGame game) {
        this.game = game;
        this.assets = game.assets();
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(MenuScreen.WORLD_WIDTH, MenuScreen.WORLD_HEIGHT, camera);
        this.batch = new SpriteBatch();
        this.stage = new Stage(viewport, batch);

        backgroundImage = new Image();
        backgroundImage.setFillParent(true);
        backgroundImage.setScaling(Scaling.fill);

        Label title = new Label("Loading...", assets.skin(), "big");
        title.setFontScale(0.8f);
        statusLabel = new Label("Preparing menu assets", assets.skin(), "medium");
        progressBar = new ProgressBar(0f, 1f, 0.01f, false, assets.skin(), "xp_green");
        progressBar.setAnimateDuration(0.15f);

        Table content = new Table();
        content.setFillParent(true);
        content.add(title).padBottom(30).row();
        content.add(progressBar).width(800).height(45).padBottom(16).row();
        content.add(statusLabel);

        Stack root = new Stack();
        root.setFillParent(true);
        root.add(backgroundImage);
        root.add(content);
        stage.addActor(root);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        applyRandomTitleBackdrop();
        collectAtlasIds();
        if (atlasIds.isEmpty()) {
            finishBoot();
            return;
        }
        for (String atlasId : atlasIds) {
            assets.textures().loadAsync(atlasId, () -> {
                completed++;
                updateProgress();
                if (completed >= atlasIds.size() && !finished) {
                    finishBoot();
                }
            });
        }
        updateProgress();
    }

    private void applyRandomTitleBackdrop() {
        String[] backdrops = MenuAssetIds.TITLE_BACKDROPS;
        String imageId = backdrops[MathUtils.random(backdrops.length - 1)];
        ResourceIndex.ImageEntry entry = assets.resourceIndex().image(imageId);
        if (entry == null) {
            Gdx.app.error(TAG, "Missing title backdrop id in RESOURCES.json: " + imageId);
            return;
        }
        assets.textures().loadSync(entry.atlasId);
        backgroundImage.setDrawable(new TextureRegionDrawable(assets.region(imageId)));
    }

    private void collectAtlasIds() {
        ResourceIndex index = assets.resourceIndex();
        for (String imageId : MenuAssetIds.ALL) {
            addAtlasId(index, imageId);
        }
        for (String imageId : AdventureAssetIds.ALL) {
            addAtlasId(index, imageId);
        }
        for (String imageId : LawnAssetIds.BOOT_PRELOAD) {
            addAtlasId(index, imageId);
        }
    }

    private void addAtlasId(ResourceIndex index, String imageId) {
        ResourceIndex.ImageEntry entry = index.image(imageId);
        if (entry == null) {
            Gdx.app.error(TAG, "Missing menu image id in RESOURCES.json: " + imageId);
            return;
        }
        atlasIds.add(entry.atlasId);
    }

    private void updateProgress() {
        float value = atlasIds.isEmpty() ? 1f : (float) completed / atlasIds.size();
        progressBar.setValue(value);
        statusLabel.setText("Loaded " + completed + " / " + atlasIds.size() + " atlases");
    }

    private void finishBoot() {
        if (finished) {
            return;
        }
        finished = true;
        progressBar.setValue(1f);
        statusLabel.setText("Starting...");
        if (!started) {
            started = true;
            game.startApplication();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
    }
}
