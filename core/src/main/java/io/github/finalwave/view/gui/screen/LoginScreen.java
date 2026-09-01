package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.LoginController;
import io.github.finalwave.util.StayLoggedInStorage;
import io.github.finalwave.view.gui.widget.FormField;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import pvz.skin.BorderedTable;


public final class LoginScreen extends MenuScreen {
    private enum ForgotStep {
        IDENTITY,
        SECURITY,
        PASSWORD
    }

    private LoginController controller;
    private FormField username;
    private FormField password;
    private Label formError;
    private TextButton stayLoggedInBtn;
    private boolean stayLoggedIn;
    private ModalPanel forgotModal;
    private Table forgotContent;
    private ForgotStep forgotStep = ForgotStep.IDENTITY;

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
        forgotStep = ForgotStep.IDENTITY;

        Skin skin = assets.skin();
        username = new FormField(skin, "Username");
        password = new FormField(skin, "Password", true);
        String savedUsername = StayLoggedInStorage.loadUsername();
        if (savedUsername != null && !savedUsername.isBlank()) {
            username.field().setText(savedUsername);
        }
        stayLoggedIn = StayLoggedInStorage.loadSession() != null;
        stayLoggedInBtn = PvzButtons.textButton(
                stayLoggedIn ? "Stay logged in: On" : "Stay logged in: Off",
                skin,
                "brown",
                () -> {
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
        formError = new Label("", skin, "secondary");
        formError.setColor(1f, 0.35f, 0.35f, 1f);
        formError.setWrap(true);
        panel.add(formError).width(400).left().padBottom(8).row();
        panel.add(username.field()).height(60).row();
        panel.add(password.field()).height(60).row();
        panel.add(stayLoggedInBtn).height(50).padBottom(16).row();
        panel.add(loginBtn).height(70).row();
        panel.add(forgotBtn).height(50).row();
        panel.add(backBtn).height(50);

        contentLayer.add(panel);
    }

    public void showSecurityQuestionStep() {
        forgotStep = ForgotStep.SECURITY;
        if (forgotModal == null) {
            openForgotPasswordModal();
            return;
        }
        buildForgotStepSecurity();
    }

    public void showPasswordResetStep() {
        forgotStep = ForgotStep.PASSWORD;
        if (forgotModal == null) {
            openForgotPasswordModal();
            return;
        }
        buildForgotStepPassword();
    }

    public void closeForgotPasswordModal() {
        if (forgotModal != null) {
            forgotModal.dismiss();
            forgotModal = null;
        }
        forgotStep = ForgotStep.IDENTITY;
        if (controller != null) {
            controller.cancelPasswordReset();
        }
    }

    public void showInlineError(String message) {
        if (formError == null) {
            return;
        }
        formError.setText(message == null ? "" : message);
    }

    public void clearInlineError() {
        showInlineError("");
    }

    private void submitLogin() {
        if (controller == null) {
            return;
        }
        clearInlineError();
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
        switch (forgotStep) {
            case SECURITY -> buildForgotStepSecurity();
            case PASSWORD -> buildForgotStepPassword();
            default -> buildForgotStepIdentity();
        }
        forgotModal.pack();
        forgotModal.show(modalLayer, viewport);
    }

    private void buildForgotStepIdentity() {
        Skin skin = assets.skin();
        forgotContent.clearChildren();
        FormField userField = new FormField(skin, "Username");
        FormField emailField = new FormField(skin, "Email");

        if (username != null && !username.text().isBlank()) {
            userField.field().setText(username.text());
        }

        TextButton submit = PvzButtons.textButton("Continue", skin, "purple", () ->
                controller.beginPasswordReset(userField.text(), emailField.text()));
        TextButton cancel = PvzButtons.textButton("Cancel", skin, "green_small", this::closeForgotPasswordModal);

        forgotContent.add(PanelLabels.body(skin, "Verify your identity")).padBottom(12).row();
        forgotContent.add(userField.field()).height(55).row();
        forgotContent.add(emailField.field()).height(55).row();
        forgotContent.add(submit).height(60).padTop(10).row();
        forgotContent.add(cancel).height(45);
        forgotModal.pack();
    }

    private void buildForgotStepSecurity() {
        if (forgotModal == null) {
            return;
        }
        Skin skin = assets.skin();
        forgotContent.clearChildren();
        String question = controller.pendingSecurityQuestionText();
        Label questionLabel = PanelLabels.body(skin,
                question != null && !question.isBlank() ? question : "Security question unavailable.");
        questionLabel.setWrap(true);
        questionLabel.setAlignment(Align.center);

        FormField answerField = new FormField(skin, "Security answer");
        TextButton submit = PvzButtons.textButton("Verify", skin, "purple", () ->
                controller.verifySecurityAnswer(answerField.text()));
        TextButton cancel = PvzButtons.textButton("Cancel", skin, "green_small", this::closeForgotPasswordModal);

        forgotContent.add(PanelLabels.body(skin, "Answer your security question")).padBottom(12).row();
        forgotContent.add(questionLabel).width(420).padBottom(8).row();
        forgotContent.add(answerField.field()).height(55).row();
        forgotContent.add(submit).height(60).padTop(10).row();
        forgotContent.add(cancel).height(45);
        forgotModal.pack();
    }

    private void buildForgotStepPassword() {
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
