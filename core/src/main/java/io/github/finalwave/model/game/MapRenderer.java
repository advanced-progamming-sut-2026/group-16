package io.github.finalwave.model.game;

import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.util.AnsiColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MapRenderer {

    private MapRenderer() {
    }

    public static String render(GameSession session) {
        StringBuilder sb = new StringBuilder();
        WaveManager waves = session.getWaveManager();
        int wave = waves == null ? 0 : waves.getCurrentWaveNumber();
        sb.append("Wave: ").append(AnsiColors.color(AnsiColors.CYAN, String.valueOf(wave)))
                .append(" | Sun: ").append(AnsiColors.color(AnsiColors.YELLOW, String.valueOf(session.getSunBalance())))
                .append(" | PlantFood: ")
                .append(AnsiColors.color(AnsiColors.GREEN, String.valueOf(session.getPlantFoodCount())))
                .append(" | Result: ").append(coloredResult(session.getMatchResult()));
        appendModeHeader(sb, session);
        sb.append('\n');
        appendBoard(sb, session);
        return sb.toString();
    }

    private static void appendModeHeader(StringBuilder sb, GameSession session) {
        if (session.isDeadLineActive()) {
            sb.append(" | Dead line: column ").append(session.getDeadLineColumn());
        }
        if (session.isWalnutBowlingActive()) {
            sb.append(" | Red line: column ").append(session.getWalnutBowlingRedLineColumn());
        }
        if (session.isIZombieActive()) {
            sb.append(" | Placement line: column ").append(session.getIZombiePlacementColumn());
            sb.append(" | Brains: ").append(session.getIZombieBrainsEatenCount())
                    .append('/').append(session.getBoard().getRows());
        }
        if (session.isBeghouledActive()) {
            sb.append(" | Matches: ")
                    .append(session.getBeghouledBoard().getMatchesMade())
                    .append('/')
                    .append(session.getBeghouledMatchTarget());
        }
        if (session.isLoveYourPlantsActive()) {
            sb.append(" | Plants lost: ")
                    .append(session.getPlantsLost())
                    .append('/')
                    .append(session.getLoveYourPlantsMaxLoss());
        }
        if (session.isPlantWhatYouGetActive()) {
            if (session.isPrepPhaseActive()) {
                sb.append(" | Prep: plant freely, then: start zombie waves");
            } else {
                sb.append(" | Combat started");
            }
        }
    }

    private static void appendBoard(StringBuilder sb, GameSession session) {
        GameBoard board = session.getBoard();
        List<LawnMower> mowers = session.getLawnMowers();
        for (int row = 0; row < board.getRows(); row++) {
            sb.append("Row ").append(row + 1)
                    .append(rowMarker(session, mowers, row))
                    .append(": ");
            for (int col = 0; col < board.getCols(); col++) {
                sb.append('[').append(cellLabel(session, col, row)).append(']');
            }
            sb.append('\n');
        }
    }

    private static String rowMarker(GameSession session, List<LawnMower> mowers, int row) {
        if (session.isIZombieActive()) {
            return session.isIZombieBrainEaten(row)
                    ? AnsiColors.color(AnsiColors.RED, " [Eaten]")
                    : AnsiColors.color(AnsiColors.CYAN, " [Brain]");
        }
        boolean mowerReady = row < mowers.size() && !mowers.get(row).isUsed();
        return mowerReady
                ? AnsiColors.color(AnsiColors.GREEN, " [Mower]")
                : AnsiColors.color(AnsiColors.GRAY, " [----]");
    }

    public static String plantsStatus(GameSession session) {
        StringBuilder sb = new StringBuilder();
        for (String name : session.getSelectedLoadout()) {
            var def = session.getPlantRegistry().getDefinition(name);
            if (def == null) {
                continue;
            }
            int level = 1;
            var stats = io.github.finalwave.model.game.entity.plant.PlantStatsCalculator.compute(def, level);
            boolean ready = session.getCooldownTracker().isReady(name);
            int ticks = session.getCooldownTracker().ticksRemaining(name);
            double seconds = ticks / (double) GameSession.TICKS_PER_SECOND;
            sb.append(AnsiColors.color(AnsiColors.GREEN, name))
                    .append(" cost=").append(stats.cost())
                    .append(" ready=").append(ready
                            ? AnsiColors.color(AnsiColors.GREEN, "true")
                            : AnsiColors.color(AnsiColors.YELLOW, "false"));
            if (!ready) {
                sb.append(" cooldown=").append(String.format(Locale.US, "%.1fs", seconds));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String tileStatus(GameSession session, int col, int row) {
        GameBoard board = session.getBoard();
        if (!board.inBounds(col, row)) {
            return "Invalid tile.";
        }
        StringBuilder sb = new StringBuilder();
        Tile tile = board.getTile(col, row);
        sb.append("Tile (").append(col).append(',').append(row).append(") type=")
                .append(coloredTileLabel(tile));
        if (tile instanceof GraveTile grave) {
            sb.append(" HP=").append(grave.getHealth()).append('/').append(grave.getMaxHealth());
        } else if (tile instanceof IceTile ice) {
            sb.append(" HP=").append(ice.getHealth()).append('/').append(ice.getMaxHealth());
        }
        sb.append('\n');
        Plant ground = board.getGroundPlantAt(col, row);
        Plant overlay = board.getOverlayPlantAt(col, row);
        if (ground != null) {
            sb.append("Ground plant: ").append(AnsiColors.color(AnsiColors.GREEN, ground.getName()))
                    .append(" HP=").append(ground.getHealth()).append('/')
                    .append(ground.getMaxHealth()).append('\n');
        }
        if (overlay != null) {
            sb.append("Overlay plant: ").append(AnsiColors.color(AnsiColors.GREEN, overlay.getName()))
                    .append(" HP=").append(overlay.getHealth()).append('/')
                    .append(overlay.getMaxHealth()).append('\n');
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getRow() == row && (int) Math.floor(zombie.getX()) == col) {
                sb.append("Zombie: ").append(AnsiColors.color(AnsiColors.RED, zombie.getType()))
                        .append(" HP=").append(zombie.getHealth())
                        .append(" x=").append(String.format(Locale.US, "%.2f", zombie.getX()))
                        .append('\n');
            }
        }
        for (Sun sun : session.getSunItems()) {
            if (sun.getCol() == col && sun.getRow() == row) {
                sb.append("Sun: ").append(AnsiColors.color(AnsiColors.YELLOW, sun.getType().toString()))
                        .append(" value=").append(sun.getValue())
                        .append(sun.isFalling() ? " (falling)" : " (ground)")
                        .append('\n');
            }
        }
        return sb.toString();
    }

    private static String cellLabel(GameSession session, int col, int row) {
        GameBoard board = session.getBoard();
        Tile tile = board.getTile(col, row);
        List<String> parts = new ArrayList<>();
        if (session.isDeadLineActive() && col == session.getDeadLineColumn()) {
            parts.add(AnsiColors.color(AnsiColors.BRIGHT_YELLOW, "DL"));
        } else if (session.isWalnutBowlingActive() && col == session.getWalnutBowlingRedLineColumn()) {
            parts.add(AnsiColors.color(AnsiColors.BRIGHT_RED, "RL"));
        } else if (session.isIZombieActive() && col == session.getIZombiePlacementColumn()) {
            parts.add(AnsiColors.color(AnsiColors.CYAN, "PL"));
        } else parts.add(coloredTileLabel(tile));
        Plant ground = board.getGroundPlantAt(col, row);
        Plant overlay = board.getOverlayPlantAt(col, row);
        if (ground != null) parts.add(AnsiColors.color(AnsiColors.GREEN, shortName(ground.getName())));
        if (overlay != null) parts.add(AnsiColors.color(AnsiColors.GREEN, shortName(overlay.getName())));
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && zombie.getRow() == row
                    && (int) Math.floor(zombie.getX()) == col) {
                parts.add(AnsiColors.color(AnsiColors.RED, "Z:" + shortName(zombie.getType())));
            }
        }
        for (Sun sun : session.getSunItems()) {
            if (sun.getCol() == col && sun.getRow() == row) {
                parts.add(AnsiColors.color(AnsiColors.YELLOW, sun.isFalling() ? "Sun*" : "Sun"));
            }
        }
        if (session.getVaseAt(col, row) != null) {
            io.github.finalwave.model.game.entity.Vase vase = session.getVaseAt(col, row);
            String vaseLabel = switch (vase.getContent()) {
                case PLANT_SEED -> "Vp";
                case GARGANTUAR -> "Vg";
                case ZOMBIE -> "Vz";
                case EMPTY -> "V?";
            };
            parts.add(AnsiColors.color(AnsiColors.MAGENTA, vaseLabel));
        }
        if (session.getGroundSeedPacketAt(col, row) != null) parts.add(AnsiColors.color(AnsiColors.YELLOW, "Seed"));
        for (io.github.finalwave.model.minigame.bowling.BowlingNut nut : session.getBowlingNutSystem().getNuts()) {
            if ((int) Math.floor(nut.getX()) == col && (int) Math.round(nut.getRow()) == row) {
                String nutLabel = switch (nut.getType()) {
                    case STANDARD -> "Bn";
                    case EXPLOSIVE -> "Be";
                    case GIANT -> "Bg";
                };
                parts.add(AnsiColors.color(AnsiColors.BRIGHT_YELLOW, nutLabel));
            }
        }
        return String.join("|", parts);
    }

    private static String coloredTileLabel(Tile tile) {
        String label = tileLabel(tile);
        return switch (label) {
            case "X" -> AnsiColors.color(AnsiColors.GRAY, label);
            case "G", "Gs", "Gf" -> AnsiColors.color(AnsiColors.MAGENTA, label);
            case "I" -> AnsiColors.color(AnsiColors.CYAN, label);
            case "W" -> AnsiColors.color(AnsiColors.BLUE, label);
            case "S" -> AnsiColors.color(AnsiColors.CYAN, label);
            case "." -> AnsiColors.color(AnsiColors.GRAY, label);
            default -> label;
        };
    }

    private static String coloredResult(MatchResult result) {
        if (result == null) {
            return "";
        }
        return switch (result) {
            case WON -> AnsiColors.color(AnsiColors.GREEN, result.name());
            case LOST -> AnsiColors.color(AnsiColors.RED, result.name());
            case IN_PROGRESS -> AnsiColors.color(AnsiColors.CYAN, result.name());
        };
    }

    private static String tileLabel(Tile tile) {
        if (tile.isCrater()) {
            return "X";
        }
        if (tile instanceof io.github.finalwave.model.game.board.tile.GraveTile grave) {
            return switch (grave.getLoot()) {
                case SUN_50 -> "Gs";
                case PLANT_FOOD -> "Gf";
                case NONE -> "G";
            };
        }
        if (tile.isGrave()) {
            return "G";
        }
        if (tile.isIce()) {
            return "I";
        }
        if (tile.isWater()) {
            return "W";
        }
        if (tile instanceof io.github.finalwave.model.game.SlipperyTile) {
            return "S";
        }
        return ".";
    }

    private static String shortName(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String cleaned = name.replace("Zombie", "Z");
        if (cleaned.length() <= 6) {
            return cleaned;
        }
        return cleaned.substring(0, 6);
    }
}
