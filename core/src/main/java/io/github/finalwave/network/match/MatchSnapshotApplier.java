package io.github.finalwave.network.match;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;

import java.util.ArrayList;
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
        if (payload.getZombieRoster() != null && !payload.getZombieRoster().isEmpty()) {
            session.setIZombieRoster(payload.getZombieRoster(),
                    payload.getZombieCosts() == null ? Map.of() : payload.getZombieCosts());
        }
        if (payload.getPlantLoadout() != null && !payload.getPlantLoadout().isEmpty()) {
            session.setSelectedLoadout(new java.util.LinkedHashSet<>(payload.getPlantLoadout()));
        }
        syncPlants(session, payload.getPlants());
        syncZombies(session, payload.getZombies());
        syncProjectiles(session, payload.getProjectiles());
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
            } else {
                zombie.setPosition(dto.getX(), dto.getRow());
                zombie.restoreHealth(dto.getHealth());
            }
            if (zombie != null) {
                prepareNetworkZombie(zombie, dto.isStationary());
                applyZombieState(zombie, dto.getState());
                applyZombieStatuses(zombie, dto);
                applyZombieArmor(zombie, dto.getArmorLayers());
            }
        }
        for (Zombie zombie : List.copyOf(session.getZombies())) {
            if (!seen.contains(zombie.getId()) && !matchesAnyZombieSnapshot(zombie, zombies)) {
                zombie.takeDirectDamage(Math.max(zombie.getHealth(), 1) + zombieTotalArmor(zombie));
            }
        }
    }

    private static void prepareNetworkZombie(Zombie zombie, boolean stationary) {
        if (zombie.getState() == ZombieState.SPAWNING) {
            zombie.setState(ZombieState.MOVING);
        }
        if (stationary) {
            zombie.setStationary(true);
        }
        zombie.lockLane();
    }

    private static void applyZombieState(Zombie zombie, String stateName) {
        if (stateName == null || stateName.isBlank()) {
            if (zombie.getState() == ZombieState.SPAWNING) {
                zombie.setState(ZombieState.MOVING);
            }
            return;
        }
        try {
            ZombieState state = ZombieState.valueOf(stateName);
            if (state == ZombieState.SPAWNING) {
                state = ZombieState.MOVING;
            }
            zombie.setState(state);
        } catch (IllegalArgumentException ignored) {
            if (zombie.getState() == ZombieState.SPAWNING) {
                zombie.setState(ZombieState.MOVING);
            }
        }
    }

    private static void applyZombieStatuses(Zombie zombie, SnapshotZombie dto) {
        zombie.clearColdStatuses();
        if (dto.getFreezeTicks() > 0) {
            zombie.applyFreeze(dto.getFreezeTicks());
        }
        if (dto.getChillTicks() > 0) {
            zombie.applyChill(dto.getChillTicks());
        }
        if (dto.getPoisonTicks() > 0) {
            zombie.applyPoison(dto.getPoisonTicks(), 1);
        }
    }

    private static void applyZombieArmor(Zombie zombie, List<SnapshotArmorLayer> layers) {
        if (layers == null || layers.isEmpty()) {
            return;
        }
        List<Armor> live = zombie.getArmorLayers();
        Map<String, Integer> byAlias = new HashMap<>();
        for (SnapshotArmorLayer layer : layers) {
            if (layer.getAlias() != null) {
                byAlias.put(layer.getAlias(), layer.getHealth());
            }
        }
        for (int i = 0; i < live.size(); i++) {
            Armor armor = live.get(i);
            Integer health = byAlias.get(armor.getAlias());
            if (health == null && i < layers.size()) {
                health = layers.get(i).getHealth();
            }
            if (health != null) {
                armor.restoreHealth(health);
            }
        }
    }

    private static int zombieTotalArmor(Zombie zombie) {
        int total = 0;
        for (Armor armor : zombie.getArmorLayers()) {
            total += Math.max(0, armor.getHealth());
        }
        return total;
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

    private static void syncProjectiles(GameSession session, List<SnapshotProjectile> projectiles) {
        if (projectiles == null) {
            session.getProjectileSystem().replaceAll(List.of());
            return;
        }
        Map<String, Projectile> existing = new HashMap<>();
        for (Projectile projectile : session.getProjectileSystem().getProjectiles()) {
            existing.put(projectile.getId(), projectile);
        }
        List<Projectile> next = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (SnapshotProjectile dto : projectiles) {
            if (dto.getId() == null || dto.getId().isBlank()) {
                continue;
            }
            if (!seen.add(dto.getId())) {
                continue;
            }
            Projectile projectile = existing.get(dto.getId());
            if (projectile == null) {
                projectile = Projectile.forNetworkRestore(
                        dto.getId(),
                        dto.getRow(),
                        dto.getX(),
                        dto.getDamage(),
                        profileFrom(dto.getTrajectory()),
                        ProjectileEffect.fromString(dto.getType()),
                        dto.isFromZombie(),
                        dto.isReverse());
            } else {
                projectile.setX(dto.getX());
                projectile.setY(dto.getY());
                projectile.setReverse(dto.isReverse());
            }
            projectile.setVisualClip(dto.getVisualClip());
            if (dto.getVx() != 0 || dto.getVy() != 0) {
                projectile.setVelocity(dto.getVx(), dto.getVy());
            }
            bindProjectileSource(session, projectile, dto.getSourcePlantId());
            next.add(projectile);
        }
        session.getProjectileSystem().replaceAll(next);
    }

    private static void bindProjectileSource(GameSession session, Projectile projectile, String sourcePlantId) {
        if (sourcePlantId == null || sourcePlantId.isBlank() || projectile.getSource() != null) {
            return;
        }
        for (Plant plant : session.getBoard().getAllPlants()) {
            if (sourcePlantId.equals(plant.getId())) {
                projectile.setSource(plant);
                return;
            }
        }
    }

    private static ProjectileProfile profileFrom(String trajectory) {
        if (trajectory != null && trajectory.equalsIgnoreCase(ProjectileProfile.Trajectory.ARCING.name())) {
            return ProjectileProfile.arcing();
        }
        return ProjectileProfile.straight();
    }
}
