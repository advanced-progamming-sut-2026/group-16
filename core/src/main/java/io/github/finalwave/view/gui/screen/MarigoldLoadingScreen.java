package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.finalwave.PvzGame;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.widget.PamActor;


public final class MarigoldLoadingScreen extends ScreenAdapter {
    private static final String BACK_PATH = "768/INITIAL/EFFECTS/LOAD_ICON_BACK/LOAD_ICON_BACK.PAM";
    private static final String FRONT_PATH = "768/INITIAL/EFFECTS/LOAD_ICON_FRONT/LOAD_ICON_FRONT.PAM";
    private static final String CLIP = "animation";
    private static final float ICON_SIZE = 220f;
    private static final float FADE_IN_SECONDS = 0.3f;
    private static final float FADE_OUT_SECONDS = 0.5f;

    private final PvzGame game;
    private final GameAssets assets;
    private final float holdSeconds;
    private final Screen next;
    private final Runnable onFinished;

    private ExtendViewport viewport;
    private SpriteBatch batch;
    private Stage stage;
    private Table contentLayer;
    private Texture blackTexture;
    private boolean finished;

    public MarigoldLoadingScreen(PvzGame game, float holdSeconds, Screen next, Runnable onFinished) {
        this.game = game;
        this.assets = game.assets();
        this.holdSeconds = Math.max(0f, holdSeconds);
        this.next = next;
        this.onFinished = onFinished;
    }

    @Override
    public void show() {
        finished = false;
        viewport = new ExtendViewport(MenuScreen.WORLD_WIDTH, MenuScreen.WORLD_HEIGHT);
        batch = new SpriteBatch();
        stage = new Stage(viewport, batch);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackTexture = new Texture(pixmap);
        pixmap.dispose();

        Image background = new Image(new TextureRegionDrawable(blackTexture));
        background.setFillParent(true);
        stage.addActor(background);

        PamActor back = assets.pamActor();
        back.setClip(BACK_PATH, CLIP, 1f, true);
        PamActor front = assets.pamActor();
        front.setClip(FRONT_PATH, CLIP, 1f, true);

        Stack icon = new Stack();
        icon.add(back);
        icon.add(front);

        contentLayer = new Table();
        contentLayer.setFillParent(true);
        contentLayer.add(icon).size(ICON_SIZE).center();
        contentLayer.getColor().a = 0f;
        contentLayer.addAction(Actions.sequence(
                Actions.fadeIn(FADE_IN_SECONDS),
                Actions.delay(holdSeconds),
                Actions.fadeOut(FADE_OUT_SECONDS),
                Actions.run(this::finish)));
        stage.addActor(contentLayer);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    public void render(float delta) {
        if (finished) {
            return;
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        stage.act(delta);
        if (finished) {
            return;
        }
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (blackTexture != null) {
            blackTexture.dispose();
            blackTexture = null;
        }
    }

    private void finish() {
        if (finished) {
            return;
        }
        finished = true;
        Gdx.app.postRunnable(this::completeTransition);
    }

    private void completeTransition() {
        Screen destination = next;
        Runnable done = onFinished;
        dispose();
        if (destination != null) {
            destination.show();
            destination.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (done != null) {
            done.run();
        }
        game.installScreen(destination);
    }
}
