package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.scoregame.MeowPointTracker;
import io.github.finalwave.model.scoregame.ScoreGameConfig;
import io.github.finalwave.model.scoregame.ScoreGameSessionFactory;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

import java.time.Clock;
import java.util.Set;

public class ScoreGamePlantSelectionController extends PlantSelectionController {
    private final ScoreGameController scoreGameController;
    private final Clock clock;

    public ScoreGamePlantSelectionController(User user,
                                             UserDatabase userDatabase,
                                             ScoreGameController scoreGameController) {
        this(user, userDatabase, scoreGameController, Clock.systemUTC());
    }

    ScoreGamePlantSelectionController(User user,
                                      UserDatabase userDatabase,
                                      ScoreGameController scoreGameController,
                                      Clock clock) {
        super(user, userDatabase,
                AdventureRegistry.getInstance().getChapter(ChapterId.ANCIENT_EGYPT),
                ScoreGameConfig.level());
        this.scoreGameController = scoreGameController;
        this.clock = clock;
    }

    @Override
    protected void handleStartGame() {
        if (selected.isEmpty()) {
            getViewApi().errorLoadoutEmpty();
            return;
        }
        var match = ScoreGameSessionFactory.create(plantRegistry, zombieRegistry, clock);
        GameSession session = match.session();
        session.setSelectedLoadout(Set.copyOf(selected));
        MeowPointTracker tracker = match.tracker();
        getViewApi().showGameStarted();
        ScoreGamePlayController gameplay = new ScoreGamePlayController(
                user, userDatabase, scoreGameController, match.mode(), session,
                match.chapter(), match.level(), boosted, tracker);
        navigator.replace(gameplay);
        session.start();
    }
}
