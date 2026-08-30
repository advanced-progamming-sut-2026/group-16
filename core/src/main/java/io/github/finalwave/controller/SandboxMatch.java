package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.definition.plant.PlantDefinition;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;


public final class SandboxMatch {
    private SandboxMatch() {
    }

    public static GamePlayController create(User user, UserDatabase userDatabase) {
        PlantRegistry plants = App.getInstance().getPlantRegistry();
        ZombieRegistry zombies = PlantSelectionController.loadZombieRegistry();
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.ANCIENT_EGYPT);
        LevelConfig level = chapter.getLevel(1);
        AdventureMode mode = new AdventureMode(
                chapter, level, plants, zombies, user.getDifficultyLevel(), new Random());
        GameSession session = mode.createSession();
        session.enableSandboxPractice();
        session.setSunBalance(9990);
        session.setPlantFoodCount(GameSession.MAX_PLANT_FOOD);
        List<String> ordered = loadout(plants);
        session.setSelectedLoadout(Set.copyOf(ordered));
        session.setSelectedLoadoutOrder(ordered);
        return new GamePlayController(
                user, userDatabase, mode, session, chapter, level, Set.of(), false);
    }

    private static List<String> loadout(PlantRegistry plants) {
        List<String> selected = new ArrayList<>();
        for (PlantDefinition definition : plants.getAllDefinitions()) {
            selected.add(definition.getName());
        }
        return List.copyOf(selected);
    }
}
