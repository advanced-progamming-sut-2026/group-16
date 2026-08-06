package io.github.finalwave.model.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GamePlayMenuCommands implements Command {
    ADVANCE_TIME("^advance time\\s+-t\\s+(?<count>\\S+)\\s+ticks$"),
    COLLECT_SUN("^collect sun\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    SHOW_SUN_AMOUNT("^show sun amount$"),
    PLANT_PLANT("^plant plant\\s+-t\\s+(?<type>.+?)\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    PLUCK_PLANT("^pluck plant\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    FEED_PLANT("^feed plant\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    SHOW_MAP("^show map$"),
    SHOW_PLANTS_STATUS("^show plants status$"),
    SHOW_TILE_STATUS("^show tile status\\s+-l\\s+\\((?<x>[^,]+),\\s*(?<y>[^)]+)\\)$"),
    ZOMBIES_INFO("^zombies info$"),
    START_ZOMBIE_WAVES("^start zombie waves$"),
    CHEAT_ADD_SUNS("^cheat add\\s+-n\\s+(?<count>\\S+)\\s+suns$"),
    RELEASE_THE_NUKE("^release the nuke$"),
    CHEAT_REMOVE_COOLDOWN("^cheat remove-cooldown$"),
    CHEAT_ADD_PLANT_FOOD("^cheat add-plant-food$"),
    CHEAT_ADD_ZOMBIE("^cheat spawn-zombie\\s+-t\\s+(?<zombieType>.+?)\\s+-l\\s+(?<x>[^,]+),\\s*(?<y>\\S+)$");

    private final Pattern compiledPattern;

    GamePlayMenuCommands(String pattern) {
        this.compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Matcher getMatcher(String input) {
        Matcher matcher = this.compiledPattern.matcher(input);
        return matcher.matches() ? matcher : null;
    }
}