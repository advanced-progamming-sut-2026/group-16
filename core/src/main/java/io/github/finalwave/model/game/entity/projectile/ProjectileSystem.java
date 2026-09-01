package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.NormalTile;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.zombie.ArcadeObstacle;
import io.github.finalwave.model.game.entity.zombie.PianoObstacle;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.GameContext;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public final class ProjectileSystem {

    private static final float CACTUS_SPIKE_ANCHOR_Y = 0.88f;
    private static final double HOMING_PROJECTILE_SPEED = 0.34;
    private static final double BOWLING_SPEED = 0.32;
    private static final int[] THREEPEATER_LANE_OFFSETS = {-1, 0, 1};
    private static final double ARC_LAND_TILES = 4.0;

    private final List<Projectile> projectiles = new java.util.ArrayList<>();
    private final List<Projectile> pendingProjectiles = new java.util.ArrayList<>();
    private final List<FumeHitMark> fumeHits = new java.util.ArrayList<>();
    private final Random random = new Random();
    private boolean ticking;
    private long nextFumeHitId = 1;

    public List<Projectile> getProjectiles() {
        return List.copyOf(projectiles);
    }

    public List<FumeHitMark> getFumeHits() {
        return List.copyOf(fumeHits);
    }

    public List<FumeHitMark> drainFumeHits() {
        if (fumeHits.isEmpty()) {
            return List.of();
        }
        List<FumeHitMark> drained = List.copyOf(fumeHits);
        fumeHits.clear();
        return drained;
    }

    public void replaceAll(List<Projectile> next) {
        projectiles.clear();
        pendingProjectiles.clear();
        if (next == null || next.isEmpty()) {
            return;
        }
        for (Projectile projectile : next) {
            if (projectile != null) {
                projectiles.add(projectile);
            }
        }
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
        spawnFromPlant(plant, damage, shots, profile, effect, GameBoard.DEFAULT_ROWS);
    }

    public void spawnFromPlant(Plant plant, int damage, int shots,
                               ProjectileProfile profile, ProjectileEffect effect, boolean reverse) {
        if (!reverse) {
            spawnFromPlant(plant, damage, shots, profile, effect, GameBoard.DEFAULT_ROWS);
            return;
        }
        int additionalPierce = (int) plant.getStats()
                .specialModifier(PlantSpecialModifiers.ADDITIONAL_PIERCE);
        int pierce = profile.piercing() ? 1 + additionalPierce : additionalPierce;
        double startX = plant.getCol() - 0.15;
        for (int i = 0; i < shots; i++) {
            ProjectileEffect resolvedEffect = resolveEffect(plant, effect);
            Projectile projectile = new Projectile(
                    plant.getRow(),
                    startX,
                    damage,
                    profile,
                    resolvedEffect,
                    plant,
                    pierce);
            projectile.setReverse(true);
            applyPlantSpawnVisuals(plant, resolvedEffect, projectile);
            applyPlantProjectileClip(plant, resolvedEffect, projectile);
            projectiles.add(projectile);
        }
    }

    public void spawnFromPlant(Plant plant, int damage, int shots,
                               ProjectileProfile profile, ProjectileEffect effect, int boardRows) {
        int additionalPierce = (int) plant.getStats()
                .specialModifier(PlantSpecialModifiers.ADDITIONAL_PIERCE);
        int pierce = profile.piercing() ? 1 + additionalPierce : additionalPierce;
        if (isThreepeater(plant)) {
            int volleys = Math.max(1, shots / 3);
            for (int volley = 0; volley < volleys; volley++) {
                for (int offset : THREEPEATER_LANE_OFFSETS) {
                    int row = plant.getRow() + offset;
                    if (row < 0 || row >= boardRows) {
                        continue;
                    }
                    addPlantShot(plant, row, plant.getCol() + 0.5, damage, profile, effect, pierce, 0);
                }
            }
            return;
        }
        if (isPeaPod(plant)) {
            int heads = Math.max(1, plant.getStackCount());
            int count = Math.max(1, shots);
            for (int i = 0; i < count; i++) {
                spawnPeaPodHead(plant, damage, profile, effect, i % heads, pierce);
            }
            return;
        }
        if (plant != null && plant.isFumeShroom()) {
            addPlantShot(plant, plant.getRow(), plant.getCol() + 0.5, damage, profile, effect, pierce, 0);
            return;
        }
        for (int i = 0; i < shots; i++) {
            addPlantShot(plant, plant.getRow(), plant.getCol() + 0.5, damage, profile, effect, pierce, 0);
        }
    }

    public void spawnPeaPodHead(Plant plant, int damage, ProjectileProfile profile,
                                ProjectileEffect effect, int muzzleIndex) {
        int additionalPierce = (int) plant.getStats()
                .specialModifier(PlantSpecialModifiers.ADDITIONAL_PIERCE);
        int pierce = profile.piercing() ? 1 + additionalPierce : additionalPierce;
        spawnPeaPodHead(plant, damage, profile, effect, muzzleIndex, pierce);
    }

    public void spawnPeaPodGiant(Plant plant, int damage, ProjectileEffect effect) {
        double startX = plant.getCol() + 0.5 + PeaPodMuzzles.giantX();
        spawn(new Projectile(
                plant.getRow(),
                startX,
                damage,
                ProjectileProfile.straight(),
                resolveEffect(plant, effect),
                plant,
                0,
                PeaPodMuzzles.giantY(),
                true));
    }

    public void spawnCabbagePlantFood(Plant plant, int damage) {
        double startX = plant.getCol() + 0.5 + CabbageMuzzles.x();
        spawn(new Projectile(
                plant.getRow(),
                startX,
                damage,
                ProjectileProfile.arcing(),
                plant.projectileEffect(),
                plant,
                0,
                CabbageMuzzles.y(),
                false,
                false,
                true));
    }

    public void spawnKernelPlantFood(Plant plant, int damage) {
        double startX = plant.getCol() + 0.5 + KernelMuzzles.plantFoodX();
        spawn(new Projectile(
                plant.getRow(),
                startX,
                damage,
                ProjectileProfile.arcing(),
                ProjectileEffect.KERNEL,
                plant,
                0,
                KernelMuzzles.plantFoodY()));
    }

    public void spawnMelonPlantFood(Plant plant, int damage) {
        double startX = plant.getCol() + 0.5 + MelonMuzzles.plantFoodX();
        spawn(new Projectile(
                plant.getRow(),
                startX,
                damage,
                ProjectileProfile.arcing(),
                ProjectileEffect.MELON,
                plant,
                0,
                MelonMuzzles.plantFoodY(),
                false,
                false,
                false,
                true));
    }

    public void spawnWinterMelonPlantFood(Plant plant, int damage) {
        double startX = plant.getCol() + 0.5 + MelonMuzzles.plantFoodX();
        spawn(new Projectile(
                plant.getRow(),
                startX,
                damage,
                ProjectileProfile.arcing(),
                ProjectileEffect.WINTER_MELON,
                plant,
                0,
                MelonMuzzles.plantFoodY(),
                false,
                false,
                false,
                true));
    }

    public void spawnPepperPlantFood(Plant plant, int damage, int muzzleIndex) {
        double startX = plant.getCol() + 0.5 + PepperMuzzles.plantFoodX(muzzleIndex);
        spawn(new Projectile(
                plant.getRow(),
                startX,
                damage,
                ProjectileProfile.arcing(),
                ProjectileEffect.PEPPER,
                plant,
                0,
                PepperMuzzles.plantFoodY(muzzleIndex),
                false,
                false,
                false,
                false,
                true));
    }

    public void spawnGrapeshotGrapes(Plant plant, int count, int damage, int boardRows, int boardCols) {
        if (plant == null || count <= 0) {
            return;
        }
        double startX = plant.getCol() + 0.5;
        double startRow = plant.getRow() + 0.0;
        double step = (Math.PI * 2.0) / count;
        for (int i = 0; i < count; i++) {
            double jitter = (random.nextDouble() * 2.0 - 1.0) * GrapeshotMuzzles.GRAPE_JITTER_RADIANS;
            double angle = i * step + jitter;
            double speed = GrapeshotMuzzles.GRAPE_SPEED_TILES_PER_TICK;
            double velocityX = Math.cos(angle) * speed;
            double velocityY = Math.sin(angle) * speed;
            spawn(Projectile.grapeshotGrape(
                    plant,
                    startX,
                    startRow,
                    velocityX,
                    velocityY,
                    damage));
        }
    }

    private void spawnPeaPodHead(Plant plant, int damage, ProjectileProfile profile,
                                 ProjectileEffect effect, int muzzleIndex, int pierce) {
        int heads = Math.max(1, plant.getStackCount());
        int index = Math.max(0, Math.min(heads - 1, muzzleIndex));
        double startX = plant.getCol() + 0.5 + PeaPodMuzzles.x(heads, index);
        addPlantShot(plant, plant.getRow(), startX, damage, profile, effect, pierce,
                PeaPodMuzzles.y(heads, index));
    }

    private void addPlantShot(Plant plant, int row, double startX, int damage, ProjectileProfile profile,
                              ProjectileEffect effect, int pierce, double laneYOffset) {
        double x = startX;
        double yOffset = laneYOffset;
        boolean fumeFood = plant != null && plant.isFumeShroom() && plant.isPlantFooding();
        if (plant != null && plant.isFumeShroom()) {
            x += fumeFood ? FumeMuzzles.plantFoodX() : FumeMuzzles.x();
            yOffset = fumeFood ? FumeMuzzles.plantFoodY() : FumeMuzzles.y();
        }
        if (plant != null && plant.isKernelPult()) {
            x += KernelMuzzles.x();
            yOffset = KernelMuzzles.y();
        }
        if (plant != null && (plant.isMelonPult() || plant.isWinterMelon())) {
            x += MelonMuzzles.x();
            yOffset = MelonMuzzles.y();
        }
        if (plant != null && plant.isPepperPult()) {
            x += PepperMuzzles.x();
            yOffset = PepperMuzzles.y();
        }
        Projectile shot = new Projectile(
                row,
                x,
                damage,
                profile,
                resolveEffect(plant, effect),
                plant,
                pierce,
                yOffset,
                false,
                fumeFood);
        if (plant != null && plant.isFumeShroom()) {
            shot.setLifetimeTicks(fumeFood
                    ? FumeMuzzles.PLANTFOOD_CLOUD_TICKS
                    : FumeMuzzles.ATTACK_CLOUD_TICKS);
        }
        applyPlantSpawnVisuals(plant, shot.getEffect(), shot);
        applyPlantProjectileClip(plant, shot.getEffect(), shot);
        projectiles.add(shot);
    }

    private static boolean isThreepeater(Plant plant) {
        return plant != null && "Threepeater".equals(plant.getName());
    }

    private static boolean isPeaPod(Plant plant) {
        return plant != null && plant.isPeaPod();
    }

    private static boolean isFumeCloud(Projectile projectile) {
        return projectile != null
                && !projectile.isFromZombie()
                && projectile.getEffect() == ProjectileEffect.FUME;
    }

    private static boolean isArcing(Projectile projectile) {
        return projectile != null
                && projectile.getProfile() != null
                && projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.ARCING;
    }

    private static boolean isBouncing(Projectile projectile) {
        return projectile != null && projectile.isGrapeshotGrape();
    }

    private double resolveArcLandX(Projectile projectile, List<Zombie> zombies, GameBoard board) {
        double launch = projectile.getX();
        double land = Double.NaN;
        if (zombies != null) {
            for (Zombie zombie : zombies) {
                if (zombie.isDead() || zombie.isHypnotized()
                        || zombie.getRow() != projectile.getRow()) {
                    continue;
                }
                if (zombie.getX() <= launch) {
                    continue;
                }
                if (Double.isNaN(land) || zombie.getX() < land) {
                    land = zombie.getX();
                }
            }
        }
        if (Double.isNaN(land)) {
            land = launch + ARC_LAND_TILES;
        }
        return Math.min(land, board.getCols());
    }

    private void hitFumeLane(Projectile projectile, GameBoard board, List<Zombie> zombies,
                             ProjectileKillCallback onZombieKilled, GameContext context) {
        if (!FumeMuzzles.isHitTick(projectile.getFumeAgeTicks())) {
            return;
        }
        Plant source = projectile.getSource();
        double origin = source != null ? source.getCol() + 0.5 : projectile.getX();
        double range = projectile.isFumePlantFood()
                ? FumeMuzzles.PLANTFOOD_RANGE_TILES
                : FumeMuzzles.RANGE_TILES;
        if (source != null) {
            range += source.getStats().specialModifier(PlantSpecialModifiers.TILE_RANGE_EXT);
        }
        for (Zombie zombie : zombies) {
            if (zombie.isDead() || zombie.isHypnotized()
                    || zombie.getRow() != projectile.getRow()) {
                continue;
            }
            if (!FumeMuzzles.inRangeFromCenter(origin, zombie.getX(), range)) {
                continue;
            }
            if (context != null && zombie.interceptProjectile(projectile, context)) {
                continue;
            }
            fumeHits.add(new FumeHitMark(nextFumeHitId++, zombie.getRow(), zombie.getX()));
            applyHit(projectile, zombie, board, zombies, onZombieKilled, context);
        }
    }

    public void spawnBowlingFromPlant(Plant plant, int damage, ProjectileEffect effect) {
        ProjectileEffect resolvedEffect = resolveEffect(plant, effect);
        Projectile projectile = new Projectile(
                plant.getRow(),
                plant.getCol() + 0.5,
                damage,
                ProjectileProfile.straight(),
                resolvedEffect,
                plant,
                0);
        projectile.setBowlingBouncesRemaining(bowlingBouncesForEffect(resolvedEffect));
        projectiles.add(projectile);
    }

    private static void applyPlantSpawnVisuals(Plant plant, ProjectileEffect effect, Projectile projectile) {
        if (plant == null) {
            return;
        }
        if ("Cactus".equals(plant.getName())
                && (effect == ProjectileEffect.SPIKE || effect == ProjectileEffect.SPIKE_PF)) {
            projectile.setVisualAnchorY(CACTUS_SPIKE_ANCHOR_Y);
        }
    }

    private static void applyPlantProjectileClip(Plant plant, ProjectileEffect effect, Projectile projectile) {
        if (plant == null || effect == null) {
            return;
        }
        if (effect == ProjectileEffect.GOO) {
            int tier = Math.min(3, Math.max(1, plant.getLevel()));
            projectile.setVisualClip("projectile_t" + tier);
            return;
        }
        if (effect == ProjectileEffect.STAR) {
            int tier = Math.min(3, Math.max(1, plant.getLevel()));
            projectile.setVisualClip(tier == 1 ? "animation" : "animation" + tier);
            return;
        }
        if (effect == ProjectileEffect.MEGA_GATLING_PEA) {
            projectile.setVisualClip(plant.isUsingPlantFood() ? "animation3" : "animation");
            return;
        }
        if (effect == ProjectileEffect.SEA_SHROOM && plant.isUsingPlantFood()) {
            projectile.setVisualClip("animation2");
        }
    }

    private static int bowlingBouncesForEffect(ProjectileEffect effect) {
        return switch (effect) {
            case BOWLING_BLUE -> 2;
            case BOWLING_ORANGE, BOWLING_PF -> 3;
            case BOWLING_CYAN -> 1;
            default -> 0;
        };
    }

    private static boolean isBowlingEffect(ProjectileEffect effect) {
        return effect == ProjectileEffect.BOWLING_CYAN
                || effect == ProjectileEffect.BOWLING_BLUE
                || effect == ProjectileEffect.BOWLING_ORANGE
                || effect == ProjectileEffect.BOWLING_PF;
    }

    public void spawnDirectedFromPlant(Plant plant, int damage, double vx, double vy,
                                       ProjectileProfile profile, ProjectileEffect effect, float visualScale) {
        spawnDirectedFromPlant(plant, damage, vx, vy, profile, effect, visualScale, 0, 0);
    }

    public void spawnDirectedFromPlant(Plant plant, int damage, double vx, double vy,
                                       ProjectileProfile profile, ProjectileEffect effect, float visualScale,
                                       double laneOffset, double extraX) {
        int additionalPierce = (int) plant.getStats()
                .specialModifier(PlantSpecialModifiers.ADDITIONAL_PIERCE);
        int pierce = profile.piercing() ? 1 + additionalPierce : additionalPierce;
        Projectile resolved = new Projectile(
                plant.getRow(),
                plant.getCol() + 0.5 + extraX,
                damage,
                profile,
                resolveEffect(plant, effect),
                plant,
                pierce);
        resolved.setY(plant.getRow());
        resolved.setVelocity(vx, vy);
        resolved.setVisualScale(visualScale);
        resolved.setVisualLaneOffset(laneOffset);
        applyPlantSpawnVisuals(plant, resolveEffect(plant, effect), resolved);
        applyPlantProjectileClip(plant, resolveEffect(plant, effect), resolved);
        projectiles.add(resolved);
    }

    public void spawnLaneClearFromPlant(Plant plant, int damage, ProjectileEffect effect) {
        ProjectileProfile profile = ProjectileProfile.piercingProfile();
        float scale = switch (effect) {
            case PLASMA_PF -> 1.65f;
            case GOO_PF -> 1f;
            default -> 1.45f;
        };
        double speed = effect == ProjectileEffect.GOO_PF ? 0.45 : 0.55;
        Projectile projectile = new Projectile(
                plant.getRow(),
                plant.getCol() + 0.5,
                damage,
                profile,
                resolveEffect(plant, effect),
                plant,
                99);
        projectile.setVelocity(speed, 0);
        projectile.setVisualScale(scale);
        applyPlantSpawnVisuals(plant, resolveEffect(plant, effect), projectile);
        applyPlantProjectileClip(plant, effect, projectile);
        projectiles.add(projectile);
    }

    public void spawnPoisonLaneBallFromPlant(Plant plant, int damage) {
        spawnLaneClearFromPlant(plant, damage, ProjectileEffect.GOO_PF);
    }

    public void spawnPiercingFromPlant(Plant plant, int damage, ProjectileEffect effect, int pierce) {
        ProjectileEffect resolvedEffect = resolveEffect(plant, effect);
        Projectile projectile = new Projectile(
                plant.getRow(),
                plant.getCol() + 0.5,
                damage,
                ProjectileProfile.piercingProfile(),
                resolvedEffect,
                plant,
                Math.max(1, pierce));
        applyPlantSpawnVisuals(plant, resolvedEffect, projectile);
        projectiles.add(projectile);
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
                if (isFumeCloud(projectile)) {
                    projectile.advanceFumeAge();
                    hitFumeLane(projectile, board, zombies, onZombieKilled, context);
                    projectile.decrementLifetime();
                    if (projectile.isExpired()) {
                        iterator.remove();
                    }
                    continue;
                }
                if (isBouncing(projectile)) {
                    moveGrapeshotGrape(projectile, board);
                    Zombie hit = findGrapeshotHit(projectile, zombies);
                    if (hit != null) {
                        if (context != null && hit.interceptProjectile(projectile, context)) {
                            iterator.remove();
                            continue;
                        }
                        projectile.recordHit(hit.getId());
                        applyHit(projectile, hit, board, zombies, onZombieKilled, context);
                        iterator.remove();
                        continue;
                    }
                    projectile.decrementLifetime();
                    if (projectile.isExpired()) {
                        iterator.remove();
                    }
                    continue;
                }
                if (isArcing(projectile) && !projectile.isFromZombie()) {
                    if (!projectile.hasLandX()) {
                        projectile.setLandX(resolveArcLandX(projectile, zombies, board));
                    }
                }
                move(projectile, board, zombies, context);
                if (projectile.isExpired()) {
                    iterator.remove();
                    continue;
                }
                if (isArcing(projectile) && !projectile.isFromZombie()
                        && projectile.hasLandX()
                        && projectile.getX() >= projectile.getLandX()) {
                    Zombie landedOn = findTarget(projectile, zombies);
                    if (landedOn != null) {
                        if (context == null || !landedOn.interceptProjectile(projectile, context)) {
                            projectile.recordHit(landedOn.getId());
                            applyHit(projectile, landedOn, board, zombies, onZombieKilled, context);
                        }
                    }
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
                if (context != null && !passesObstacles(projectile)
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
                    if (tryBowlingBounce(projectile, hit, context)) {
                        projectile.decrementLifetime();
                        continue;
                    }
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
        for (PianoObstacle obstacle : context.getPianoObstacles()) {
            if (obstacle.isAlive() && obstacle.blocksStraightProjectiles()
                    && obstacle.getRow() == projectile.getRow()
                    && Math.abs(obstacle.getX() - projectile.getX()) <= 0.35) {
                obstacle.takeDamage(projectile.getDamage());
                return true;
            }
        }
        return false;
    }

    private void moveGrapeshotGrape(Projectile projectile, GameBoard board) {
        double x = projectile.getX() + projectile.getVelocityX();
        double rowPos = projectile.getRowPosition() + projectile.getVelocityY();
        double velocityX = projectile.getVelocityX();
        double velocityY = projectile.getVelocityY();
        double maxCol = board.getCols() - 1;
        double maxRow = board.getRows() - 1;
        if (x < 0) {
            x = -x;
            velocityX = -velocityX;
        } else if (x > maxCol) {
            x = maxCol - (x - maxCol);
            velocityX = -velocityX;
        }
        if (rowPos < 0) {
            rowPos = -rowPos;
            velocityY = -velocityY;
        } else if (rowPos > maxRow) {
            rowPos = maxRow - (rowPos - maxRow);
            velocityY = -velocityY;
        }
        projectile.setX(x);
        projectile.setRowPosition(rowPos);
        projectile.setVelocity(velocityX, velocityY);
    }

    private Zombie findGrapeshotHit(Projectile projectile, List<Zombie> zombies) {
        for (Zombie zombie : zombies) {
            if (zombie.isDead() || zombie.isHypnotized()) {
                continue;
            }
            double dx = zombie.getX() - projectile.getX();
            double dy = zombie.getRow() - projectile.getRowPosition();
            if (Math.hypot(dx, dy) <= GrapeshotMuzzles.GRAPE_HIT_RADIUS) {
                return zombie;
            }
        }
        return null;
    }

    private void move(Projectile projectile, GameBoard board, List<Zombie> zombies,
                      GameContext context) {
        if (projectile.getProfile().homing() && !projectile.isFromZombie()) {
            steerHomingProjectile(projectile, zombies);
        }
        double speed = projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.ARCING ? 0.25 : 0.3;
        if (projectile.isDirected()) {
            projectile.setX(projectile.getX() + projectile.getVx());
            projectile.setY(projectile.getY() + projectile.getVy());
        } else if (projectile.isFromZombie() || projectile.isReverse()) {
            projectile.setX(projectile.getX() - speed);
        } else {
            projectile.setX(projectile.getX() + speed);
        }
        if (passesObstacles(projectile) || projectile.isFromZombie()) {
            return;
        }
        int col = (int) Math.floor(projectile.getX());
        int row = projectile.getRow();
        if (!board.inBounds(col, row)) {
            return;
        }
        var tile = board.getTile(col, row);
        if (tile != null && tile.isGrave()) {
            applyTileDamage(board, context, col, row, projectile, true);
            projectile.setX(-1);
            return;
        }
        if (tile != null && tile.isIce()) {
            applyTileDamage(board, context, col, row, projectile, false);
            projectile.setX(-1);
        }
        if (context != null) {
            applyTorchwoodPassThrough(projectile, context);
        }
    }

    private void applyTorchwoodPassThrough(Projectile projectile, GameContext context) {
        if (projectile.isTorchwoodBoosted() || projectile.isFromZombie() || projectile.isDirected()) {
            return;
        }
        int col = (int) Math.floor(projectile.getX());
        Plant plant = context.getPlantAt(col, projectile.getRow());
        if (plant != null && plant.isAlive() && "Torchwood".equals(plant.getName())) {
            projectile.applyTorchwoodBoost();
        }
    }

    private void applyTileDamage(GameBoard board, GameContext context, int col, int row,
                                 Projectile projectile, boolean grave) {
        int damage = projectile.getDamage();
        if (!grave && projectile.getEffect() == ProjectileEffect.FIRE) {
            damage = IceTile.MAX_HEALTH;
        }
        if (context != null) {
            if (grave) {
                context.damageGraveAt(col, row, damage);
            } else {
                context.damageIceAt(col, row, damage);
            }
            return;
        }
        var tile = board.getTile(col, row);
        if (grave && tile instanceof GraveTile graveTile) {
            graveTile.takeDamage(damage);
            if (graveTile.isDestroyed()) {
                board.setTile(col, row, new NormalTile());
            }
        } else if (!grave && tile instanceof IceTile iceTile) {
            iceTile.takeDamage(damage);
            if (iceTile.isDestroyed()) {
                board.setTile(col, row, new NormalTile());
            }
        }
    }

    private void steerHomingProjectile(Projectile projectile, List<Zombie> zombies) {
        Zombie target = nearestHomingTarget(projectile, zombies);
        if (target == null) {
            return;
        }
        double dx = target.getX() + 0.15 - projectile.getX();
        double dy = target.getRow() - projectile.getY();
        double length = Math.max(0.001, Math.sqrt(dx * dx + dy * dy));
        double travelSpeed = 0.34;
        projectile.setVelocity(travelSpeed * dx / length, travelSpeed * dy / length);
    }

    private Zombie nearestHomingTarget(Projectile projectile, List<Zombie> zombies) {
        Zombie nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        boolean prioritizeGargantuars = projectile.getSource() != null
                && projectile.getSource().getStats()
                .hasSpecialModifier(PlantSpecialModifiers.PRIORITIZE_GARGANTUARS);
        if (prioritizeGargantuars) {
            for (Zombie zombie : zombies) {
                if (!isHomingCandidate(zombie)) {
                    continue;
                }
                if (!zombie.getType().toLowerCase().contains("gargantuar")) {
                    continue;
                }
                double distance = distanceTo(projectile, zombie);
                if (distance < nearestDistance) {
                    nearest = zombie;
                    nearestDistance = distance;
                }
            }
            if (nearest != null) {
                return nearest;
            }
        }
        for (Zombie zombie : zombies) {
            if (!isHomingCandidate(zombie)) {
                continue;
            }
            double distance = distanceTo(projectile, zombie);
            if (distance < nearestDistance) {
                nearest = zombie;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static double distanceTo(Projectile projectile, Zombie zombie) {
        double dx = zombie.getX() - projectile.getX();
        double dy = zombie.getRow() - projectile.getY();
        return Math.sqrt(dx * dx + dy * dy * 4.0);
    }

    private static boolean isHomingCandidate(Zombie zombie) {
        if (zombie == null || !zombie.isAlive() || zombie.isHypnotized()) {
            return false;
        }
        return true;
    }

    private boolean tryBowlingBounce(Projectile projectile, Zombie hit, GameContext context) {
        if (!isBowlingEffect(projectile.getEffect()) || projectile.getBowlingBouncesRemaining() <= 0) {
            return false;
        }
        int rowCount = context == null ? 5 : context.getRowCount();
        int currentRow = projectile.getRow();
        int targetRow = resolveBowlingDeflectRow(projectile, hit, rowCount);
        if (targetRow == currentRow) {
            return false;
        }
        projectile.consumeBowlingBounce();
        projectile.setRow(targetRow);
        projectile.setY(currentRow);
        double rowDelta = targetRow - currentRow;
        projectile.setVelocity(0.32, rowDelta * 0.22);
        return true;
    }

    private static int resolveBowlingDeflectRow(Projectile projectile, Zombie hit, int rowCount) {
        int currentRow = projectile.getRow();
        int maxRow = Math.max(0, rowCount - 1);
        int preferred = projectile.getY() <= hit.getRow() - 0.05 ? currentRow - 1 : currentRow + 1;
        if (preferred >= 0 && preferred <= maxRow && preferred != currentRow) {
            return preferred;
        }
        int alternate = preferred < currentRow ? currentRow + 1 : currentRow - 1;
        if (alternate >= 0 && alternate <= maxRow && alternate != currentRow) {
            return alternate;
        }
        return currentRow;
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
            double dx = zombie.getX() - projectile.getX();
            double reach = zombie.isBoss() ? 1.45 : 0.3;
            if (projectile.isDirected()) {
                double dy = zombie.getRow() - projectile.getY();
                if (Math.abs(dx) <= reach && Math.abs(dy) <= 0.55) {
                    return zombie;
                }
                continue;
            }
            if (!zombie.occupiesRow(projectile.getRow())) {
                continue;
            }
            if (projectile.isFromZombie()) {
                if (Math.abs(dx) <= reach) {
                    return zombie;
                }
                continue;
            }
            if (projectile.isReverse()) {
                if (dx <= 0.35 && dx >= -reach) {
                    return zombie;
                }
                continue;
            }
            if (dx >= -0.35 && dx <= reach) {
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
        if (projectile.getEffect() == ProjectileEffect.POISON
                || projectile.getEffect() == ProjectileEffect.GOO
                || projectile.getEffect() == ProjectileEffect.GOO_PF) {
            zombie.setSuppressHitFlash(true);
            int poisonBonus = projectile.getSource() == null ? 0
                    : (int) projectile.getSource().getStats()
                    .specialModifier(PlantSpecialModifiers.POISON_TICK_BUFF);
            int poisonTicks = projectile.getEffect() == ProjectileEffect.GOO_PF ? 120 : 50;
            int poisonDamage = projectile.getEffect() == ProjectileEffect.GOO_PF ? 8 : 5 + poisonBonus;
            zombie.applyPoison(poisonTicks, poisonDamage);
            zombie.applyChill(projectile.getEffect() == ProjectileEffect.GOO_PF ? 120 : 20);
            zombie.takeDirectDamage(damage);
            if (projectile.getEffect() == ProjectileEffect.GOO_PF) {
                zombie.moveRight(0.75);
                if (context != null) {
                    context.addGooPuddle((int) Math.floor(zombie.getX()), zombie.getRow(), 80);
                }
            }
        } else if (projectile.getEffect() == ProjectileEffect.ICE
                || projectile.getEffect() == ProjectileEffect.SNOWBALL
                || projectile.getEffect() == ProjectileEffect.WINTER_MELON) {
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
        } else if (projectile.getEffect() == ProjectileEffect.MAGIC_BEAM) {
            zombie.hypnotize(1.0, 1.0);
        } else if (projectile.getEffect() == ProjectileEffect.PEPPER) {
            zombie.clearColdStatuses();
            zombie.takeDamage(damage);
        } else {
            zombie.takeDamage(damage);
        }
        if (projectile.getEffect() == ProjectileEffect.FIRE
                || projectile.getEffect() == ProjectileEffect.PEPPER) {
            int warmRadius = projectile.getSource() == null ? 0
                    : (int) projectile.getSource().getStats()
                    .specialModifier(PlantSpecialModifiers.WARM_RADIUS_EXT);
            meltIceNear(board, zombie.getRow(), (int) zombie.getX(), warmRadius, context);
        }
        String killer = projectile.getSource() == null ? null : projectile.getSource().getName();
        applySplash(projectile, zombie, board, zombies, onZombieKilled, killer, context);
        if (zombie.isDead()) {
            onZombieKilled.accept(zombie, killer, projectile.getId());
        }
    }

    private void applySplash(Projectile projectile, Zombie primary, GameBoard board, List<Zombie> zombies,
                             ProjectileKillCallback onZombieKilled, String killer, GameContext context) {
        if (!shouldSplash(projectile)) {
            return;
        }
        int splashDamage = splashDamage(projectile);
        boolean winter = projectile.getEffect() == ProjectileEffect.WINTER_MELON;
        boolean pepper = projectile.getEffect() == ProjectileEffect.PEPPER;
        boolean immune = context != null && context.areZombiesImmuneToChill();
        for (Zombie other : zombies) {
            if (other.isDead() || other == primary) {
                continue;
            }
            if (Math.abs(other.getRow() - primary.getRow()) <= 1
                    && Math.abs(other.getX() - primary.getX()) <= 1.0) {
                if (winter && !immune) {
                    other.applyChill(30);
                }
                if (pepper) {
                    other.clearColdStatuses();
                }
                other.takeDamage(splashDamage);
                if (other.isDead()) {
                    onZombieKilled.accept(other, killer, projectile.getId());
                }
            }
        }
        if (pepper) {
            meltIceNear(board, primary.getRow(), (int) primary.getX(), 0, context);
        }
    }

    private static boolean shouldSplash(Projectile projectile) {
        ProjectileEffect effect = projectile.getEffect();
        if (effect == ProjectileEffect.MELON
                || effect == ProjectileEffect.WINTER_MELON
                || effect == ProjectileEffect.PEPPER
                || effect == ProjectileEffect.PLASMA) {
            return true;
        }
        Plant source = projectile.getSource();
        return source != null && source.hasTag(PlantTag.AOE);
    }

    private static int splashDamage(Projectile projectile) {
        Plant source = projectile.getSource();
        if (source != null && source.getStats().hasSpecialModifier(PlantSpecialModifiers.SPLASH_DAMAGE_BUFF)) {
            int bonus = (int) source.getStats().specialModifier(PlantSpecialModifiers.SPLASH_DAMAGE_BUFF);
            if (bonus > 0) {
                return bonus;
            }
        }
        if (projectile.getEffect() == ProjectileEffect.PLASMA) {
            return Math.max(1, projectile.getDamage() / 2);
        }
        return projectile.getDamage();
    }

    private static boolean passesObstacles(Projectile projectile) {
        if (projectile.getProfile() != null) {
            if (projectile.getProfile().trajectory() == ProjectileProfile.Trajectory.ARCING) {
                return true;
            }
            if (projectile.getProfile().piercing()) {
                return true;
            }
        }
        ProjectileEffect effect = projectile.getEffect();
        return effect == ProjectileEffect.FUME
                || effect == ProjectileEffect.SPIKE
                || effect == ProjectileEffect.SPIKE_PF
                || effect == ProjectileEffect.PUFF;
    }

    private void meltIceNear(GameBoard board, int row, int col, int bonusRadius,
                             GameContext context) {
        int radius = 1 + bonusRadius;
        for (int targetRow = row - bonusRadius; targetRow <= row + bonusRadius; targetRow++) {
            for (int targetCol = col - radius; targetCol <= col + radius; targetCol++) {
                if (!board.inBounds(targetCol, targetRow)
                        || !board.getTile(targetCol, targetRow).isIce()) {
                    continue;
                }
                if (context != null) {
                    context.damageIceAt(targetCol, targetRow, IceTile.MAX_HEALTH);
                    continue;
                }
                var tile = board.getTile(targetCol, targetRow);
                if (tile instanceof IceTile ice) {
                    ice.takeDamage(IceTile.MAX_HEALTH);
                    if (ice.isDestroyed()) {
                        board.setTile(targetCol, targetRow, new NormalTile());
                    }
                } else {
                    board.setTile(targetCol, targetRow, new NormalTile());
                }
            }
        }
    }
}
