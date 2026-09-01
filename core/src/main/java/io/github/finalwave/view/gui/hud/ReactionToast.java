package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.finalwave.network.match.MatchReactionPayload;
import io.github.finalwave.network.match.MatchReactions;
import pvz.skin.BorderedTable;

public final class ReactionToast extends BorderedTable {

    public ReactionToast(Skin skin, MatchReactionPayload payload) {
        pad(14f);
        String from = payload.getFromUsername() == null || payload.getFromUsername().isBlank()
                ? "Opponent"
                : payload.getFromUsername();
        Label who = new Label(from, skin, "secondary");
        Label body = new Label(MatchReactions.describe(payload.getKind(), payload.getIndex()), skin, "medium");
        body.setWrap(true);
        add(who).left().row();
        add(body).width(240f).left();
        addAction(Actions.sequence(
                Actions.delay(2.8f),
                Actions.fadeOut(0.35f),
                Actions.removeActor()));
    }

    public static void show(Table layer, Skin skin, MatchReactionPayload payload) {
        if (layer == null || skin == null || payload == null) {
            return;
        }
        ReactionToast toast = new ReactionToast(skin, payload);
        toast.setPosition(layer.getWidth() - 280f, layer.getHeight() - 160f);
        layer.addActor(toast);
    }
}
