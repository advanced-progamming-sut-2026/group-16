package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.ScoreGameController;
import io.github.finalwave.model.scoregame.MeowPointBreakdown;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import pvz.skin.BorderedTable;

public final class ScoreGameScreen extends MenuScreen {
    private ScoreGameController controller;
    private Label bestLabel;
    private Label resultLabel;

    public ScoreGameScreen(PvzGame game) {
        super(game);
    }

    public void bind(ScoreGameController controller) {
        this.controller = controller;
        if (controller != null && controller.getUser() != null) {
            bindCurrency(controller.getUser());
        }
    }

    public void showResult(MeowPointBreakdown breakdown, Integer bestMeowPoint, boolean newBest) {
        if (bestLabel != null) {
            bestLabel.setText("Best MeowPoint: " + formatBest(bestMeowPoint));
        }
        if (resultLabel == null) {
            return;
        }
        if (breakdown == null) {
            resultLabel.setText("");
            return;
        }
        StringBuilder text = new StringBuilder("Last match: " + breakdown.total() + " MeowPoint");
        if (newBest) {
            text.append("  New best!");
        }
        resultLabel.setText(text.toString());
        toastMessage(text.toString());
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        if (controller != null && controller.getUser() != null) {
            bindCurrency(controller.getUser());
        }
        Skin skin = assets.skin();
        BorderedTable panel = new BorderedTable();
        panel.pad(48f);
        panel.add(PanelLabels.title(skin, "Score Game")).padBottom(18f).row();
        Integer best = controller == null ? null : controller.bestMeowPoint();
        bestLabel = PanelLabels.body(skin, "Best MeowPoint: " + formatBest(best));
        bestLabel.setAlignment(Align.center);
        panel.add(bestLabel).width(560f).padBottom(12f).row();
        resultLabel = PanelLabels.body(skin, "");
        resultLabel.setAlignment(Align.center);
        resultLabel.setWrap(true);
        panel.add(resultLabel).width(560f).padBottom(28f).row();
        TextButton start = PvzButtons.textButton("START", skin, "purple", () -> {
            if (controller != null) {
                controller.startMatch();
            }
        });
        panel.add(start).width(260f).height(64f).padBottom(12f).row();
        TextButton back = PvzButtons.textButton("Back", skin, "green_small", () -> {
            if (controller != null) {
                controller.back();
            }
        });
        panel.add(back).width(220f).height(52f);
        contentLayer.add(panel);
    }

    private static String formatBest(Integer bestMeowPoint) {
        return bestMeowPoint == null ? "-" : String.valueOf(bestMeowPoint);
    }
}
