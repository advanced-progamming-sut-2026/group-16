package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.SettingController;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.ThemedCheckBox;
import io.github.finalwave.view.gui.widget.ThemedSlider;
import pvz.skin.BorderedTable;

import java.util.function.IntConsumer;

public final class SettingScreen extends MenuScreen {
    private SettingController controller;
    private Slider difficultySlider;
    private Label difficultyValue;
    private Slider speedSlider;
    private Label speedValue;
    private CheckBox gridCheck;
    private CheckBox debugCheck;
    private boolean applyingForm;

    public SettingScreen(PvzGame game) {
        super(game);
    }

    public void bind(SettingController controller) {
        this.controller = controller;
        if (controller != null) {
            bindCurrency(controller.getUser());
        }
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();

        Skin skin = assets.skin();
        if (controller != null) {
            bindCurrency(controller.getUser());
        }

        BorderedTable panel = new BorderedTable();
        panel.pad(48);
        panel.add(PanelLabels.title(skin, "Settings")).padBottom(24).row();

        difficultySlider = addDiscreteSlider(panel, skin, "Difficulty", 1, 5, value -> {
            if (controller != null) {
                controller.changeDifficulty(value);
            }
        });
        difficultyValue = (Label) difficultySlider.getUserObject();

        speedSlider = addDiscreteSlider(panel, skin, "Game speed", 1, 3, value -> {
            if (controller != null) {
                controller.setGameSpeed(value);
            }
        });
        speedValue = (Label) speedSlider.getUserObject();

        gridCheck = ThemedCheckBox.create(skin, "Show lawn grid");
        gridCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (applyingForm || controller == null) {
                    return;
                }
                controller.setShowLawnGrid(gridCheck.isChecked());
            }
        });
        panel.add(gridCheck).left().height(48).padBottom(14).row();

        debugCheck = ThemedCheckBox.create(skin, "Debug mode");
        debugCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (applyingForm || controller == null) {
                    return;
                }
                controller.setDebugMode(debugCheck.isChecked());
            }
        });
        panel.add(debugCheck).left().height(48).padBottom(28).row();

        TextButton backBtn = PvzButtons.textButton("Back", skin, "green_small", () -> {
            if (controller != null) {
                controller.back();
            }
        });
        panel.add(backBtn).width(180).height(56);

        contentLayer.add(panel);
        refreshForm();
    }

    public void refreshForm() {
        if (controller == null || difficultySlider == null) {
            return;
        }
        User user = controller.getUser();
        if (user == null) {
            return;
        }
        applyingForm = true;
        setSliderValue(difficultySlider, difficultyValue, user.getDifficultyLevel());
        setSliderValue(speedSlider, speedValue, user.getGameSpeed());
        if (gridCheck != null) {
            gridCheck.setChecked(user.isShowLawnGrid());
        }
        if (debugCheck != null) {
            debugCheck.setChecked(user.isDebugMode());
        }
        applyingForm = false;
    }

    private Slider addDiscreteSlider(Table panel, Skin skin, String title, int min, int max, IntConsumer onChange) {
        panel.add(sectionLabel(skin, title)).left().padBottom(8).row();

        Slider slider = ThemedSlider.create(skin, min, max);
        Label value = new Label(String.valueOf(min), skin, "medium");
        value.setColor(PanelLabels.panelText(skin));
        slider.setUserObject(value);

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int amount = Math.round(slider.getValue());
                value.setText(String.valueOf(amount));
                if (applyingForm || controller == null) {
                    return;
                }
                onChange.accept(amount);
            }
        });

        Table row = new Table();
        row.add(slider).width(420).height(48);
        row.add(value).width(48).padLeft(16);
        panel.add(row).left().padBottom(22).row();
        return slider;
    }

    private void setSliderValue(Slider slider, Label value, int amount) {
        if (slider != null) {
            slider.setValue(amount);
        }
        if (value != null) {
            value.setText(String.valueOf(amount));
        }
    }

    private static Label sectionLabel(Skin skin, String text) {
        Label label = new Label(text, skin, "secondary");
        label.setColor(PanelLabels.panelText(skin));
        return label;
    }
}
