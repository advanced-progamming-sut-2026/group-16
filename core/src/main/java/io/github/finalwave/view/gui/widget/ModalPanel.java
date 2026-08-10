package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import pvz.skin.BorderedTable;


public final class ModalPanel extends BorderedTable {
    private final Table content;
    private Table blocker;

    public ModalPanel(Skin skin, String title) {
        content = new Table();
        content.defaults().pad(8);
        pad(40);
        if (title != null && !title.isBlank()) {
            Label titleLabel = PanelLabels.title(skin, title);
            titleLabel.setFontScale(0.7f);
            add(titleLabel).padBottom(20).row();
        }
        add(content).grow();
        pack();
    }

    public Table content() {
        return content;
    }

    public void show(Group modalLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        blocker = new Table();
        blocker.setSize(width, height);
        blocker.setTouchable(Touchable.enabled);
        blocker.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        modalLayer.addActor(blocker);

        pack();
        setPosition(
                Math.round((width - getWidth()) / 2f),
                Math.round((height - getHeight()) / 2f)
        );
        modalLayer.addActor(this);
    }

    public void addCloseButton(Skin skin) {
        TextButton close = new TextButton("X", skin, "brown");
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dismiss();
            }
        });
        addActor(close);
        close.setPosition(getWidth() - close.getWidth() - 12f, getHeight() - close.getHeight() - 12f);
    }

    public void dismiss() {
        if (blocker != null) {
            blocker.remove();
            blocker = null;
        }
        remove();
    }

    @Override
    public boolean remove() {
        if (blocker != null) {
            blocker.remove();
            blocker = null;
        }
        return super.remove();
    }
}
