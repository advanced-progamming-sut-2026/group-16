package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import pvz.skin.BorderedTable;

public final class ChallengeInviteDialog extends Table {
    private final Skin skin;
    private final InviteResponder responder;
    private final Texture dimTexture;
    private String inviteId;
    private Label messageLabel;

    public interface InviteResponder {
        void respond(String inviteId, boolean accepted);
    }

    public ChallengeInviteDialog(Skin skin, InviteResponder responder) {
        this.skin = skin;
        this.responder = responder;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();
        dimTexture = new Texture(pixmap);
        pixmap.dispose();
        setFillParent(true);
        setTouchable(Touchable.enabled);
        center();
        rebuild("");
        setVisible(false);
    }

    public void show(String inviteId, String fromUsername) {
        this.inviteId = inviteId;
        rebuild(fromUsername);
        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
        inviteId = null;
    }

    private void rebuild(String fromUsername) {
        clearChildren();
        Image dimmer = new Image(new TextureRegionDrawable(new TextureRegion(dimTexture)));
        dimmer.setFillParent(true);
        dimmer.setColor(1f, 1f, 1f, 0.55f);
        dimmer.setTouchable(Touchable.enabled);
        addActor(dimmer);

        BorderedTable panel = new BorderedTable();
        panel.pad(32);
        messageLabel = PanelLabels.title(skin, fromUsername == null || fromUsername.isBlank()
                ? "Challenge"
                : "Challenge from " + fromUsername);
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
        add(panel).center();
    }
}
