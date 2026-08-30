package io.github.finalwave.model.game;

import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.NormalTile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CraterSystem {

    private final List<TimedCrater> pending = new ArrayList<>();
    private final List<CraterFadeMark> fadeMarks = new ArrayList<>();

    public void schedule(int col, int row, int currentTick, int durationTicks) {
        if (durationTicks <= 0) {
            return;
        }
        pending.add(new TimedCrater(col, row, currentTick + durationTicks));
    }

    public void tick(GameBoard board, int currentTick) {
        if (pending.isEmpty() || board == null) {
            return;
        }
        Iterator<TimedCrater> iterator = pending.iterator();
        while (iterator.hasNext()) {
            TimedCrater crater = iterator.next();
            if (crater.expireTick() > currentTick) {
                continue;
            }
            if (board.inBounds(crater.col(), crater.row())
                    && board.getTile(crater.col(), crater.row()).isCrater()) {
                board.setTile(crater.col(), crater.row(), new NormalTile());
                fadeMarks.add(new CraterFadeMark(crater.row(), crater.col()));
            }
            iterator.remove();
        }
    }

    public List<CraterFadeMark> drainFadeMarks() {
        if (fadeMarks.isEmpty()) {
            return List.of();
        }
        List<CraterFadeMark> drained = List.copyOf(fadeMarks);
        fadeMarks.clear();
        return drained;
    }

    public void clear() {
        pending.clear();
        fadeMarks.clear();
    }

    private record TimedCrater(int col, int row, int expireTick) {
    }
}
