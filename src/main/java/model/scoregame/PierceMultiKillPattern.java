package model.scoregame;

import model.quest.event.GameEvent;

import java.util.HashMap;
import java.util.Map;

public final class PierceMultiKillPattern implements MeowPointPattern {
    public static final String ID = "pierce-multi-kill";
    private static final int POINTS_PER_EXTRA = 40;

    private final Map<String, Integer> killsByProjectile = new HashMap<>();
    private int score;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void onEvent(GameEvent event) {
        if (!(event instanceof GameEvent.ZombieKilled killed)) {
            return;
        }
        String projectileId = killed.projectileId();
        if (projectileId == null || projectileId.isBlank()) {
            return;
        }
        int count = killsByProjectile.merge(projectileId, 1, Integer::sum);
        if (count > 1) {
            score += POINTS_PER_EXTRA;
        }
    }

    @Override
    public int score() {
        return score;
    }

    @Override
    public void reset() {
        killsByProjectile.clear();
        score = 0;
    }
}
