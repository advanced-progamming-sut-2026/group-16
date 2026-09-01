package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.game.entity.zombie.Zombie;

public record DeadLineHandler(int deadLineColumn) implements SpecialLevelHandler {

    public static final int DEFAULT_DEAD_LINE_COLUMN = 3;

    public DeadLineHandler() {
        this(DEFAULT_DEAD_LINE_COLUMN);
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.DEAD_LINE;
    }

    @Override
    public void onLevelStart(GameSession session) {
        session.activateDeadLine(deadLineColumn);
    }

    @Override
    public void onTick(GameSession session) {
        if (!session.isDeadLineActive() || session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.countsAsEnemy()) {
                continue;
            }
            if (zombie.getX() <= deadLineColumn) {
                MatchListener listener = session.getMatchListener();
                if (listener != null) {
                    listener.onDeadLineBreached(deadLineColumn, zombie.getType());
                }
                session.loseMatch();
                return;
            }
        }
    }
}
