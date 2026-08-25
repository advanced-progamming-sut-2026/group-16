package io.github.finalwave.view.gui.hud;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.game.MatchResult;

import java.util.List;


public final class NpcDialogScript {
    private NpcDialogScript() {
    }

    public static List<NpcDialogLine> forLevel(ChapterConfig chapter, LevelConfig level) {
        if (chapter == null || level == null || level.getType() == LevelType.BOSS) {
            return List.of();
        }
        ChapterId chapterId = chapter.getId();
        int index = level.getIndex();
        LevelType type = level.getType();
        if (chapterId == ChapterId.ANCIENT_EGYPT && index == 1) {
            return List.of(
                    dave("I'm sure my taco is around here somewhere, but in the meantime..."),
                    penny("The lawn is clear, neighbor. Those plants are ready."),
                    dave("These plants were made for plantin'."));
        }
        if (type == LevelType.CONVEYOR_BELT) {
            return List.of(dave("No picking this time. Plant whatever rolls down the belt!"));
        }
        if (type == LevelType.LOCKED_PLANTS) {
            return List.of(dave("Some plants are locked. Work with what you've got!"));
        }
        if (type == LevelType.SAVE_OUR_SEEDS) {
            return List.of(dave("Those glowing plants are VIPs. Don't let the zombies munch 'em!"));
        }
        if (type == LevelType.TIMED_WAR) {
            if ("timed-sun".equals(level.getSpecialHandlerKey())) {
                return List.of(dave("Make sun. Lots of sun. The clock is already ticking!"));
            }
            return List.of(dave("Smash those zombies before the timer runs out!"));
        }
        if (type == LevelType.NIGHT_OPS) {
            return List.of(dave("No sun from the sky tonight. Grow your own!"));
        }
        if (type == LevelType.DEAD_LINE) {
            return List.of(dave("Don't let a single zombie cross that flower line!"));
        }
        if (type == LevelType.LOVE_YOUR_PLANTS) {
            return List.of(dave("Protect your plants. Lose too many and it's game over!"));
        }
        if (type == LevelType.PLANT_WHAT_YOU_GET) {
            return List.of(
                    dave("Spend that starting sun now. No sunflowers. No sky sun."),
                    dave("When your lawn looks tasty, hit LET'S ROCK!"));
        }
        if (index == 1 && chapterId == ChapterId.FROSTBITE_CAVES) {
            return List.of(
                    dave("Brr! Keep your plants warm or the wind will freeze 'em solid!"),
                    penny("Stay close to the warm tiles, neighbor."));
        }
        if (index == 1 && chapterId == ChapterId.BIG_WAVE_BEACH) {
            return List.of(
                    dave("Watch the tide! Those zombies love a sneaky beach landing."),
                    penny("The water line will keep moving. Plan around it."));
        }
        if (index == 1 && chapterId == ChapterId.DARK_AGES) {
            return List.of(
                    dave("It's dark out here. Make your own sun, neighbor!"),
                    penny("Graves can hide surprises. Keep an eye on them."));
        }
        return List.of();
    }

    public static List<NpcDialogLine> forBossResult(MatchResult result) {
        if (result == MatchResult.WON) {
            return List.of(
                    dave("That tin can is toast! Extra crispy!"),
                    penny("Zomboss is down. Collect yourself, then we move on."),
                    dave("Did I mention I still want my taco?"));
        }
        if (result == MatchResult.LOST) {
            return List.of(
                    dave("Okay, that robot is rude."),
                    penny("We can try again. The lawn will be waiting."));
        }
        return List.of();
    }

    private static NpcDialogLine dave(String text) {
        return new NpcDialogLine(NpcSpeaker.DAVE, text);
    }

    private static NpcDialogLine penny(String text) {
        return new NpcDialogLine(NpcSpeaker.PENNY, text);
    }
}
