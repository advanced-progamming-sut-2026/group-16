package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;


public final class MatchResultModal {
    private ModalPanel panel;

    public void show(Table modalLayer,
                     Viewport viewport,
                     Skin skin,
                     MatchResult result,
                     Runnable onExit,
                     Runnable onRetry) {
        show(modalLayer, viewport, skin,
                result == MatchResult.WON ? "Victory" : "Defeat",
                result == MatchResult.WON
                        ? "You survived the attack."
                        : "The zombies ate your brains!",
                onExit, onRetry);
    }

    public void show(Table modalLayer,
                     Viewport viewport,
                     Skin skin,
                     String title,
                     String message,
                     Runnable onExit,
                     Runnable onRetry) {
        dismiss();
        panel = new ModalPanel(skin, title);
        Label body = PanelLabels.body(skin, message);
        body.setAlignment(Align.center);
        panel.content().add(body).width(420f).padBottom(18f).row();
        TextButton exit = PvzButtons.textButton("Exit", skin, "purple", () -> {
            dismiss();
            if (onExit != null) {
                onExit.run();
            }
        });
        panel.content().add(exit).width(260f).height(56f).padBottom(10f).row();
        if (onRetry != null) {
            TextButton retry = PvzButtons.textButton("Retry", skin, "brown", () -> {
                dismiss();
                onRetry.run();
            });
            panel.content().add(retry).width(260f).height(56f);
        }
        panel.show(modalLayer, viewport);
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
