package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.finalwave.view.gui.widget.PanelLabels;
import pvz.skin.BorderedTable;

public final class ChallengeInviteDialog extends Table {
    private final Skin skin;
    private final InviteResponder responder;
    private String inviteId;
    private Label messageLabel;

    public interface InviteResponder {
        void respond(String inviteId, boolean accepted);
    }

    public ChallengeInviteDialog(Skin skin, InviteResponder responder) {
        this.skin = skin;
        this.responder = responder;
        setFillParent(true);
        rebuild("");
    }

    public void show(String inviteId, String fromUsername) {
        this.inviteId = inviteId;
        rebuild(fromUsername);
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
        inviteId = null;
    }

    private void rebuild(String fromUsername) {
        clearChildren();
        BorderedTable panel = new BorderedTable();
        panel.pad(32);
        messageLabel = PanelLabels.title(skin, "Challenge from " + fromUsername);
        panel.add(messageLabel).padBottom(20).row();
        TextButton accept = PvzButtons.textButton("Accept", skin, "green_small", () -> {
            if (responder != null && inviteId != null) {
                responder.respond(inviteId, true);
            }
            hide();
        });
        TextButton reject = PvzButtons.textButton("Reject", skin, "green_small", () -> {
            if (responder != null && inviteId != null) {
                responder.respond(inviteId, false);
            }
            hide();
        });
        Table row = new Table();
        row.add(accept).width(140).height(48).padRight(12);
        row.add(reject).width(140).height(48);
        panel.add(row);
        add(panel);
    }
}
