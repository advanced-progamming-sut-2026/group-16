package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.finalwave.network.match.MatchReactions;
import io.github.finalwave.view.gui.widget.PvzButtons;

import java.util.function.BiConsumer;

public final class ReactionBar extends Table {

    public ReactionBar(Skin skin, BiConsumer<String, Integer> send) {
        defaults().pad(2f);
        top().right();
        String[] lines = MatchReactions.messages();
        for (int i = 0; i < lines.length; i++) {
            int index = i;
            TextButton button = PvzButtons.textButton(lines[i], skin, "green_small",
                    () -> send.accept(MatchReactions.TEXT, index));
            add(button).height(36f).padRight(4f);
        }
        row();
        String[] faces = MatchReactions.faces();
        for (int i = 0; i < faces.length; i++) {
            int index = i;
            TextButton button = PvzButtons.textButton(faces[i], skin, "green_small",
                    () -> send.accept(MatchReactions.EMOJI, index));
            add(button).height(36f).padRight(4f);
        }
        row();
        String[] stickers = MatchReactions.stickerLabels();
        for (int i = 0; i < stickers.length; i++) {
            int index = i;
            TextButton button = PvzButtons.textButton(stickers[i], skin, "green_small",
                    () -> send.accept(MatchReactions.STICKER, index));
            add(button).height(36f).padRight(4f);
        }
    }
}
