package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.finalwave.PvzGame;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.widget.CurrencyBar;
import io.github.finalwave.view.gui.widget.ToastArea;
import io.github.finalwave.model.App;
import io.github.finalwave.model.user.User;


public abstract class MenuScreen extends ScreenAdapter {
    public static final float WORLD_WIDTH = 1920f;
    public static final float WORLD_HEIGHT = 1080f;

    protected final PvzGame game;
    protected final GameAssets assets;
    protected final Viewport viewport;
    protected final Stage stage;
    protected final Table contentLayer;
    protected final Table modalLayer;
    protected final Table toastLayer;
    protected final Table hudLayer;
    protected final ToastArea toastArea;
    protected final CurrencyBar currencyBar;

    private final Image backgroundImage;

    protected MenuScreen(PvzGame game) {
        this.game = game;
        this.assets = game.assets();
        this.viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);

        this.stage = new Stage(viewport);

        backgroundImage = new Image();
        backgroundImage.setFillParent(true);
        backgroundImage.setScaling(Scaling.fill);

        contentLayer = new Table();
        contentLayer.setFillParent(true);
        modalLayer = new Table();
        modalLayer.setFillParent(true);
        toastLayer = new Table();
        toastLayer.setFillParent(true);
        hudLayer = new Table();
        hudLayer.setFillParent(true);
        hudLayer.top().right().padTop(20).padRight(24);
        hudLayer.setTouchable(Touchable.childrenOnly);
        modalLayer.setTouchable(Touchable.childrenOnly);
        toastLayer.setTouchable(Touchable.childrenOnly);

        Stack root = new Stack();
        root.setFillParent(true);
        root.add(backgroundImage);
        root.add(contentLayer);
        root.add(modalLayer);
        root.add(toastLayer);
        root.add(hudLayer);
        stage.addActor(root);

        toastArea = new ToastArea(assets.skin());
        toastLayer.addActor(toastArea);

        currencyBar = new CurrencyBar(assets, assets.skin());
        if (showsCurrencyBar()) {
            hudLayer.add(currencyBar);
        }
    }

    protected boolean showsCurrencyBar() {
        return true;
    }

    protected void setBackground(TextureRegion background) {
        if (background == null) {
            backgroundImage.setDrawable(null);
            return;
        }
        backgroundImage.setDrawable(new TextureRegionDrawable(background));
    }

    protected void useDefaultBackground() {
        setBackground(assets.region(MenuAssetIds.BACKGROUND));
    }

    public void toastMessage(String message) {
        toastArea.showMessage(message);
    }

    public void toastError(String message) {
        toastArea.showError(message);
    }

    public void bindCurrency(User user) {
        currencyBar.bind(user);
    }

    public Table contentLayer() {
        return contentLayer;
    }

    public Table modalLayer() {
        return modalLayer;
    }

    public Viewport viewport() {
        return viewport;
    }

    public Stage stage() {
        return stage;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (showsCurrencyBar()) {
            User current = App.getInstance().getCurrentUser();
            if (current != null) {
                bindCurrency(current);
            }
        }
        buildUi();
    }

    protected abstract void buildUi();

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
        modalLayer.clearChildren();
        contentLayer.clearChildren();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
