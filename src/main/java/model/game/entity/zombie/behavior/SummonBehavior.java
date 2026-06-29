package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

public final class SummonBehavior implements ZombieBehavior {

    private final String summonedAlias;
    private final double triggerHealthRatio;
    private final double spawnOffsetX;   // relative to summoner's x
    private final int spawnRowOffset;    // relative to summoner's row
    private boolean summoned = false;

    public SummonBehavior(String summonedAlias, double triggerHealthRatio, double spawnOffsetX, int spawnRowOffset) {
        this.summonedAlias = summonedAlias;
        this.triggerHealthRatio = triggerHealthRatio;
        this.spawnOffsetX = spawnOffsetX;
        this.spawnRowOffset = spawnRowOffset;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (summoned) return;
        if (zombie.getHealthRatio() > triggerHealthRatio) return;

        summoned = true;
        int row = Math.max(0, Math.min(context.getRowCount() - 1, zombie.getRow() + spawnRowOffset));
        double x = Math.max(0, zombie.getX() + spawnOffsetX);

        context.spawnZombieOfType(summonedAlias, row, x);
    }
}