package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.game.board.BoardGameContext;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.view.gui.render.sync.PlantVisualState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlantAbilityTest {

    private PlantRegistry registry;
    private GameSession session;
    private BoardGameContext context;

    @BeforeEach
    void setUp() throws IOException {
        registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        session = new GameSession(registry, 500);
        session.start();
        context = session.getContext();
    }

    @Test
    void sunProductionSpawnsCollectible() {
        session.tryPlant("Sunflower", 2, 2, 1);
        Plant sunflower = session.getBoard().getPlantAt(2, 2);
        assertNotNull(sunflower);
        int before = session.getSunItems().size();
        for (int i = 0; i < 240; i++) {
            session.tick();
        }
        assertTrue(session.getSunItems().size() > before);
        int balanceBefore = session.getSunBalance();
        assertTrue(session.collectSun(session.getSunItems().getFirst()));
        assertTrue(session.getSunBalance() > balanceBefore);
    }

    @Test
    void explosiveTrapArmsOnAction() {
        session.tryPlant("Potato Mine", 1, 1, 1);
        Plant mine = session.getBoard().getPlantAt(1, 1);
        assertNotNull(mine);
        for (int i = 0; i < 149; i++) {
            session.tick();
        }
        assertFalse(mine.isArmedTrap());
        session.tick();
        assertTrue(mine.isArmedTrap());
    }

    @Test
    void trapUpgradeReducesArmingTime() {
        session.tryPlant("Potato Mine", 1, 1, 2);
        Plant mine = session.getBoard().getPlantAt(1, 1);
        for (int i = 0; i < 119; i++) {
            session.tick();
        }
        assertFalse(mine.isArmedTrap());
        session.tick();
        assertTrue(mine.isArmedTrap());
    }

    @Test
    void mintBoostResetsFamilyCooldown() {
        session.getCooldownTracker().startCooldown("Sunflower", 100, 10);
        assertFalse(session.getCooldownTracker().isReady("Sunflower"));
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Enlighten-mint", 4, 2, 4));
        assertTrue(session.getCooldownTracker().isReady("Sunflower"));
    }

    @Test
    void mintBoostAffectsFamilyAndExpires() {
        session.tryPlant("Sunflower", 1, 1, 1);
        Plant sunflower = session.getBoard().getPlantAt(1, 1);
        session.tryPlant("Enlighten-mint", 4, 2, 1);

        context.spawnSun(sunflower, 50);
        assertEquals(100, session.getSunItems().getLast().getValue());

        for (int i = 0; i < 100; i++) {
            session.tick();
        }
        context.spawnSun(sunflower, 50);
        assertEquals(50, session.getSunItems().getLast().getValue());
    }

    @Test
    void peashooterDoesNotShootEmptyLane() {
        session.tryPlant("Peashooter", 1, 2, 1);
        for (int i = 0; i < 40; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void actionIntervalUsesSecondsNotTicks() {
        session.tryPlant("Peashooter", 1, 2, 1);
        placeMovingZombie(7, 2);
        for (int i = 0; i < 18; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
        session.tick();
        assertFalse(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void torchwoodConvertsPeaProjectilesToFire() {
        session.tryPlant("Torchwood", 2, 2, 1);
        session.tryPlant("Peashooter", 1, 2, 1);
        placeMovingZombie(7, 2);
        for (int i = 0; i < 19; i++) {
            session.tick();
        }
        assertEquals(io.github.finalwave.model.game.entity.projectile.ProjectileEffect.FIRE,
                session.getProjectileSystem().getProjectiles().getFirst().getEffect());
    }

    @Test
    void sunProduceMarksPlant() {
        session.tryPlant("Sunflower", 2, 2, 1);
        Plant sunflower = session.getBoard().getPlantAt(2, 2);
        context.spawnSun(sunflower, 50);
        assertTrue(sunflower.isProducingSun());
    }

    @Test
    void goldBloomStaysAliveForAttackThenDies() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Gold Bloom", 3, 2, 1));
        Plant bloom = session.getBoard().getPlantAt(3, 2);
        assertNotNull(bloom);
        assertTrue(bloom.isAlive());
        assertTrue(bloom.isAttacking());
        for (int i = 0; i < 9; i++) {
            session.tick();
            assertTrue(bloom.isAlive());
        }
        session.tick();
        assertFalse(bloom.isAlive());
    }

    @Test
    void sunShroomCanProduceBeforeMaxGrowth() {
        session.tryPlant("Sun-shroom", 1, 1, 1);
        Plant shroom = session.getBoard().getPlantAt(1, 1);
        assertEquals(0, shroom.getGrowthStage());
        int before = session.getSunItems().size();
        for (int i = 0; i < 250; i++) {
            session.tick();
        }
        assertTrue(session.getSunItems().size() > before);
    }

    @Test
    void splitPeaFiresReverseAtRearZombie() {
        session.tryPlant("Split Pea", 5, 2, 1);
        placeMovingZombie(2, 2);
        for (int i = 0; i < 30; i++) {
            session.tick();
            if (!session.getProjectileSystem().getProjectiles().isEmpty()) {
                break;
            }
        }
        assertFalse(session.getProjectileSystem().getProjectiles().isEmpty());
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(io.github.finalwave.model.game.entity.projectile.Projectile::isReverse));
    }

    @Test
    void repeaterFiresTwoPeasInSuccession() {
        session.tryPlant("Repeater", 1, 2, 1);
        placeMovingZombie(7, 2);
        for (int i = 0; i < 18; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        double firstX = session.getProjectileSystem().getProjectiles().getFirst().getX();
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        session.tick();
        assertEquals(2, session.getProjectileSystem().getProjectiles().size());
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(projectile -> projectile.getX() != firstX));
    }

    @Test
    void rotobagaFiresFourDiagonalSeeds() {
        session.tryPlant("Rotobaga", 4, 2, 1);
        placeMovingZombie(6, 4);
        for (int i = 0; i < 30; i++) {
            session.tick();
            if (session.getProjectileSystem().getProjectiles().size() >= 4) {
                break;
            }
        }
        var shots = session.getProjectileSystem().getProjectiles();
        assertEquals(4, shots.size());
        assertTrue(shots.stream().allMatch(io.github.finalwave.model.game.entity.projectile.Projectile::isDirected));
        long distinctVy = shots.stream()
                .map(io.github.finalwave.model.game.entity.projectile.Projectile::getVy)
                .distinct()
                .count();
        long distinctVx = shots.stream()
                .map(io.github.finalwave.model.game.entity.projectile.Projectile::getVx)
                .distinct()
                .count();
        assertEquals(2, distinctVy);
        assertEquals(2, distinctVx);
        assertEquals(io.github.finalwave.model.game.entity.projectile.ProjectileEffect.ROTOBAGA,
                shots.getFirst().getEffect());
    }

    @Test
    void rotobagaIgnoresStraightLaneZombie() {
        session.tryPlant("Rotobaga", 1, 2, 1);
        placeMovingZombie(7, 2);
        for (int i = 0; i < 40; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void peashooterPlantFoodRapidFires() {
        session.tryPlant("Peashooter", 1, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(1, 2));
        Plant plant = session.getBoard().getPlantAt(1, 2);
        assertTrue(plant.isUsingPlantFood());
        for (int i = 0; i < 12; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().size() >= 4);
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .map(io.github.finalwave.model.game.entity.projectile.Projectile::getVisualLaneOffset)
                .distinct()
                .count() > 1);
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(projectile -> projectile.getVx() >= PlantShotPatterns.PLANT_FOOD_PEA_SPEED - 0.01));
    }

    @Test
    void rotobagaPlantFoodEndsAfterOneClip() {
        session.tryPlant("Rotobaga", 4, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(4, 2));
        Plant plant = session.getBoard().getPlantAt(4, 2);
        assertTrue(plant.isUsingPlantFood());
        for (int i = 0; i < PlantShotPatterns.ROTOBAGA_PLANT_FOOD_TICKS; i++) {
            session.tick();
        }
        assertFalse(plant.isUsingPlantFood());
    }

    @Test
    void repeaterPlantFoodSpawnsGiantPea() {
        session.tryPlant("Repeater", 1, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(1, 2));
        for (int i = 0; i < 60 + PlantShotPatterns.GIANT_PEA_FIRE_DELAY_TICKS - 1; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream().noneMatch(projectile ->
                projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.GIANT_PEA));
        session.tick();
        assertTrue(session.getProjectileSystem().getProjectiles().stream().anyMatch(projectile ->
                projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.GIANT_PEA
                        && projectile.getVisualScale() >= PlantShotPatterns.GIANT_PEA_SCALE));
    }

    @Test
    void sunShroomPlantFoodGrowsToMax() {
        session.tryPlant("Sun-shroom", 1, 1, 1);
        Plant shroom = session.getBoard().getPlantAt(1, 1);
        assertEquals(0, shroom.getGrowthStage());
        session.setPlantFoodCount(5);
        int before = session.getSunItems().size();
        assertTrue(session.usePlantFood(1, 1));
        for (int i = 0; i < 8; i++) {
            session.tick();
        }
        assertEquals(shroom.maxGrowthStage(), shroom.getGrowthStage());
        assertTrue(session.getSunItems().size() > before);
    }

    @Test
    void twinSunflowerProducesSpecialSun() {
        session.tryPlant("Twin Sunflower", 2, 2, 1);
        Plant twin = session.getBoard().getPlantAt(2, 2);
        context.spawnSun(twin, 100, io.github.finalwave.model.item.SunType.SPECIAL);
        assertEquals(io.github.finalwave.model.item.SunType.SPECIAL, session.getSunItems().getLast().getType());
        assertEquals(100, session.getSunItems().getLast().getValue());
    }

    @Test
    void snowPeaPlantFoodFreezesRow() {
        session.tryPlant("Snow Pea", 2, 2, 1);
        placeMovingZombie(6, 2);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        session.tick();
        Zombie zombie = session.getZombies().getFirst();
        assertTrue(zombie.getFreezeTicksRemaining() > 0);
    }

    @Test
    void splitPeaPlantFoodFiresBothDirections() {
        session.tryPlant("Split Pea", 5, 2, 1);
        placeMovingZombie(2, 2);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(5, 2));
        for (int i = 0; i < 10; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(io.github.finalwave.model.game.entity.projectile.Projectile::isReverse));
    }

    @Test
    void citronPlantFoodSpawnsLaneClearOrb() {
        session.tryPlant("Citron", 1, 2, 1);
        placeMovingZombie(7, 2);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(1, 2));
        for (int i = 0; i < 14; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream().anyMatch(projectile ->
                projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.PLASMA_PF));
    }

    @Test
    void caulipowerPlantFoodHypnotizesTargets() {
        placeMovingZombie(3, 1);
        placeMovingZombie(4, 2);
        placeMovingZombie(5, 3);
        session.tryPlant("Caulipower", 1, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(1, 2));
        for (int i = 0; i < 8; i++) {
            session.tick();
        }
        long hypnotized = session.getZombies().stream()
                .filter(Zombie::isHypnotized)
                .count();
        assertTrue(hypnotized >= 1);
    }

    @Test
    void bowlingBulbFiresColoredProjectileAndAdvancesAmmo() {
        session.tryPlant("Bowling Bulb", 1, 2, 1);
        Plant bulb = session.getBoard().getPlantAt(1, 2);
        placeMovingZombie(6, 2);
        for (int i = 0; i < 30; i++) {
            session.tick();
            if (!session.getProjectileSystem().getProjectiles().isEmpty()) {
                break;
            }
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream().anyMatch(projectile ->
                projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.BOWLING_CYAN));
        assertEquals(2, bulb.getBowlingAmmo());
    }

    @Test
    void citronEntersRecoveryAfterShot() {
        session.tryPlant("Citron", 1, 2, 1);
        Plant citron = session.getBoard().getPlantAt(1, 2);
        citron.setChargeTicksRemaining(0);
        placeMovingZombie(6, 2);
        for (int i = 0; i < 100; i++) {
            session.tick();
            if (citron.getRecoveryTicksRemaining() > 0) {
                break;
            }
        }
        assertTrue(citron.getRecoveryTicksRemaining() > 0);
    }

    @Test
    void cactusPlantFoodFiresSingleLaneSpike() {
        session.tryPlant("Cactus", 1, 2, 1);
        placeMovingZombie(6, 2);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(1, 2));
        for (int i = 0; i < 12; i++) {
            session.tick();
        }
        long pfSpikes = session.getProjectileSystem().getProjectiles().stream()
                .filter(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.SPIKE_PF)
                .count();
        assertEquals(1, pfSpikes);
    }

    @Test
    void splitPeaBackwardShotUsesAttack3Visual() {
        session.tryPlant("Split Pea", 5, 2, 1);
        Plant plant = session.getBoard().getPlantAt(5, 2);
        plant.setSplitFireVisual(Plant.SplitFireVisual.BACKWARD);
        plant.setAttacking(true);
        assertArrayEquals(new String[]{"attack3", "idle"},
                PlantVisualState.preferredClips(plant, false, false, "idle", List.of()));
    }

    @Test
    void splitPeaPlantFoodFiresForwardAndReverse() {
        session.tryPlant("Split Pea", 5, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(5, 2));
        for (int i = 0; i < 6; i++) {
            session.tick();
        }
        var projectiles = session.getProjectileSystem().getProjectiles();
        assertFalse(projectiles.isEmpty());
        assertTrue(projectiles.stream().anyMatch(p -> !p.isReverse()));
        assertTrue(projectiles.stream().anyMatch(io.github.finalwave.model.game.entity.projectile.Projectile::isReverse));
    }

    @Test
    void splitPeaBothDirectionsUsesAttack2Visual() {
        session.tryPlant("Split Pea", 5, 2, 1);
        Plant plant = session.getBoard().getPlantAt(5, 2);
        plant.setSplitFireVisual(Plant.SplitFireVisual.BOTH);
        plant.setAttacking(true);
        assertArrayEquals(new String[]{"attack2", "idle"},
                PlantVisualState.preferredClips(plant, false, false, "idle", List.of()));
    }

    @Test
    void starfruitFiresFiveDirectedProjectiles() {
        session.tryPlant("Starfruit", 3, 2, 1);
        placeMovingZombie(5, 2);
        for (int i = 0; i < 30; i++) {
            session.tick();
            if (!session.getProjectileSystem().getProjectiles().isEmpty()) {
                break;
            }
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.STAR));
    }

    @Test
    void megaGatlingPlantFoodBoostsVolleySize() {
        session.tryPlant("Mega Gatling Pea", 2, 2, 1);
        Plant plant = session.getBoard().getPlantAt(2, 2);
        placeMovingZombie(6, 2);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        for (int i = 0; i < 25; i++) {
            session.tick();
        }
        assertTrue(plant.isMegaGatlingBoosted());
    }

    @Test
    void megaGatlingFiresStaggeredVolley() {
        session.tryPlant("Mega Gatling Pea", 2, 2, 1);
        placeMovingZombie(6, 2);
        for (int i = 0; i < 40; i++) {
            session.tick();
            if (session.getProjectileSystem().getProjectiles().size() >= 2) {
                break;
            }
        }
        long count = session.getProjectileSystem().getProjectiles().stream()
                .filter(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.MEGA_GATLING_PEA)
                .count();
        assertTrue(count >= 1);
        for (int i = 0; i < 12; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .filter(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.MEGA_GATLING_PEA)
                .count() >= 2);
    }

    @Test
    void gooPeashooterFiresPoisonProjectile() {
        session.tryPlant("Goo Peashooter", 2, 2, 1);
        placeMovingZombie(6, 2);
        for (int i = 0; i < 30; i++) {
            session.tick();
            if (!session.getProjectileSystem().getProjectiles().isEmpty()) {
                break;
            }
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.GOO));
    }

    @Test
    void megaGatlingUsesCustomProjectile() {
        session.tryPlant("Mega Gatling Pea", 2, 2, 1);
        placeMovingZombie(6, 2);
        for (int i = 0; i < 30; i++) {
            session.tick();
            if (!session.getProjectileSystem().getProjectiles().isEmpty()) {
                break;
            }
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.MEGA_GATLING_PEA));
    }

    @Test
    void seaShroomUsesSeaProjectile() {
        session.enableSandboxPractice();
        session.tryPlant("Sea-shroom", 2, 2, 1);
        placeMovingZombie(5, 2);
        for (int i = 0; i < 30; i++) {
            session.tick();
            if (!session.getProjectileSystem().getProjectiles().isEmpty()) {
                break;
            }
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.SEA_SHROOM));
    }

    @Test
    void gooPeashooterPlantFoodSpawnsPoisonLaneBall() {
        session.tryPlant("Goo Peashooter", 1, 2, 1);
        placeMovingZombie(7, 2);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(1, 2));
        for (int i = 0; i < 11; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.GOO_PF));
    }

    @Test
    void puffShroomPlantFoodFiresPoweredSpores() {
        session.tryPlant("Puff-shroom", 2, 2, 1);
        placeMovingZombie(5, 2);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        for (int i = 0; i < 20; i++) {
            session.tick();
        }
        assertFalse(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void seaShroomPlantFoodResetsLifespan() {
        session.enableSandboxPractice();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Sea-shroom", 2, 2, 1));
        Plant shroom = session.getBoard().getPlantAt(2, 2);
        int before = shroom.getLifespanTicksRemaining();
        for (int i = 0; i < 50; i++) {
            session.tick();
        }
        assertTrue(shroom.getLifespanTicksRemaining() < before);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        assertTrue(shroom.getLifespanTicksRemaining() > before / 2);
    }

    @Test
    void caulipowerIgnoresHypnotizedLaneTarget() {
        session.tryPlant("Caulipower", 1, 2, 1);
        Plant plant = session.getBoard().getPlantAt(1, 2);
        plant.setChargeTicksRemaining(0);
        Zombie hypnotized = new Zombie.Builder("dummy")
                .maxHealth(200)
                .speed(0)
                .position(4, 2)
                .build();
        hypnotized.setState(ZombieState.MOVING);
        hypnotized.setHypnotized(true);
        session.addZombie(hypnotized);
        for (int i = 0; i < 200; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void sunflowerPlantFoodDropsOneFiftySunInThreePieces() {
        session.tryPlant("Sunflower", 2, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        tickUntilSunBurst(10);
        assertEquals(150, totalSunValue());
        assertEquals(3, session.getSunItems().size());
        assertTrue(session.getSunItems().stream().allMatch(sun -> sun.getType() == SunType.NORMAL));
        assertTrue(session.getSunItems().stream().allMatch(sun -> sun.getValue() == 50));
    }

    @Test
    void twinSunflowerPlantFoodDropsTwoFiftySun() {
        session.tryPlant("Twin Sunflower", 2, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        tickUntilSunBurst(12);
        assertEquals(250, totalSunValue());
        assertEquals(3, session.getSunItems().size());
        long special = session.getSunItems().stream().filter(sun -> sun.getType() == SunType.SPECIAL).count();
        assertEquals(2, special);
        assertEquals(50, session.getSunItems().stream()
                .filter(sun -> sun.getType() == SunType.NORMAL)
                .mapToInt(Sun::getValue)
                .sum());
    }

    @Test
    void primalSunflowerPlantFoodDropsThreeLargeSuns() {
        session.tryPlant("Primal Sunflower", 2, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        tickUntilSunBurst(14);
        assertEquals(225, totalSunValue());
        assertEquals(3, session.getSunItems().size());
        assertTrue(session.getSunItems().stream().allMatch(sun -> sun.getType() == SunType.SPECIAL));
        assertTrue(session.getSunItems().stream().allMatch(sun -> sun.getValue() == 75));
    }

    @Test
    void sunShroomPlantFoodDropsTwoTwentyFiveAsLargeSuns() {
        session.tryPlant("Sun-shroom", 1, 1, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(1, 1));
        tickUntilSunBurst(30);
        assertEquals(225, totalSunValue());
        assertEquals(3, session.getSunItems().size());
        assertTrue(session.getSunItems().stream().allMatch(sun -> sun.getType() == SunType.SPECIAL));
        assertTrue(session.getSunItems().stream().allMatch(sun -> sun.getValue() == 75));
    }

    private void tickUntilSunBurst(int ticks) {
        for (int i = 0; i < ticks; i++) {
            session.tick();
        }
    }

    private int totalSunValue() {
        return session.getSunItems().stream().mapToInt(Sun::getValue).sum();
    }

    private void placeMovingZombie(double x, int row) {
        Zombie zombie = new Zombie.Builder("dummy")
                .maxHealth(200)
                .speed(0)
                .position(x, row)
                .build();
        zombie.setState(ZombieState.MOVING);
        session.addZombie(zombie);
    }
}
