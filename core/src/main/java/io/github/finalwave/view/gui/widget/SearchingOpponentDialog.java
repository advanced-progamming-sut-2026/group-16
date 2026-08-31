package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import pvz.skin.BorderedTable;

public final class SearchingOpponentDialog extends Table {
    private static final String[] DOTS = {"", ".", "..", "..."};
    private static final float DOT_INTERVAL = 0.4f;

    private final Skin skin;
    private final Runnable onCancel;
    private final Texture dimTexture;
    private Label titleLabel;
    private Label elapsedLabel;
    private float elapsed;
    private int dotIndex;
    private float dotTimer;

    public SearchingOpponentDialog(Skin skin, Runnable onCancel) {
        this.skin = skin;
        this.onCancel = onCancel;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();
        dimTexture = new Texture(pixmap);
        pixmap.dispose();
        setFillParent(true);
        setTouchable(Touchable.enabled);
        center();
        rebuild();
        setVisible(false);
    }

    public void show() {
        if (isVisible()) {
            return;
        }
        elapsed = 0f;
        dotTimer = 0f;
        dotIndex = 0;
        updateLabels();
        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!isVisible()) {
            return;
        }
        elapsed += delta;
        dotTimer += delta;
        if (dotTimer >= DOT_INTERVAL) {
            dotTimer -= DOT_INTERVAL;
            dotIndex = (dotIndex + 1) % DOTS.length;
        }
        updateLabels();
    }

    private void updateLabels() {
        if (titleLabel != null) {
            titleLabel.setText("Searching for opponent" + DOTS[dotIndex]);
        }
        if (elapsedLabel != null) {
            int seconds = (int) elapsed;
            elapsedLabel.setText(String.format("Waiting %d:%02d", seconds / 60, seconds % 60));
        }
    }

    private void rebuild() {
        clearChildren();
        Image dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTexture)));
        dimmer.setFillParent(true);
        dimmer.setColor(1f, 1f, 1f, 0.55f);
        dimmer.setTouchable(Touchable.enabled);
        addActor(dimmer);

        BorderedTable panel = new BorderedTable();
        panel.pad(32);
        titleLabel = PanelLabels.title(skin, "Searching for opponent");
        panel.add(titleLabel).padBottom(12).row();

        Label hint = PanelLabels.body(skin, "You will be matched with the next available player.");
        hint.setAlignment(Align.center);
        panel.add(hint).width(420).padBottom(10).row();

        elapsedLabel = new Label("Waiting 0:00", skin, "secondary");
        panel.add(elapsedLabel).padBottom(20).row();

        TextButton cancel = PvzButtons.textButton("Cancel", skin, "green_small", () -> {
            if (onCancel != null) {
                onCancel.run();
            }
        });
        panel.add(cancel).width(180).height(52);
        add(panel).center();
    }

    public void dispose() {
        dimTexture.dispose();
    }
}
