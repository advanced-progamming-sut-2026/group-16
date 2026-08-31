package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.IZombieMatchmakingController;
import io.github.finalwave.network.match.ListMatchUsersResponse;
import io.github.finalwave.network.match.MatchUserEntry;
import io.github.finalwave.view.gui.widget.ChallengeInviteDialog;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.SearchingOpponentDialog;
import pvz.skin.BorderedTable;

public final class IZombieMatchmakingScreen extends MenuScreen {
    private static final float POLL_INTERVAL_SECONDS = 2.5f;

    private IZombieMatchmakingController controller;
    private SearchingOpponentDialog searchingDialog;
    private ChallengeInviteDialog inviteDialog;
    private TextField usernameField;
    private Label sessionDebugLabel;
    private Table playerListTable;
    private boolean searching;
    private ListMatchUsersResponse lastDirectory;
    private String lastDirectorySignature;
    private float pollTimer;

    public IZombieMatchmakingScreen(PvzGame game) {
        super(game);
    }

    public void bind(IZombieMatchmakingController controller) {
        this.controller = controller;
    }

    @Override
    protected boolean showsCurrencyBar() {
        return false;
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();
        Skin skin = assets.skin();

        Table wrapper = new Table();
        wrapper.center();

        Table columns = new Table();
        columns.defaults().top();
        columns.add(buildLeftPanel(skin)).width(440).padRight(14);
        columns.add(buildRightPanel(skin)).width(440);
        wrapper.add(columns);
        contentLayer.add(wrapper).expand().fill();

        searchingDialog = new SearchingOpponentDialog(skin, () -> {
            if (controller != null) {
                controller.leaveQueue();
            }
        });
        modalLayer.addActor(searchingDialog);
        if (searching) {
            searchingDialog.show();
        }

        inviteDialog = new ChallengeInviteDialog(skin, (inviteId, accepted) -> {
            if (controller != null) {
                controller.respondInvite(inviteId, accepted);
            }
        });
        inviteDialog.setVisible(false);
        modalLayer.addActor(inviteDialog);

        if (lastDirectory != null) {
            applyDirectory(lastDirectory);
        }
    }

    private BorderedTable buildLeftPanel(Skin skin) {
        BorderedTable panel = new BorderedTable();
        panel.pad(40);
        panel.top().left();
        panel.add(PanelLabels.title(skin, "I, Zombie - Two Players")).left().padBottom(20).row();
        Label subtitle = new Label("Challenge a player or join random matchmaking.", skin, "secondary");
        subtitle.setWrap(true);
        panel.add(subtitle).left().width(340).padBottom(18).row();

        usernameField = new TextField("", skin);
        panel.add(new Label("Username", skin, "medium")).left().padBottom(6).row();
        panel.add(usernameField).width(320).height(48).padBottom(12).row();

        panel.add(actionButton("Challenge user", skin, () -> {
            if (controller != null && usernameField != null) {
                controller.challengeUser(usernameField.getText().trim());
            }
        })).width(220).height(52).padBottom(10).row();

        panel.add(actionButton("Random match", skin, () -> {
            if (controller != null) {
                controller.joinRandomQueue();
            }
        })).width(220).height(52).padBottom(10).row();

        panel.add(actionButton("Check status", skin, () -> {
            if (controller != null && usernameField != null) {
                controller.checkUserStatus(usernameField.getText().trim());
            }
        })).width(220).height(52).padBottom(18).row();

        panel.add(actionButton("Back", skin, () -> {
            if (controller != null) {
                controller.back();
            }
        })).width(180).height(56).left();
        return panel;
    }

