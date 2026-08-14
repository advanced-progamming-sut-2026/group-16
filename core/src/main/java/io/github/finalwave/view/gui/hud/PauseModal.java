package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PvzButtons;


public final class PauseModal {
    private ModalPanel panel;

    public void show(Table modalLayer, Viewport viewport, Skin skin, Runnable onResume, Runnable onRestart, Runnable onExit) {
        dismiss();
        panel = new ModalPanel(skin, "Paused");
        TextButton resume = PvzButtons.textButton("Resume", skin, "purple", () -> {
            dismiss();
            if (onResume != null) {
                onResume.run();
            }
        });
        TextButton restart = PvzButtons.textButton("Restart", skin, "brown", () -> {
            dismiss();
            if (onRestart != null) {
                onRestart.run();
            }
        });
        TextButton exit = PvzButtons.textButton("Save & Exit", skin, "brown", () -> {
            dismiss();
            if (onExit != null) {
                onExit.run();
            }
        });
        panel.content().add(resume).width(280f).height(56f).padBottom(10f).row();
        panel.content().add(restart).width(280f).height(56f).padBottom(10f).row();
        panel.content().add(exit).width(280f).height(56f);
        panel.show(modalLayer, viewport);
        panel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                event.stop();
            }
        });
    }

    public void dismiss() {
        if (panel != null) {
            panel.dismiss();
            panel = null;
        }
    }

    public boolean isShowing() {
        return panel != null && panel.getStage() != null;
    }
}
