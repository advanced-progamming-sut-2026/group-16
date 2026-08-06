package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.item.SunType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TimedWarHandlerTest {

    private PlantRegistry plantRegistry;
    private ZombieRegistry zombieRegistry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");
    }

    private GameSession newTimedWarSession(Random random) {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(3), plantRegistry, zombieRegistry, 1, random);
        GameSession session = mode.createSession();
        session.setWavesAutoStart(false);
        session.addSunBalance(1000);
        return session;
    }

    private void killOneZombie(GameSession session) {
        Zombie zombie = session.spawnZombieOfType("ZombieDefault", 0, 5.0);
        zombie.takeDirectDamage(zombie.getHealth() + 99999);
        session.handleZombieKilled(zombie);
    }

    @Test
    void onLevelStartActivatesTimedWar() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        TimedWarRules rules = TimedWarRulesFactory.create(chapter, chapter.getLevel(3));
        GameSession session = newTimedWarSession(new Random(1));
        TimedWarHandler handler = new TimedWarHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);

        assertTrue(session.isTimedWarActive());
        assertEquals(TimedWarMode.KILL, session.getTimedWarRules().getMode());
        assertEquals(0, session.getTimedWarProgress());
        assertEquals(rules.getDurationTicks(), session.getTimedWarRemainingTicks());
    }

    @Test
    void killModeWinsAfterMeetingKillGoal() {
        TimedWarRules rules = new TimedWarRules(TimedWarMode.KILL, 600, 3);
        GameSession session = newTimedWarSession(new Random(2));
        TimedWarHandler handler = new TimedWarHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        killOneZombie(session);
        killOneZombie(session);
        killOneZombie(session);
        assertEquals(3, session.getTimedWarProgress());
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());

        handler.onTick(session);
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void killModeLosesWhenTimeExpiresWithoutKills() {
        TimedWarRules rules = new TimedWarRules(TimedWarMode.KILL, 5, 3);
        GameSession session = newTimedWarSession(new Random(3));
        TimedWarHandler handler = new TimedWarHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        session.advanceTicks(5);
        assertEquals(MatchResult.LOST, session.getMatchResult());
    }

    @Test
    void sunModeWinsAfterProducedSunMeetsGoal() {
        TimedWarRules rules = new TimedWarRules(TimedWarMode.SUN, 600, 150);
        GameSession session = newTimedWarSession(new Random(4));
        TimedWarHandler handler = new TimedWarHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        session.spawnSunItem(new Sun(0, 0, 100, SunType.NORMAL, true));
        session.spawnSunItem(new Sun(1, 0, 50, SunType.NORMAL, true));
        assertEquals(150, session.getTimedWarProgress());

        handler.onTick(session);
        assertEquals(MatchResult.WON, session.getMatchResult());
    }

    @Test
    void cheatAddSunBalanceDoesNotCountTowardSunGoal() {
        TimedWarRules rules = new TimedWarRules(TimedWarMode.SUN, 600, 150);
        GameSession session = newTimedWarSession(new Random(5));
        TimedWarHandler handler = new TimedWarHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        session.addSunBalance(500);
        assertEquals(0, session.getTimedWarProgress());
        handler.onTick(session);
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
    }

    @Test
    void timedWarSuppressesDefaultWaveClearWin() {
        TimedWarRules rules = new TimedWarRules(TimedWarMode.KILL, 600, 99);
        GameSession session = newTimedWarSession(new Random(6));
        TimedWarHandler handler = new TimedWarHandler(rules);
        session.setActiveSpecialLevelHandler(handler);
        handler.onLevelStart(session);
        session.start();

        assertTrue(session.isTimedWarActive());
        session.advanceTicks(10);
        assertEquals(MatchResult.IN_PROGRESS, session.getMatchResult());
        assertFalse(session.isTimedWarGoalMet());
    }
}