    private BorderedTable buildRightPanel(Skin skin) {
        BorderedTable panel = new BorderedTable();
        panel.pad(24);
        panel.top().left();
        panel.add(PanelLabels.title(skin, "Online players")).left().padBottom(10).row();

        sessionDebugLabel = new Label("Loading...", skin, "secondary");
        sessionDebugLabel.setWrap(true);
        panel.add(sessionDebugLabel).width(320).left().padBottom(10).row();

        panel.add(actionButton("Refresh", skin, () -> {
            if (controller != null) {
                controller.pollDirectory();
            }
        })).width(130).height(44).left().padBottom(12).row();

        playerListTable = new Table(skin);
        playerListTable.top().left();
        ScrollPane scroll = new ScrollPane(playerListTable, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        panel.add(scroll).width(320).height(480).left();
        return panel;
    }

    private TextButton actionButton(String label, Skin skin, Runnable action) {
        return PvzButtons.textButton(label, skin, "green_small", action);
    }

    public void refresh() {
        buildUi();
    }

    public void updatePlayerDirectory(ListMatchUsersResponse response) {
        lastDirectory = response;
        String signature = directorySignature(response);
        boolean changed = !signature.equals(lastDirectorySignature);
        lastDirectorySignature = signature;
        if (playerListTable == null || sessionDebugLabel == null) {
            return;
        }
        if (!changed) {
            return;
        }
        applyDirectory(response);
    }

    private void applyDirectory(ListMatchUsersResponse response) {
        Skin skin = assets.skin();
        sessionDebugLabel.setText(formatSessionDebug(response));
        playerListTable.clearChildren();
        if (response == null || response.getUsers() == null || response.getUsers().isEmpty()) {
            playerListTable.add(new Label("No users found.", skin, "secondary")).left().row();
            return;
        }
        String selfUsername = response.getSelfUsername() == null ? "" : response.getSelfUsername().trim();
        for (MatchUserEntry entry : response.getUsers()) {
            if (entry == null || entry.getUsername() == null || entry.getUsername().isBlank()) {
                continue;
            }
            addPlayerRow(skin, entry, entry.getUsername().equals(selfUsername));
        }
    }

    private static String directorySignature(ListMatchUsersResponse response) {
        if (response == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder()
                .append(response.getSelfUsername())
                .append('|').append(response.isSelfOnline())
                .append('|').append(response.isSelfBusy());
        if (response.getUsers() != null) {
            for (MatchUserEntry entry : response.getUsers()) {
                if (entry == null) {
                    continue;
                }
                builder.append('|').append(entry.getUsername())
                        .append(',').append(entry.isOnline())
                        .append(',').append(entry.isBusy());
            }
        }
        return builder.toString();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        pollTimer += delta;
        if (pollTimer < POLL_INTERVAL_SECONDS) {
            return;
        }
        pollTimer = 0f;
        if (controller != null) {
            controller.pollDirectory();
        }
    }

    public void selectUsername(String username) {
        if (usernameField != null && username != null) {
            usernameField.setText(username);
        }
    }

    private void addPlayerRow(Skin skin, MatchUserEntry entry, boolean self) {
        Table row = new Table(skin);
        row.padBottom(5);

        Label marker = new Label(statusMarker(entry), skin, "medium");
        marker.setColor(statusColor(entry));
        row.add(marker).width(24).left();

        String suffix = self ? " (you)" : entry.isBusy() ? " (busy)" : "";
        Label nameLabel = new Label(entry.getUsername() + suffix, skin, "medium");
        nameLabel.setColor(entry.isOnline() ? Color.WHITE : Color.LIGHT_GRAY);
        row.add(nameLabel).expandX().left().padLeft(4);

        if (!self && entry.isOnline() && !entry.isBusy()) {
            row.add(actionButton("Challenge", skin, () -> {
                if (controller != null) {
                    controller.selectPlayer(entry.getUsername());
                    controller.challengeUser(entry.getUsername());
                }
            })).width(100).height(38).padLeft(6);
        } else if (!self) {
            row.add(actionButton("Pick", skin, () -> {
                if (controller != null) {
                    controller.selectPlayer(entry.getUsername());
                }
            })).width(70).height(38).padLeft(6);
        }

        playerListTable.add(row).width(320).left().row();
    }

    private static String statusMarker(MatchUserEntry entry) {
        if (entry.isOnline()) {
            return entry.isBusy() ? "◐" : "●";
        }
        return "○";
    }

    private static Color statusColor(MatchUserEntry entry) {
        if (entry.isOnline()) {
            return entry.isBusy() ? Color.ORANGE : Color.GREEN;
        }
        return Color.GRAY;
    }

    private static String formatSessionDebug(ListMatchUsersResponse response) {
        if (response == null) {
            return "Server: no response";
        }
        String self = response.getSelfUsername() == null || response.getSelfUsername().isBlank()
                ? "not bound"
                : response.getSelfUsername();
        String online = response.isSelfOnline() ? "online" : "offline";
        String busy = response.isSelfBusy() ? "busy" : "ready";
        return "You: " + self + " | " + online + " | " + busy;
    }

    public void showSearching(boolean searching) {
        this.searching = searching;
        if (searchingDialog == null) {
            return;
        }
        if (searching) {
            searchingDialog.show();
        } else {
            searchingDialog.hide();
        }
    }

    public void showInvite(String inviteId, String fromUsername) {
        if (inviteDialog != null) {
            inviteDialog.show(inviteId, fromUsername);
        }
    }

    public void hideInvite() {
        if (inviteDialog != null) {
            inviteDialog.hide();
        }
    }
}
