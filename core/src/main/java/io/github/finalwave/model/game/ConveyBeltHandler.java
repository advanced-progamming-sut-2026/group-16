package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.LevelType;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public final class ConveyBeltHandler implements SpecialLevelHandler {

    public static final int DROP_INTERVAL_SECONDS = 12;
    public static final int DROP_INTERVAL_TICKS = DROP_INTERVAL_SECONDS * GameSession.TICKS_PER_SECOND;

    private final List<String> availablePlants;
    private final Random random;
    private int ticksUntilNextDrop = DROP_INTERVAL_TICKS;

    public ConveyBeltHandler(Collection<String> availablePlants, Random random) {
        this.availablePlants = availablePlants == null ? List.of() : List.copyOf(availablePlants);
        this.random = random == null ? new Random() : random;
    }

    public ConveyBeltHandler() {
        this(List.of(), new Random());
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.CONVEYOR_BELT;
    }

    @Override
    public void onLevelStart(GameSession session) {
        if (session != null && session.getSkySunSystem() != null) {
            session.getSkySunSystem().setEnabled(false);
        }
        if (!availablePlants.isEmpty()) {
            session.activateConveyorBelt();
            dropPlant(session, false);
        }
    }

    @Override
    public void onTick(GameSession session) {
        if (availablePlants.isEmpty()) {
            return;
        }
        ticksUntilNextDrop--;
        if (ticksUntilNextDrop <= 0) {
            dropPlant(session, true);
        }
    }

    private void dropPlant(GameSession session, boolean notifyListener) {
        if (availablePlants.isEmpty()) {
            return;
        }
        String plant = availablePlants.get(random.nextInt(availablePlants.size()));
        session.addConveyorBeltPlant(plant);
        ticksUntilNextDrop = DROP_INTERVAL_TICKS;
        if (notifyListener && session.getMatchListener() != null) {
            session.getMatchListener().onConveyorBeltPlantArrived(plant);
        }
    }
}
