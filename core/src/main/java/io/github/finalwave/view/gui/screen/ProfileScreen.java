package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.ProfileController;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.widget.FormField;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import pvz.skin.BorderedTable;

public final class ProfileScreen extends MenuScreen {
    private static final float FIELD_WIDTH = 420f;
    private static final float SAVE_WIDTH = 140f;
    private static final float FIELD_HEIGHT = 52f;

    private ProfileController controller;
    private Label usernameValue;
    private Label nicknameValue;
    private Label emailValue;
    private Label gamesValue;
    private Label levelsValue;
    private Label meowValue;
    private FormField usernameField;
    private FormField nicknameField;
    private FormField emailField;
    private ModalPanel passwordModal;

    public ProfileScreen(PvzGame game) {
        super(game);
    }

    public void bind(ProfileController controller) {
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
        passwordModal = null;

        Skin skin = assets.skin();
        if (controller != null) {
            bindCurrency(controller.getUser());
        }

        BorderedTable panel = new BorderedTable();
        panel.pad(40);
        panel.add(PanelLabels.title(skin, "Profile")).padBottom(18).row();
        addInfoSection(panel, skin);
        addEditSection(panel, skin);
        addActionButtons(panel, skin);

        contentLayer.add(panel);
        refreshFromUser(controller == null ? null : controller.getUser());
    }

    public void refreshFromUser(User user) {
        if (user == null || usernameValue == null) {
            return;
        }
        bindCurrency(user);
        usernameValue.setText(user.getUsername());
        nicknameValue.setText(user.getNickname());
        emailValue.setText(user.getEmail());
        gamesValue.setText(String.valueOf(user.getGamesPlayed()));
        levelsValue.setText(String.valueOf(user.getChapterProgress().countCompletedLevels()));
        meowValue.setText(String.valueOf(user.getBestMeowPoint()));
        usernameField.field().setText(user.getUsername());
        nicknameField.field().setText(user.getNickname());
        emailField.field().setText(user.getEmail());
    }

    public void closePasswordModal() {
        if (passwordModal != null) {
            passwordModal.dismiss();
            passwordModal = null;
        }
    }

    private void addInfoSection(Table panel, Skin skin) {
        usernameValue = addInfoRow(panel, skin, "Username");
        nicknameValue = addInfoRow(panel, skin, "Nickname");
        emailValue = addInfoRow(panel, skin, "Email");
        gamesValue = addInfoRow(panel, skin, "Games played");
        levelsValue = addInfoRow(panel, skin, "Levels cleared");
        meowValue = addInfoRow(panel, skin, "Best MeowPoint");
    }

    private Label addInfoRow(Table panel, Skin skin, String title) {
        Label heading = new Label(title, skin, "secondary");
        heading.setColor(PanelLabels.panelText(skin));
        Label value = PanelLabels.body(skin, "");
        value.setWrap(false);
        Table row = new Table();
        row.add(heading).width(180).left();
        row.add(value).width(FIELD_WIDTH - 180).left();
        panel.add(row).left().padBottom(6).row();
        return value;
    }

    private void addEditSection(Table panel, Skin skin) {
        usernameField = new FormField(skin, "Username");
        nicknameField = new FormField(skin, "Nickname");
        emailField = new FormField(skin, "Email");
        addEditRow(panel, skin, usernameField, () -> {
            if (controller != null) {
                controller.changeUsername(usernameField.text());
            }
        });
        addEditRow(panel, skin, nicknameField, () -> {
            if (controller != null) {
                controller.changeNickname(nicknameField.text());
            }
        });
        addEditRow(panel, skin, emailField, () -> {
            if (controller != null) {
                controller.changeEmail(emailField.text());
            }
        });
    }

    private void addEditRow(Table panel, Skin skin, FormField field, Runnable onSave) {
        TextButton save = PvzButtons.textButton("Save", skin, "green_small", onSave);
        Table row = new Table();
        row.add(field.field()).width(FIELD_WIDTH - SAVE_WIDTH - 12).height(FIELD_HEIGHT);
        row.add(save).width(SAVE_WIDTH).height(FIELD_HEIGHT).padLeft(12);
        panel.add(row).left().padBottom(10).row();
    }

    private void addActionButtons(Table panel, Skin skin) {
        TextButton passwordBtn = PvzButtons.textButton("Change password", skin, "purple",
                this::openPasswordModal);
        TextButton backBtn = PvzButtons.textButton("Back", skin, "green_small", () -> {
            if (controller != null) {
                controller.back();
            }
        });
        panel.add(passwordBtn).width(FIELD_WIDTH).height(56).padTop(8).padBottom(12).row();
        panel.add(backBtn).width(180).height(56);
    }

    private void openPasswordModal() {
        if (controller == null) {
            return;
        }
        closePasswordModal();
        Skin skin = assets.skin();
        passwordModal = new ModalPanel(skin, "Change Password");
        Table content = passwordModal.content();
        content.defaults().width(FIELD_WIDTH).padBottom(8);

        FormField oldPassword = new FormField(skin, "Old Password", true);
        FormField newPassword = new FormField(skin, "New Password", true);
        FormField repeatPassword = new FormField(skin, "Repeat Password", true);

        TextButton confirm = PvzButtons.textButton("Confirm", skin, "purple", () ->
                submitPasswordChange(oldPassword, newPassword, repeatPassword));
        TextButton cancel = PvzButtons.textButton("Cancel", skin, "green_small", this::closePasswordModal);

        content.add(oldPassword.field()).height(55).row();
        content.add(newPassword.field()).height(55).row();
        content.add(repeatPassword.field()).height(55).row();
        content.add(confirm).height(60).padTop(10).row();
        content.add(cancel).height(60);

        passwordModal.pack();
        passwordModal.show(modalLayer, viewport);
    }

    private void submitPasswordChange(FormField oldPassword, FormField newPassword, FormField repeatPassword) {
        String next = newPassword.field().getText();
        String repeat = repeatPassword.field().getText();
        if (!next.equals(repeat)) {
            toastError("The given password and its confirmation do not match.");
            return;
        }
        if (controller != null) {
            controller.changePassword(next, oldPassword.field().getText());
        }
    }
}
