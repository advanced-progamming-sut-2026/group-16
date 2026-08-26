package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.MainMenuController;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.widget.PanelLabels;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.model.user.NewsManager;


public final class MainMenuScreen extends MenuScreen {
    private static final float LOGO_WIDTH = 820f;
    private static final float SIDE_ICON = 110f;
    private static final float PROFILE_WIDTH = 420f;
    private static final float PROFILE_HEIGHT = 70f;
    private static final float PLAY_WIDTH = 340f;
    private static final float PLAY_HEIGHT = 96f;

    private MainMenuController controller;
    private Label unreadBadge;
    private Label nicknameLabel;

    public MainMenuScreen(PvzGame game) {
        super(game);
    }

    public void bind(MainMenuController controller) {
        this.controller = controller;
        if (controller != null) {
            bindCurrency(controller.getActiveUser());
        }
    }

    public void updateHeader(String nickname, boolean hasUnreadNews) {
        if (nicknameLabel != null && nickname != null) {
            nicknameLabel.setText(nickname);
        }
        refreshUnreadBadge();
    }

    @Override
    protected void buildUi() {
        useDefaultBackground();
        contentLayer.clearChildren();
        modalLayer.clearChildren();

        Skin skin = assets.skin();
        contentLayer.top();

        if (controller != null) {
            bindCurrency(controller.getActiveUser());
        }

        TextButton logout = PvzButtons.textButton("Logout", skin, "brown", () -> {
            if (controller != null) {
                controller.logout();
            }
        });
        Table top = new Table();
        top.add(logout).width(150).height(48).left().padLeft(36).padTop(18);
        top.add().expandX();

        TextureRegion logoRegion = assets.region(MenuAssetIds.LOGO);
        Image logo = new Image(new TextureRegionDrawable(logoRegion));
        logo.setScaling(Scaling.fit);
        float logoHeight = LOGO_WIDTH * logoRegion.getRegionHeight()
                / (float) Math.max(1, logoRegion.getRegionWidth());

        Actor profileBar = buildProfileBar(skin);

        Actor cloud = PvzButtons.framedIconButton(skin, assets.region(MenuAssetIds.CLOUD_ICON), SIDE_ICON,
                () -> openDestination(MainMenuController.Destination.SCORE_GAME));
        Actor news = PvzButtons.framedIconButton(skin, assets.region(MenuAssetIds.NEWS_ICON), SIDE_ICON,
                () -> openDestination(MainMenuController.Destination.NEWS));
        Actor settings = PvzButtons.framedIconButton(skin, assets.region(MenuAssetIds.SETTINGS_ICON), SIDE_ICON,
                () -> openDestination(MainMenuController.Destination.SETTINGS));
        Actor leaderboard = PvzButtons.framedIconButton(skin, assets.region(MenuAssetIds.LEADERBOARD_ICON), SIDE_ICON,
                () -> openDestination(MainMenuController.Destination.LEADERBOARD));

        TextButton play = PvzButtons.textButton("PLAY", skin, "purple",
                () -> openDestination(MainMenuController.Destination.GAME));
        play.getLabel().setFontScale(1.45f);

        String badgeStyle = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        unreadBadge = new Label("0", skin, badgeStyle);
        unreadBadge.setAlignment(Align.center);
        unreadBadge.setColor(Color.RED);
        unreadBadge.setVisible(false);

        Stack newsStack = new Stack();
        newsStack.add(news);
        Table badgeTable = new Table();
        badgeTable.top().right();
        badgeTable.add(unreadBadge).padTop(-4).padRight(-4);
        newsStack.add(badgeTable);
        refreshUnreadBadge();

        Table leftCluster = new Table();
        leftCluster.add(cloud).size(SIDE_ICON).padRight(18);
        leftCluster.add(newsStack).size(SIDE_ICON);

        Table rightCluster = new Table();
        rightCluster.add(settings).size(SIDE_ICON).padRight(18);
        rightCluster.add(leaderboard).size(SIDE_ICON);

        Table centerCluster = new Table();
        centerCluster.add(profileBar).size(PROFILE_WIDTH, PROFILE_HEIGHT).padBottom(14).row();
        centerCluster.add(play).width(PLAY_WIDTH).height(PLAY_HEIGHT);

        Table sides = new Table();
        sides.add(leftCluster).expandX().left().padLeft(48).bottom();
        sides.add(rightCluster).expandX().right().padRight(48).bottom();

        Table centerHost = new Table();
        centerHost.add(centerCluster).center().bottom().padBottom(8);

        Stack bottom = new Stack();
        bottom.add(sides);
        bottom.add(centerHost);

        contentLayer.add(top).growX().row();
        contentLayer.add(logo).size(LOGO_WIDTH, logoHeight).padTop(28).row();
        contentLayer.add().expandY().row();
        contentLayer.add(bottom).growX().padBottom(36);
    }

    private Actor buildProfileBar(Skin skin) {
        Image nameBg = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.NAME_ENTRY)));
        nameBg.setScaling(Scaling.stretch);

        String nick = controller != null ? controller.getActiveUser().getNickname() : "";
        nicknameLabel = new Label(nick, skin, "medium");
        nicknameLabel.setFontScale(1.45f);
        nicknameLabel.setAlignment(Align.center);
        nicknameLabel.setColor(PanelLabels.panelText(skin));

        Image profileIcon = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.PROFILE_ICON)));
        profileIcon.setScaling(Scaling.fit);

        Table overlay = new Table();
        overlay.add(nicknameLabel).expandX().growX().padLeft(28).padRight(8);
        overlay.add(profileIcon).size(40, 40).padRight(16);

        Stack profileBar = new Stack();
        profileBar.add(nameBg);
        profileBar.add(overlay);
        PvzButtons.animate(profileBar, 1.05f, 0.95f, () ->
                openDestination(MainMenuController.Destination.PROFILE));
        return profileBar;
    }

    private void refreshUnreadBadge() {
        if (unreadBadge == null) {
            return;
        }
        int count = unreadCount();
        unreadBadge.setText(String.valueOf(count));
        unreadBadge.setVisible(count > 0);
    }

    private int unreadCount() {
        if (controller == null || controller.getActiveUser() == null) {
            return 0;
        }
        return new NewsManager().getUnread(controller.getActiveUser()).size();
    }

    private void openDestination(MainMenuController.Destination destination) {
        if (controller == null) {
            return;
        }
        if (!game.router().supportsDestination(destination)) {
            toastMessage("Coming soon");
            return;
        }
        controller.open(destination);
    }
}
