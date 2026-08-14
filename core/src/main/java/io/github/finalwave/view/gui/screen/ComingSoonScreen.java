package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.finalwave.PvzGame;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import pvz.skin.BorderedTable;

public final class ComingSoonScreen extends MenuScreen {
    private Runnable onBack;
    private String title = "Coming soon";

    public ComingSoonScreen(PvzGame game) {
        super(game);
    }

    public void bind(Runnable onBack, String title) {
        this.onBack = onBack;
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();

        Skin skin = assets.skin();
        BorderedTable panel = new BorderedTable();
        panel.pad(48);
        panel.add(PanelLabels.title(skin, title)).padBottom(18).row();
        panel.add(PanelLabels.body(skin, "This part of adventure is not in the GUI yet.")).width(560).padBottom(28).row();
        TextButton back = PvzButtons.textButton("Back", skin, "green_small", () -> {
            if (onBack != null) {
                onBack.run();
            }
        });
        panel.add(back).width(180).height(56);
        contentLayer.add(panel);
    }
}
