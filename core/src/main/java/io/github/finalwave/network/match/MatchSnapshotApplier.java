package io.github.finalwave.network.match;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MatchSnapshotApplier {

    private MatchSnapshotApplier() {
    }

    public static void apply(GameSession session, MatchStatePayload payload) {
        if (session == null || payload == null) {
            return;
        }
        session.syncNetworkTick(payload.getTick());
        session.setSunBalance(payload.getSunBalance());
        session.setIZombieSunBalance(payload.getZombieSunBalance());
        session.syncIZombieBrainsFromNetwork(payload.getBrainsEaten());
        syncPlants(session, payload.getPlants());
        syncZombies(session, payload.getZombies());
    }

    private static void syncPlants(GameSession session, List<SnapshotPlant> plants) {
        if (plants == null) {
            return;
        }
        Map<String, Plant> existing = new HashMap<>();
        for (Plant plant : session.getBoard().getAllPlants()) {
            existing.put(plant.getId(), plant);
        }
        Set<String> seen = new HashSet<>();
        for (SnapshotPlant dto : plants) {
            if (dto.getId() == null) {
                continue;
            }
            seen.add(dto.getId());
            Plant plant = existing.get(dto.getId());
            if (plant == null) {
                plant = findPlantAt(session, dto.getCol(), dto.getRow(), dto.getName());
            }
            if (plant == null) {
                plant = session.restorePlant(
                        dto.getName(), 1, dto.getCol(), dto.getRow(), dto.getHealth(), false);
            } else {
                plant.restoreHealth(dto.getHealth());
            }
        }
        for (Plant plant : List.copyOf(session.getBoard().getAllPlants())) {
            if (!seen.contains(plant.getId()) && !matchesAnySnapshot(plant, plants)) {
                session.removePlantFromBoard(plant, false);
            }
        }
    }

    private static Plant findPlantAt(GameSession session, int col, int row, String name) {
        for (Plant plant : session.getBoard().getAllPlants()) {
            if ((int) plant.getX() == col && (int) plant.getY() == row
                    && plant.getDefinition().getName().equals(name)) {
                return plant;
            }
        }
        return null;
    }

    private static boolean matchesAnySnapshot(Plant plant, List<SnapshotPlant> plants) {
        for (SnapshotPlant dto : plants) {
            if (dto.getId() != null && dto.getId().equals(plant.getId())) {
                return true;
            }
            if ((int) plant.getX() == dto.getCol() && (int) plant.getY() == dto.getRow()
                    && plant.getDefinition().getName().equals(dto.getName())) {
                return true;
            }
        }
        return false;
    }

    private static void syncZombies(GameSession session, List<SnapshotZombie> zombies) {
        if (zombies == null) {
            return;
        }
        Map<String, Zombie> existing = new HashMap<>();
        for (Zombie zombie : session.getZombies()) {
            existing.put(zombie.getId(), zombie);
        }
        Set<String> seen = new HashSet<>();
        for (SnapshotZombie dto : zombies) {
            if (dto.getId() == null) {
                continue;
            }
            seen.add(dto.getId());
            Zombie zombie = existing.get(dto.getId());
            if (zombie == null) {
                zombie = findZombieNear(session, dto.getRow(), dto.getX(), dto.getType());
            }
            if (zombie == null) {
                zombie = session.restoreZombie(dto.getType(), dto.getRow(), dto.getX(), dto.getHealth(), 0);
                if (zombie != null && dto.isStationary()) {
                    zombie.setStationary(true);
                    zombie.lockLane();
                }
            } else {
                zombie.setPosition(dto.getX(), dto.getRow());
                zombie.restoreHealth(dto.getHealth());
                if (dto.isStationary()) {
                    zombie.setStationary(true);
                    zombie.lockLane();
                }
            }
        }
        for (Zombie zombie : List.copyOf(session.getZombies())) {
            if (!seen.contains(zombie.getId()) && !matchesAnyZombieSnapshot(zombie, zombies)) {
                zombie.takeDamage(zombie.getHealth());
            }
        }
    }

    private static Zombie findZombieNear(GameSession session, int row, double x, String type) {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getRow() == row && zombie.getType().equals(type) && Math.abs(zombie.getX() - x) < 0.6) {
                return zombie;
            }
        }
        return null;
    }

    private static boolean matchesAnyZombieSnapshot(Zombie zombie, List<SnapshotZombie> zombies) {
        for (SnapshotZombie dto : zombies) {
            if (dto.getId() != null && dto.getId().equals(zombie.getId())) {
                return true;
            }
            if (zombie.getRow() == dto.getRow() && zombie.getType().equals(dto.getType())
                    && Math.abs(zombie.getX() - dto.getX()) < 0.6) {
                return true;
            }
        }
        return false;
    }
}
