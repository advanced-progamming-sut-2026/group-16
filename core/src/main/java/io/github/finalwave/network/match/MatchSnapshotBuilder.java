package io.github.finalwave.network.match;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.List;

public final class MatchSnapshotBuilder {

    private MatchSnapshotBuilder() {
    }

    public static MatchStatePayload build(GameSession session, String matchId) {
        MatchStatePayload payload = new MatchStatePayload();
        payload.setMatchId(matchId);
        payload.setTick(session.getCurrentTick());
        payload.setElapsedSeconds(session.getCurrentTick() / (double) GameSession.TICKS_PER_SECOND);
        payload.setSunBalance(session.getSunBalance());
        payload.setZombieSunBalance(session.getIZombieSunBalance());
        int rows = session.getBoard().getRows();
        boolean[] brains = new boolean[rows];
        for (int row = 0; row < rows; row++) {
            brains[row] = session.isIZombieBrainEaten(row);
        }
        payload.setBrainsEaten(brains);
        payload.setPlants(session.getBoard().getAllPlants().stream().map(MatchSnapshotBuilder::toPlant).toList());
        payload.setZombies(session.getZombies().stream().filter(Zombie::isAlive).map(MatchSnapshotBuilder::toZombie).toList());
        payload.setProjectiles(List.of());
        return payload;
    }

    private static SnapshotPlant toPlant(Plant plant) {
        SnapshotPlant dto = new SnapshotPlant();
        dto.setId(plant.getId());
        dto.setName(plant.getDefinition().getName());
        dto.setX(plant.getX());
        dto.setY(plant.getY());
        dto.setCol((int) plant.getX());
        dto.setRow((int) plant.getY());
        dto.setHealth(plant.getHealth());
        dto.setMaxHealth(plant.getMaxHealth());
        return dto;
    }

    private static SnapshotZombie toZombie(Zombie zombie) {
        SnapshotZombie dto = new SnapshotZombie();
        dto.setId(zombie.getId());
        dto.setType(zombie.getType());
        dto.setX(zombie.getX());
        dto.setY(zombie.getY());
        dto.setRow(zombie.getRow());
        dto.setHealth(zombie.getHealth());
        dto.setMaxHealth(zombie.getMaxHealth());
        dto.setStationary(zombie.isStationary());
        return dto;
    }
}
