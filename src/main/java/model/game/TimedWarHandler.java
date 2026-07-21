package model.game;

import model.adventure.LevelType;

public final class TimedWarHandler implements SpecialLevelHandler {

    private final TimedWarRules rules;
    private boolean goalNotified;
    private boolean timeUpNotified;

    public TimedWarHandler(TimedWarRules rules) {
        this.rules = rules == null
                ? new TimedWarRules(TimedWarMode.KILL, 0, 0)
                : rules;
    }

    public TimedWarHandler() {
        this(new TimedWarRules(TimedWarMode.KILL, 0, 0));
    }

    public TimedWarRules getRules() {
        return rules;
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.TIMED_WAR;
    }

    @Override
    public void onLevelStart(GameSession session) {
        session.activateTimedWar(rules);
    }

    @Override
    public void onTick(GameSession session) {
        if (!session.isTimedWarActive() || session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        session.advanceTimedWarTick();
        if (session.isTimedWarGoalMet()) {
            if (!goalNotified && session.getMatchListener() != null) {
                session.getMatchListener().onTimedWarGoalReached(
                        rules.getMode(), session.getTimedWarProgress());
                goalNotified = true;
            }
            session.winMatch();
            return;
        }
        if (session.getTimedWarRemainingTicks() <= 0) {
            if (!timeUpNotified && session.getMatchListener() != null) {
                session.getMatchListener().onTimedWarTimeUp();
                timeUpNotified = true;
            }
            session.loseMatch();
        }
    }
}
