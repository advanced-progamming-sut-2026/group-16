package model.game.entity.projectile;

import model.game.board.GameBoard;
import model.game.board.tile.NormalTile;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantCovering;
import model.game.entity.plant.PlantSpecialModifiers;
import model.game.entity.zombie.ArcadeObstacle;
import model.game.entity.zombie.Zombie;
import model.game.entity.GameContext;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public final class ProjectileSystem {

    private final List<Projectile> projectiles = new java.util.ArrayList<>();
    private final List<Projectile> pendingProjectiles = new java.util.ArrayList<>();
    private final Random random = new Random();
    private boolean ticking;

    public List<Projectile> getProjectiles() {
        return List.copyOf(projectiles);
    }

    public void spawn(Projectile projectile) {
        if (projectile != null) {
            if (ticking) {
                pendingProjectiles.add(projectile);
            } else {
                projectiles.add(projectile);
            }
        }
    }

    public void spawnReflected(Zombie reflector, Projectile original) {
        if (reflector == null || original == null) {
            return;
        }
        spawn(Projectile.reflected(reflector.getRow(), reflector.getX() - 0.1,
                original, reflector.getId()));
    }

    public void spawnFromPlant(Plant plant, int damage, int shots, ProjectileProfile profile) {
        spawnFromPlant(plant, damage, shots, profile, plant.projectileEffect());
    }

    public void spawnFromPlant(Plant plant, int damage, int shots,
                               ProjectileProfile profile, ProjectileEffect effect) {
        int additionalPierce = (int) plant.getStats()
                .specialModifier(PlantSpecialModifiers.ADDITIONAL_PIERCE);
        int pierce = profile.piercing() ? 1 + additionalPierce : additionalPierce;
        for (int i = 0; i < shots; i++) {
            ProjectileEffect resolvedEffect = resolveEffect(plant, effect);
            projectiles.add(new Projectile(
                    plant.getRow(),
                    plant.getCol() + 0.5,
                    damage,
                    profile,
                    resolvedEffect,
                    plant,
                    pierce));
        }
    }

    private ProjectileEffect resolveEffect(Plant plant, ProjectileEffect requestedEffect) {
        double butterChance = plant.getStats()
                .specialModifier(PlantSpecialModifiers.BUTTER_CHANCE_BUFF);
        if (butterChance > 0 && random.nextDouble() < butterChance) {
            return ProjectileEffect.BUTTER;
        }
        return requestedEffect;
    }

    public void tick(GameBoard board, List<Zombie> zombies, Consumer<Zombie> onZombieKilled) {
        tick(board, zombies, (zombie, killer, projectileId) -> onZombieKilled.accept(zombie), null);
    }

    public void tick(GameBoard board, List<Zombie> zombies,
                     ProjectileKillCallback onZombieKilled, GameContext context) {
        ticking = true;
        try {
            Iterator<Projectile> iterator = projectiles.iterator();
            while (iterator.hasNext()) {
                Projectile projectile = iterator.next();
                if (projectile.isExpired()) {
                    iterator.remove();
                    continue;
                }
                move(projectile, board, zombies);
                if (projectile.isExpired()) {
                    iterator.remove();
                    continue;
                }
                if (projectile.isFromZombie()) {
                    Plant target = board.getPlantAt((int) Math.floor(projectile.getX()), projectile.getRow());
                    if (target != null && target.canBeTargetedByZombie()) {
                        applyHostileHit(projectile, target, context);
                        iterator.remove();
                        continue;
                    }
                    projectile.decrementLifetime();
                    continue;
                }
                if (context != null && projectile.getProfile().trajectory()
                        != ProjectileProfile.Trajectory.ARCING
                        && hitBoardObject(projectile, context)) {
                    iterator.remove();
                    continue;
                }
                Zombie hit = findTarget(projectile, zombies);
                if (hit != null) {
                    if (context != null && hit.interceptProjectile(projectile, context)) {
                        iterator.remove();
                        continue;
                    }
                    projectile.recordHit(hit.getId());
                    applyHit(projectile, hit, board, zombies, onZombieKilled, context);
                    if (!projectile.canPierce()) iterator.remove();
                     else projectile.consumePierce();
                }
                projectile.decrementLifetime();
            }
        } finally {
            ticking = false;
            projectiles.addAll(pendingProjectiles);
            pendingProjectiles.clear();
        }
    }

    private void applyHostileHit(Projectile projectile, Plant target, GameContext context) {
        target.takeDamage(projectile.getDamage());
        if (projectile.getEffect() == ProjectileEffect.SNOWBALL) {
            if (context != null) {
                context.registerHunterIceHit(target);
            } else {
                target.addHostileIceStack(projectile.getHostileSourceId());
            }
        } else if (projectile.getEffect() == ProjectileEffect.FIRE) {
            target.clearHostileIce();
        }
    }

    private boolean hitBoardObject(Projectile projectile, GameContext context) {
        for (PlantCovering covering : context.getPlantCoverings()) {
            if (covering.isAlive() && covering.blocksStraightProjectiles()
                    && covering.getRow() == projectile.getRow()
                    && Math.abs(covering.getX() - projectile.getX()) <= 0.35) {
                if (covering.getType() == PlantCovering.Type.HUNTER_ICE
                        && projectile.getEffect() == ProjectileEffect.FIRE) {
                    covering.takeDamage(covering.getHealth());
                } else {
                    covering.takeDamage(projectile.getDamage());
                }
                return true;
            }
        }
        for (ArcadeObstacle obstacle : context.getArcadeObstacles()) {
            if (obstacle.isAlive() && obstacle.blocksStraightProjectiles()
                    && obstacle.getRow() == projectile.getRow()
                    && Math.abs(obstacle.getX() - projectile.getX()) <= 0.35) {
                obstacle.takeDamage(projectile.getDamage());
                return true;
            }
        }
        return false;
    }

    private void move(Projectile projectile, GameBoard board, List<Zombie> zombies) {
        if (projectile.getProfile().homing() && !projectile.isFromZombie()) {
            Zombie target = nearestLivingZombieAhead(projectile, zombies);
            if (target != null) {
                projectile.setRow(target.getRow());
            }
        }
        double speed = projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.ARCING ? 0.25 : 0.3;
        if (projectile.isFromZombie()) {
            projectile.setX(projectile.getX() - speed);
        } else {
            projectile.setX(projectile.getX() + speed);
        }
        int col = (int) Math.floor(projectile.getX());
        int row = projectile.getRow();
        if (board.inBounds(col, row) && projectile.getProfile().trajectory() != ProjectileProfile.Trajectory.ARCING
                && board.getTile(col, row).blocksProjectiles()) {
            projectile.setX(-1);
        }
    }

    private Zombie nearestLivingZombieAhead(Projectile projectile, List<Zombie> zombies) {
        Zombie nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        boolean prioritizeGargantuars = projectile.getSource() != null
                && projectile.getSource().getStats()
                .hasSpecialModifier(PlantSpecialModifiers.PRIORITIZE_GARGANTUARS);
        if (prioritizeGargantuars) {
            for (Zombie zombie : zombies) {
                double distance = zombie.getX() - projectile.getX();
                if (zombie.isAlive() && !zombie.isHypnotized()
                        && zombie.getType().toLowerCase().contains("gargantuar")
                        && distance >= 0 && distance < nearestDistance) {
                    nearest = zombie;
                    nearestDistance = distance;
                }
            }
            if (nearest != null) {
                return nearest;
            }
        }
        for (Zombie zombie : zombies) {
            double distance = zombie.getX() - projectile.getX();
            if (zombie.isAlive() && !zombie.isHypnotized()
                    && distance >= 0 && distance < nearestDistance) {
                nearest = zombie;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private Zombie findTarget(Projectile projectile, List<Zombie> zombies) {
        for (Zombie zombie : zombies) {
            if (zombie.isDead() || zombie.isHypnotized()
                    || projectile.hasHit(zombie.getId())) {
                continue;
            }
            if (zombie.getRow() != projectile.getRow()) {
                continue;
            }
            double dx = zombie.getX() - projectile.getX();
            if (Math.abs(dx) <= 0.3) {
                return zombie;
            }
        }
        return null;
    }

    private void applyHit(Projectile projectile, Zombie zombie, GameBoard board, List<Zombie> zombies,
                          ProjectileKillCallback onZombieKilled, GameContext context) {
        int damage = projectile.getDamage();
        if (projectile.getEffect() == ProjectileEffect.FIRE) {
            damage *= 2;
            zombie.clearColdStatuses();
        }
        if (projectile.getEffect() == ProjectileEffect.POISON) {
            int poisonBonus = projectile.getSource() == null ? 0
                    : (int) projectile.getSource().getStats()
                    .specialModifier(PlantSpecialModifiers.POISON_TICK_BUFF);
            zombie.applyPoison(50, 5 + poisonBonus);
            zombie.takeDirectDamage(damage);
        } else if (projectile.getEffect() == ProjectileEffect.ICE
                || projectile.getEffect() == ProjectileEffect.SNOWBALL) {
            boolean immune = context != null && context.areZombiesImmuneToChill();
            if (!immune) {
                int chillExt = projectile.getSource() == null ? 0
                        : (int) (projectile.getSource().getStats()
                        .specialModifier(PlantSpecialModifiers.CHILL_DURATION_EXT) * 10);
                zombie.applyChill(30 + chillExt);
            }
            zombie.takeDamage(damage);
        } else if (projectile.getEffect() == ProjectileEffect.BUTTER) {
            zombie.applyFreeze(20);
            zombie.takeDamage(damage);
        } else {
            zombie.takeDamage(damage);
        }
        if (projectile.getEffect() == ProjectileEffect.FIRE) {
            int warmRadius = projectile.getSource() == null ? 0
                    : (int) projectile.getSource().getStats()
                    .specialModifier(PlantSpecialModifiers.WARM_RADIUS_EXT);
            meltIceNear(board, zombie.getRow(), (int) zombie.getX(), warmRadius);
        }
        String killer = projectile.getSource() == null ? null : projectile.getSource().getName();
        applySplash(projectile, zombie, zombies, onZombieKilled, killer);
        if (zombie.isDead()) {
            onZombieKilled.accept(zombie, killer, projectile.getId());
        }
    }

    private void applySplash(Projectile projectile, Zombie primary, List<Zombie> zombies,
                             ProjectileKillCallback onZombieKilled, String killer) {
        Plant source = projectile.getSource();
        if (source == null || !source.getStats().hasSpecialModifier(PlantSpecialModifiers.SPLASH_DAMAGE_BUFF)) {
            return;
        }
        int splashDamage = (int) source.getStats().specialModifier(PlantSpecialModifiers.SPLASH_DAMAGE_BUFF);
        for (Zombie other : zombies) {
            if (other.isDead() || other == primary) {
                continue;
            }
            if (Math.abs(other.getRow() - primary.getRow()) <= 1
                    && Math.abs(other.getX() - primary.getX()) <= 1.0) {
                other.takeDamage(splashDamage);
                if (other.isDead()) {
                    onZombieKilled.accept(other, killer, projectile.getId());
                }
            }
        }
    }

    private void meltIceNear(GameBoard board, int row, int col, int bonusRadius) {
        int radius = 1 + bonusRadius;
        for (int targetRow = row - bonusRadius; targetRow <= row + bonusRadius; targetRow++) {
            for (int targetCol = col - radius; targetCol <= col + radius; targetCol++) {
                if (board.inBounds(targetCol, targetRow)
                        && board.getTile(targetCol, targetRow).isIce()) {
                    board.setTile(targetCol, targetRow, new NormalTile());
                }
            }
        }
    }
}
