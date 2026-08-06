package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class SummonBehavior implements ZombieBehavior {

    private final String summonedAlias;
    private final double triggerHealthRatio;
    private final double spawnOffsetX;   // relative to summoner's x
    private final int spawnRowOffset;    // relative to summoner's row
    private final Integer fixedColumn;
    private boolean summoned = false;

    public SummonBehavior(String summonedAlias, double triggerHealthRatio, double spawnOffsetX, int spawnRowOffset) {
        this(summonedAlias, triggerHealthRatio, spawnOffsetX, spawnRowOffset, null);
    }

    private SummonBehavior(String summonedAlias, double triggerHealthRatio, double spawnOffsetX,
                           int spawnRowOffset, Integer fixedColumn) {
        this.summonedAlias = summonedAlias;
        this.triggerHealthRatio = triggerHealthRatio;
        this.spawnOffsetX = spawnOffsetX;
        this.spawnRowOffset = spawnRowOffset;
        this.fixedColumn = fixedColumn;
    }

    public static SummonBehavior atFixedColumn(String summonedAlias, double triggerHealthRatio,
                                                int zeroBasedColumn, int spawnRowOffset) {
        return new SummonBehavior(summonedAlias, triggerHealthRatio, 0,
                spawnRowOffset, zeroBasedColumn);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (summoned) return;
        if (zombie.getHealthRatio() > triggerHealthRatio) return;
        if (!zombie.tryBeginAbilityAction()) return;

        summoned = true;
        int row = Math.max(0, Math.min(context.getRowCount() - 1, zombie.getRow() + spawnRowOffset));
        double x = fixedColumn == null
                ? Math.max(0, Math.min(context.getColCount() - 1, zombie.getX() + spawnOffsetX))
                : Math.max(0, Math.min(context.getColCount() - 1, fixedColumn));

        context.spawnZombieOfType(summonedAlias, row, x);
    }
}