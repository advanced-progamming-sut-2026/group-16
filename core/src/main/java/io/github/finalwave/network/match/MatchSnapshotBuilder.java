package io.github.finalwave.network.match;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog;
import io.github.finalwave.model.minigame.izombie.NetworkedIZombieHandler;

import java.util.ArrayList;
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
        payload.setProjectiles(session.getProjectileSystem().getProjectiles().stream()
                .map(MatchSnapshotBuilder::toProjectile)
                .toList());
        if (session.getActiveMiniGameHandler() instanceof NetworkedIZombieHandler handler && handler.isPlaying()) {
            payload.setPhase(IZombieDuelCatalog.PHASE_PLAYING);
            payload.setSecondsLeft(handler.secondsLeft());
        } else {
            payload.setPhase(IZombieDuelCatalog.PHASE_PICKING);
            payload.setSecondsLeft(IZombieDuelCatalog.ROUND_SECONDS);
        }
        payload.setPlantLoadout(new ArrayList<>(session.getSelectedLoadout()));
        payload.setZombieRoster(new ArrayList<>(session.getIZombieZombiePool()));
        payload.setZombieCosts(session.getIZombieZombieCosts());
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
        dto.setState(zombie.getState() == null ? null : zombie.getState().name());
        dto.setFreezeTicks(zombie.getFreezeTicksRemaining());
        dto.setChillTicks(zombie.getChillTicksRemaining());
        dto.setPoisonTicks(zombie.getPoisonTicksRemaining());
        List<SnapshotArmorLayer> armor = new ArrayList<>();
        for (Armor layer : zombie.getArmorLayers()) {
            armor.add(new SnapshotArmorLayer(layer.getAlias(), layer.getHealth()));
        }
        dto.setArmorLayers(armor);
        return dto;
    }

    private static SnapshotProjectile toProjectile(Projectile projectile) {
        SnapshotProjectile dto = new SnapshotProjectile();
        dto.setId(projectile.getId());
        dto.setType(projectile.getEffect() == null ? "GENERIC" : projectile.getEffect().name());
        dto.setX(projectile.getX());
        dto.setY(projectile.getY());
        dto.setRow(projectile.getRow());
        dto.setFromZombie(projectile.isFromZombie());
        dto.setReverse(projectile.isReverse());
        ProjectileProfile.Trajectory trajectory = projectile.getProfile() == null
                ? ProjectileProfile.Trajectory.STRAIGHT
                : projectile.getProfile().trajectory();
        dto.setTrajectory(trajectory.name());
        dto.setVx(projectile.getVx());
        dto.setVy(projectile.getVy());
        dto.setDamage(projectile.getDamage());
        if (projectile.getSource() != null) {
            dto.setSourcePlantId(projectile.getSource().getId());
        }
        dto.setVisualClip(projectile.getVisualClip());
        return dto;
    }
}
