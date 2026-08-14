package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.view.gui.widget.PanelLabels;
import pvz.skin.BorderedTable;


public final class NpcDialogBox extends BorderedTable {
    private final Label speaker;
    private final Label body;

    public NpcDialogBox(Skin skin) {
        pad(28f);
        speaker = PanelLabels.title(skin, "");
        speaker.setFontScale(0.48f);
        speaker.setAlignment(Align.left);
        body = PanelLabels.body(skin, "");
        body.setAlignment(Align.left);
        add(speaker).growX().padBottom(10f).row();
        add(body).width(640f);
        setVisible(false);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
    }

    public void show(String speakerName, String text) {
        speaker.setText(speakerName == null ? "" : speakerName);
        body.setText(text == null ? "" : text);
        pack();
        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
    }
}
