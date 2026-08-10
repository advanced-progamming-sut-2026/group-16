package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.LoginController;
import io.github.finalwave.view.gui.widget.FormField;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import pvz.skin.BorderedTable;


public final class LoginScreen extends MenuScreen {
    private LoginController controller;
    private FormField username;
    private FormField password;
    private TextButton stayLoggedInBtn;
    private boolean stayLoggedIn;
    private ModalPanel forgotModal;
    private Table forgotContent;
    private boolean awaitingNewPassword;

    public LoginScreen(PvzGame game) {
        super(game);
    }

    public void bind(LoginController controller) {
        this.controller = controller;
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        forgotModal = null;
        awaitingNewPassword = false;

        Skin skin = assets.skin();
        username = new FormField(skin, "Username");
        password = new FormField(skin, "Password", true);
        stayLoggedIn = false;
        stayLoggedInBtn = PvzButtons.textButton("Stay logged in: Off", skin, "brown", () -> {
            stayLoggedIn = !stayLoggedIn;
            stayLoggedInBtn.setText(stayLoggedIn ? "Stay logged in: On" : "Stay logged in: Off");
        });

        TextButton loginBtn = PvzButtons.textButton("Login", skin, "purple", this::submitLogin);
        TextButton forgotBtn = PvzButtons.textButton("Forgot password", skin, "green_small",
                this::openForgotPasswordModal);
        TextButton backBtn = PvzButtons.textButton("Back", skin, "green_small", () -> {
            if (controller != null) {
                controller.back();
            }
        });

        BorderedTable panel = new BorderedTable();
        panel.pad(70);
        panel.defaults().width(400).padBottom(10);
        panel.add(PanelLabels.title(skin, "Login")).padBottom(24).row();
        panel.add(username.field()).height(60).row();
        panel.add(password.field()).height(60).row();
        panel.add(stayLoggedInBtn).height(50).padBottom(16).row();
        panel.add(loginBtn).height(70).row();
        panel.add(forgotBtn).height(50).row();
        panel.add(backBtn).height(50);

        contentLayer.add(panel);
    }

    public void showPasswordResetStep() {
        awaitingNewPassword = true;
        if (forgotModal == null) {
            openForgotPasswordModal();
        }
        buildForgotStepTwo();
    }

    public void closeForgotPasswordModal() {
        if (forgotModal != null) {
            forgotModal.dismiss();
            forgotModal = null;
        }
        awaitingNewPassword = false;
    }

    private void submitLogin() {
        if (controller == null) {
            return;
        }
        controller.login(username.text(), password.field().getText(), stayLoggedIn);
    }

    private void openForgotPasswordModal() {
        if (controller == null) {
            return;
        }
        if (forgotModal != null) {
            forgotModal.dismiss();
        }
        Skin skin = assets.skin();
        forgotModal = new ModalPanel(skin, "Forgot Password");
        forgotContent = forgotModal.content();
        forgotContent.defaults().width(420).padBottom(8);
        if (awaitingNewPassword) {
            buildForgotStepTwo();
        } else {
            buildForgotStepOne();
        }
        forgotModal.pack();
        forgotModal.show(modalLayer, viewport);
    }

    private void buildForgotStepOne() {
        Skin skin = assets.skin();
        forgotContent.clearChildren();
        FormField userField = new FormField(skin, "Username");
        FormField emailField = new FormField(skin, "Email");
        FormField answerField = new FormField(skin, "Security answer");

        TextButton submit = PvzButtons.textButton("Verify", skin, "purple", () ->
                controller.verifyIdentity(userField.text(), emailField.text(), answerField.text()));
        TextButton cancel = PvzButtons.textButton("Cancel", skin, "green_small", this::closeForgotPasswordModal);

        forgotContent.add(PanelLabels.body(skin, "Verify your identity")).padBottom(12).row();
        forgotContent.add(userField.field()).height(55).row();
        forgotContent.add(emailField.field()).height(55).row();
        forgotContent.add(answerField.field()).height(55).row();
        forgotContent.add(submit).height(60).padTop(10).row();
        forgotContent.add(cancel).height(45);
        forgotModal.pack();
    }

    private void buildForgotStepTwo() {
        if (forgotModal == null) {
            return;
        }
        Skin skin = assets.skin();
        forgotContent.clearChildren();
        FormField newPassword = new FormField(skin, "New password", true);
        FormField confirm = new FormField(skin, "Confirm password", true);

        TextButton submit = PvzButtons.textButton("Change password", skin, "purple", () ->
                controller.resetPassword(newPassword.field().getText(), confirm.field().getText()));
        TextButton cancel = PvzButtons.textButton("Cancel", skin, "green_small", this::closeForgotPasswordModal);

        forgotContent.add(PanelLabels.body(skin, "Choose a new password")).padBottom(12).row();
        forgotContent.add(newPassword.field()).height(55).row();
        forgotContent.add(confirm.field()).height(55).row();
        forgotContent.add(submit).height(60).padTop(10).row();
        forgotContent.add(cancel).height(45);
        forgotModal.pack();
    }
}
