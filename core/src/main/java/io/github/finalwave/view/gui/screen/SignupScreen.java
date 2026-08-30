package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.RegistrationController;
import io.github.finalwave.view.gui.widget.FormField;
import io.github.finalwave.view.gui.widget.ModalPanel;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.ThemedCheckBox;
import io.github.finalwave.view.gui.widget.ThemedSelectBox;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.SecurityQuestion;
import pvz.skin.BorderedTable;


public final class SignupScreen extends MenuScreen {
    private RegistrationController controller;
    private FormField username;
    private FormField password;
    private FormField passwordConfirm;
    private FormField nickname;
    private FormField email;
    private CheckBox maleCheck;
    private CheckBox femaleCheck;
    private Label formError;
    private ModalPanel securityModal;

    public SignupScreen(PvzGame game) {
        super(game);
    }

    public void bind(RegistrationController controller) {
        this.controller = controller;
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        securityModal = null;

        Skin skin = assets.skin();
        username = new FormField(skin, "Username");
        password = new FormField(skin, "Password", true);
        passwordConfirm = new FormField(skin, "Repeat Password", true);
        nickname = new FormField(skin, "Nickname");
        email = new FormField(skin, "Email");

        maleCheck = ThemedCheckBox.create(skin, "Male");
        femaleCheck = ThemedCheckBox.create(skin, "Female");
        ButtonGroup<CheckBox> genderGroup = new ButtonGroup<>(maleCheck, femaleCheck);
        genderGroup.setMaxCheckCount(1);
        genderGroup.setMinCheckCount(1);
        maleCheck.setChecked(true);

        TextButton registerBtn = PvzButtons.textButton("Register", skin, "purple", this::submitRegister);
        TextButton loginBtn = PvzButtons.textButton("Already have an account? Login", skin, "green_small",
                () -> {
                    if (controller != null) {
                        controller.goToLogin();
                    }
                });

        BorderedTable panel = new BorderedTable();
        panel.pad(70);
        panel.defaults().width(420).padBottom(8);
        panel.add(PanelLabels.title(skin, "Sign Up")).padBottom(20).row();
        formError = new Label("", skin, "secondary");
        formError.setColor(1f, 0.35f, 0.35f, 1f);
        formError.setWrap(true);
        panel.add(formError).width(420).left().padBottom(8).row();
        addField(panel, username);
        addField(panel, password);
        addField(panel, passwordConfirm);
        addField(panel, nickname);
        addField(panel, email);
        panel.add(new Label("Gender", skin, "secondary")).left().row();
        Table genderRow = new Table();
        genderRow.defaults().left().padRight(28);
        genderRow.add(maleCheck).height(46);
        genderRow.add(femaleCheck).height(46);
        panel.add(genderRow).left().padBottom(4).row();
        panel.add(registerBtn).height(70).padTop(16).row();
        panel.add(loginBtn).height(50).padTop(8);

        contentLayer.add(panel);
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

    public void closeSecurityQuestionModal() {
        if (securityModal != null) {
            securityModal.dismiss();
            securityModal = null;
        }
    }

    private Gender selectedGender() {
        return femaleCheck != null && femaleCheck.isChecked() ? Gender.FEMALE : Gender.MALE;
    }

    public void openSecurityQuestionModal() {
        if (controller == null) {
            return;
        }
        if (securityModal != null) {
            securityModal.dismiss();
        }

        Skin skin = assets.skin();
        securityModal = new ModalPanel(skin, "Security Question");
        Table content = securityModal.content();
        content.defaults().width(830).padBottom(8);

        Label prompt = PanelLabels.body(skin,
                "Choose a security question, then enter and confirm your answer.");
        content.add(prompt).width(830).padBottom(16).row();

        SelectBox<SecurityQuestionOption> questionBox = ThemedSelectBox.create(skin);
        Array<SecurityQuestionOption> options = new Array<>();
        for (SecurityQuestion question : SecurityQuestion.values()) {
            options.add(new SecurityQuestionOption(question));
        }
        questionBox.setItems(options);
        questionBox.setSelectedIndex(0);
        questionBox.setMaxListCount(SecurityQuestion.values().length);

        content.add(new Label("Question", skin, "secondary")).left().row();
        content.add(questionBox).height(58).padBottom(12).row();

        FormField answer = new FormField(skin, "Security answer");
        FormField answerConfirm = new FormField(skin, "Confirm answer");
        content.add(answer.field()).height(55).row();
        content.add(answerConfirm.field()).height(55).row();

        TextButton submit = PvzButtons.textButton("Finish registration", skin, "purple", () -> {
            SecurityQuestionOption selected = questionBox.getSelected();
            if (selected == null) {
                toastError("Please select a security question.");
                return;
            }
            controller.pickSecurityQuestion(
                    String.valueOf(selected.question().getNumber()),
                    answer.text(),
                    answerConfirm.text()
            );
        });
        content.add(submit).height(65).padTop(12);

        securityModal.pack();
        securityModal.show(modalLayer, viewport);
    }


    private record SecurityQuestionOption(SecurityQuestion question) {
        @Override
        public String toString() {
            return question.getNumber() + ". " + question.getText();
        }
    }

    private void submitRegister() {
        if (controller == null) {
            return;
        }
        clearInlineError();
        controller.register(
                username.text(),
                password.field().getText(),
                passwordConfirm.field().getText(),
                nickname.text(),
                email.text(),
                selectedGender().name()
        );
    }

    private static void addField(Table panel, FormField field) {
        panel.add(field.field()).height(55).row();
    }
}
