package model.game;

import model.game.board.GameBoard;
import model.game.board.tile.Tile;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.item.Sun;

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
        sb.append("Wave: ").append(wave)
                .append(" | Sun: ").append(session.getSunBalance())
                .append(" | PlantFood: ").append(session.getPlantFoodCount())
                .append(" | Result: ").append(session.getMatchResult());
        if (session.isDeadLineActive()) {
            sb.append(" | Dead line: column ").append(session.getDeadLineColumn());
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
        sb.append('\n');

        GameBoard board = session.getBoard();
        List<LawnMower> mowers = session.getLawnMowers();
        for (int row = 0; row < board.getRows(); row++) {
            boolean mowerReady = row < mowers.size() && !mowers.get(row).isUsed();
            sb.append("Row ").append(row + 1)
                    .append(mowerReady ? " [Mower]" : " [----]")
                    .append(": ");
            for (int col = 0; col < board.getCols(); col++) {
                sb.append('[').append(cellLabel(session, col, row)).append(']');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String plantsStatus(GameSession session) {
        StringBuilder sb = new StringBuilder();
        for (String name : session.getSelectedLoadout()) {
            var def = session.getPlantRegistry().getDefinition(name);
            if (def == null) {
                continue;
            }
            int level = 1;
            var stats = model.game.entity.plant.PlantStatsCalculator.compute(def, level);
            boolean ready = session.getCooldownTracker().isReady(name);
            int ticks = session.getCooldownTracker().ticksRemaining(name);
            double seconds = ticks / (double) GameSession.TICKS_PER_SECOND;
            sb.append(name)
                    .append(" cost=").append(stats.cost())
                    .append(" ready=").append(ready);
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
                .append(tileLabel(tile)).append('\n');
        Plant ground = board.getGroundPlantAt(col, row);
        Plant overlay = board.getOverlayPlantAt(col, row);
        if (ground != null) {
            sb.append("Ground plant: ").append(ground.getName())
                    .append(" HP=").append(ground.getHealth()).append('/')
                    .append(ground.getMaxHealth()).append('\n');
        }
        if (overlay != null) {
            sb.append("Overlay plant: ").append(overlay.getName())
                    .append(" HP=").append(overlay.getHealth()).append('/')
                    .append(overlay.getMaxHealth()).append('\n');
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getRow() == row && (int) Math.floor(zombie.getX()) == col) {
                sb.append("Zombie: ").append(zombie.getType())
                        .append(" HP=").append(zombie.getHealth())
                        .append(" x=").append(String.format(Locale.US, "%.2f", zombie.getX()))
                        .append('\n');
            }
        }
        for (Sun sun : session.getSunItems()) {
            if (sun.getCol() == col && sun.getRow() == row) {
                sb.append("Sun: ").append(sun.getType())
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
            parts.add("DL");
        } else {
            parts.add(tileLabel(tile));
        }
        Plant ground = board.getGroundPlantAt(col, row);
        Plant overlay = board.getOverlayPlantAt(col, row);
        if (ground != null) {
            parts.add(shortName(ground.getName()));
        }
        if (overlay != null) {
            parts.add(shortName(overlay.getName()));
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive() && zombie.getRow() == row
                    && (int) Math.floor(zombie.getX()) == col) {
                parts.add("Z:" + shortName(zombie.getType()));
            }
        }
        for (Sun sun : session.getSunItems()) {
            if (sun.getCol() == col && sun.getRow() == row) {
                parts.add(sun.isFalling() ? "Sun*" : "Sun");
            }
        }
        return String.join("|", parts);
    }

    private static String tileLabel(Tile tile) {
        if (tile instanceof model.game.board.tile.GraveTile grave) {
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
        if (tile instanceof model.game.SlipperyTile) {
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
