package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.game.board.BoardGameContext;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.board.tile.IceTile;
import io.github.finalwave.model.game.board.tile.LowBeachTile;
import io.github.finalwave.model.game.board.tile.NormalTile;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantTag;
import io.github.finalwave.model.game.entity.plant.food.CitronPlantFoodEffect;
import io.github.finalwave.model.game.entity.plant.food.CabbagePultPlantFood;
import io.github.finalwave.model.game.entity.plant.food.FumeShroomPlantFood;
import io.github.finalwave.model.game.entity.plant.food.KernelPultPlantFood;
import io.github.finalwave.model.game.entity.plant.food.MelonPultPlantFood;
import io.github.finalwave.model.game.entity.plant.food.PeaPodPlantFood;
import io.github.finalwave.model.game.entity.plant.food.PepperPultPlantFood;
import io.github.finalwave.model.game.entity.plant.food.PotatoMinePlantFood;
import io.github.finalwave.model.game.entity.plant.food.WallNutPlantFood;
import io.github.finalwave.model.game.entity.plant.food.EndurianPlantFood;
import io.github.finalwave.model.game.entity.plant.ability.ExplosiveAbility;
import io.github.finalwave.model.game.entity.plant.ability.GrapeshotAbility;
import io.github.finalwave.model.game.entity.plant.ability.JalapenoAbility;
import io.github.finalwave.model.game.entity.plant.ability.SquashAbility;
import io.github.finalwave.model.game.entity.plant.ability.TangleKelpAbility;
import io.github.finalwave.model.game.entity.plant.ability.TangleKelpGrabMark;
import io.github.finalwave.model.game.entity.projectile.DoomShroomMuzzles;
import io.github.finalwave.model.game.entity.projectile.GrapeshotMuzzles;
import io.github.finalwave.model.game.entity.projectile.JalapenoMuzzles;
import io.github.finalwave.model.game.entity.projectile.CabbageMuzzles;
import io.github.finalwave.model.game.entity.projectile.FumeMuzzles;
import io.github.finalwave.model.game.entity.projectile.KernelMuzzles;
import io.github.finalwave.model.game.entity.projectile.MelonMuzzles;
import io.github.finalwave.model.game.entity.projectile.PeaPodMuzzles;
import io.github.finalwave.model.game.entity.projectile.PepperMuzzles;
import io.github.finalwave.model.game.entity.projectile.BonkChoyMuzzles;
import io.github.finalwave.model.game.entity.projectile.WasabiWhipMuzzles;
import io.github.finalwave.model.game.entity.projectile.ChomperMuzzles;
import io.github.finalwave.model.game.entity.projectile.PhatBeetMuzzles;
import io.github.finalwave.model.game.entity.projectile.KiwibeastMuzzles;
import io.github.finalwave.model.game.entity.projectile.EndurianMuzzles;
import io.github.finalwave.model.game.entity.projectile.IcebergLettuceMuzzles;
import io.github.finalwave.model.game.entity.projectile.TangleKelpMuzzles;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.model.game.entity.zombie.behavior.MovementBehavior;
import io.github.finalwave.model.game.entity.zombie.behavior.ContactAttackBehavior;
import io.github.finalwave.model.game.entity.zombie.behavior.TransformBehavior;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.view.gui.render.sync.PlantVisualState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    void peashooterShootsGraveAheadWithNoZombie() {
        session.getBoard().setTile(5, 2, new io.github.finalwave.model.game.board.tile.GraveTile());
        session.tryPlant("Peashooter", 1, 2, 1);
        for (int i = 0; i < 18; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
        session.tick();
        assertFalse(session.getProjectileSystem().getProjectiles().isEmpty());
        int graveHp = ((io.github.finalwave.model.game.board.tile.GraveTile)
                session.getBoard().getTile(5, 2)).getHealth();
        for (int i = 0; i < 80; i++) {
            session.tick();
        }
        assertTrue(((io.github.finalwave.model.game.board.tile.GraveTile)
                session.getBoard().getTile(5, 2)).getHealth() < graveHp
                || !session.getBoard().getTile(5, 2).isGrave());
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
    void peaPodThreeHeadClustersAfterFirst() {
        Plant pod = plantStackedPeaPod(1, 2, 3);
        placeMovingZombie(7, 2);
        for (int i = 0; i < ticksUntilPeaPodFirstShot(pod); i++) {
            session.tick();
        }
        assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        assertTrue(pod.isAttacking());
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        session.tick();
        assertEquals(3, session.getProjectileSystem().getProjectiles().size());
        assertFalse(pod.isAttacking());
    }

    @Test
    void peaPodFiveHeadDelaysFifthAfterCluster() {
        Plant pod = plantStackedPeaPod(1, 2, 5);
        placeMovingZombie(7, 2);
        for (int i = 0; i < ticksUntilPeaPodFirstShot(pod); i++) {
            session.tick();
        }
        assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        assertTrue(pod.isAttacking());
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        session.tick();
        assertEquals(4, session.getProjectileSystem().getProjectiles().size());
        assertTrue(pod.isAttacking());
        session.tick();
        assertEquals(4, session.getProjectileSystem().getProjectiles().size());
        session.tick();
        assertEquals(5, session.getProjectileSystem().getProjectiles().size());
        assertFalse(pod.isAttacking());
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
    void hotPotatoStaysAliveForAttackThenMeltsIce() {
        session.getBoard().setTile(3, 2, new io.github.finalwave.model.game.board.tile.IceTile());
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Hot Potato", 3, 2, 1));
        Plant potato = session.getBoard().getPlantAt(3, 2);
        assertNotNull(potato);
        assertTrue(potato.isAlive());
        assertTrue(potato.isAttacking());
        assertTrue(session.getBoard().getTile(3, 2).isIce());
        for (int i = 0; i < 4; i++) {
            session.tick();
            assertTrue(potato.isAlive());
        }
        session.tick();
        assertFalse(potato.isAlive());
        assertFalse(session.getBoard().getTile(3, 2).isIce());
    }

    @Test
    void graveBusterPlaysAttackThenEatsGrave() {
        session.getBoard().setTile(2, 2, new io.github.finalwave.model.game.board.tile.GraveTile());
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Grave Buster", 2, 2, 1));
        Plant buster = session.getBoard().getPlantAt(2, 2);
        assertNotNull(buster);
        assertTrue(buster.isAlive());
        assertTrue(buster.isAttacking());
        assertFalse(buster.isGraveBusting());
        assertTrue(session.getBoard().getTile(2, 2).isGrave());
        for (int i = 0; i < 10; i++) {
            session.tick();
            assertTrue(buster.isAlive());
        }
        assertFalse(buster.isAttacking());
        assertTrue(buster.isGraveBusting());
        for (int i = 0; i < 39; i++) {
            session.tick();
            assertTrue(buster.isAlive());
        }
        session.tick();
        assertFalse(buster.isAlive());
        assertFalse(session.getBoard().getTile(2, 2).isGrave());
    }

    @Test
    void graveBusterDropsPlantFoodFromGrave() {
        session.getBoard().setTile(2, 2, new io.github.finalwave.model.game.board.tile.GraveTile(
                io.github.finalwave.model.game.board.tile.GraveTile.Loot.PLANT_FOOD));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Grave Buster", 2, 2, 1));
        Plant buster = session.getBoard().getPlantAt(2, 2);
        for (int i = 0; i < 50; i++) {
            session.tick();
        }
        assertFalse(buster.isAlive());
        assertEquals(0, session.getPlantFoodCount());
        assertEquals(1, session.getPlantFoodDrops().size());
        assertEquals(2, session.getPlantFoodDrops().get(0).getCol());
        assertEquals(2, session.getPlantFoodDrops().get(0).getRow());
    }

    @Test
    void graveBusterDropsSunFromGrave() {
        session.getBoard().setTile(2, 2, new io.github.finalwave.model.game.board.tile.GraveTile(
                io.github.finalwave.model.game.board.tile.GraveTile.Loot.SUN_50));
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Grave Buster", 2, 2, 1));
        Plant buster = session.getBoard().getPlantAt(2, 2);
        for (int i = 0; i < 50; i++) {
            session.tick();
        }
        assertFalse(buster.isAlive());
        assertEquals(1, session.getSunItems().size());
        assertEquals(50, session.getSunItems().get(0).getValue());
        assertEquals(2, session.getSunItems().get(0).getCol());
        assertEquals(2, session.getSunItems().get(0).getRow());
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
        for (int i = 0; i < CitronPlantFoodEffect.FIRE_TICK; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().stream().anyMatch(projectile ->
                projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.PLASMA_PF));
        assertEquals(1, session.getProjectileSystem().getProjectiles().stream()
                .filter(projectile -> projectile.getEffect()
                        == io.github.finalwave.model.game.entity.projectile.ProjectileEffect.PLASMA_PF)
                .count());
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
        citron.primeActionCooldown();
        placeMovingZombie(6, 2);
        for (int i = 0; i < 80; i++) {
            session.tick();
            if (citron.getRecoveryTicksRemaining() > 0) {
                break;
            }
        }
        assertTrue(citron.getRecoveryTicksRemaining() > 0);
    }

    @Test
    void citronWaitsChargedWithoutTargetThenFires() {
        session.tryPlant("Citron", 1, 2, 1);
        Plant citron = session.getBoard().getPlantAt(1, 2);
        int guard = 0;
        while (citron.getChargeTicksRemaining() > 0 && guard++ < 500) {
            session.tick();
        }
        assertEquals(0, citron.getChargeTicksRemaining());
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
        placeMovingZombie(6, 2);
        for (int i = 0; i < CitronAbility.FIRE_AT_TICK + 2; i++) {
            session.tick();
        }
        assertFalse(session.getProjectileSystem().getProjectiles().isEmpty());
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

    @Test
    void garlicDivertOnBite() {
        session.tryPlant("Garlic", 3, 2, 1);
        Zombie zombie = chewingZombie(3.05, 2);
        session.addZombie(zombie);
        for (int i = 0; i < 5; i++) {
            session.tick();
        }
        assertEquals(2, zombie.getRow());
        for (int i = 0; i < 25; i++) {
            session.tick();
        }
        assertNotEquals(2, zombie.getRow());
    }

    @Test
    void garlicPlantFoodHeals() {
        session.tryPlant("Garlic", 3, 2, 1);
        Plant garlic = session.getBoard().getPlantAt(3, 2);
        garlic.takeDamage(120);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(3, 2));
        assertEquals(garlic.getMaxHealth(), garlic.getHealth());
    }

    @Test
    void sweetPotatoPullsAdjacentZombie() {
        session.tryPlant("Sweet Potato", 3, 2, 1);
        Zombie zombie = approachingZombie(3.5, 1);
        session.addZombie(zombie);
        for (int i = 0; i < 20; i++) {
            session.tick();
        }
        assertEquals(2, zombie.getRow());
    }

    @Test
    void sunBeanInfectsZombieAndDropsSunOnDamage() {
        session.tryPlant("Sun Bean", 3, 2, 1);
        Zombie zombie = chewingZombie(3.05, 2);
        session.addZombie(zombie);
        for (int i = 0; i < 200; i++) {
            session.tick();
        }
        assertTrue(zombie.getSunBeanInfections() > 0);
        int before = session.getSunItems().size();
        zombie.takeDamage(50);
        assertTrue(session.getSunItems().size() > before);
    }

    @Test
    void magnetShroomStripsBucketArmor() {
        session.tryPlant("Magnet-shroom", 2, 2, 1);
        io.github.finalwave.model.game.entity.zombie.Armor bucket = new io.github.finalwave.model.game.entity.zombie.Armor(
                "BucketDefault", "Bucket", 1100, true, false);
        Zombie zombie = new Zombie.Builder("bucket")
                .maxHealth(200)
                .speed(0)
                .armor(bucket)
                .position(2.5, 2)
                .build();
        session.addZombie(zombie);
        assertTrue(zombie.hasArmor());
        for (int i = 0; i < 120; i++) {
            session.tick();
        }
        assertFalse(zombie.hasArmor());
    }

    @Test
    void explodeONutDamagesNearbyZombiesOnDeath() {
        session.tryPlant("Explode-o-nut", 3, 2, 1);
        Zombie zombie = eatingZombie(3.3, 2);
        session.addZombie(zombie);
        Plant nut = session.getBoard().getPlantAt(3, 2);
        int hpBefore = zombie.getHealth();
        nut.takeDamage(nut.getMaxHealth());
        assertTrue(zombie.getHealth() < hpBefore);
    }

    @Test
    void torchwoodPlantFoodSetsPermanentBoost() {
        session.tryPlant("Torchwood", 2, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        for (int i = 0; i < 10; i++) {
            session.tick();
        }
        assertTrue(session.getBoard().getPlantAt(2, 2).isTorchwoodBoosted());
    }

    @Test
    void hypnoShroomHypnotizesBiter() {
        session.tryPlant("Hypno-shroom", 3, 2, 1);
        Zombie zombie = chewingZombie(3.05, 2);
        session.addZombie(zombie);
        session.tick();
        assertTrue(zombie.isHypnotized());
    }

    @Test
    void hypnoShroomPlantFoodUpgradesTarget() {
        placeMovingZombie(5, 2);
        session.tryPlant("Hypno-shroom", 2, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(2, 2));
        for (int i = 0; i < 8; i++) {
            session.tick();
        }
        assertTrue(session.getZombies().stream().anyMatch(Zombie::isHypnotized));
    }

    @Test
    void imitaterMorphsToLastSelectedSeed() {
        session.setSelectedLoadout(Set.of("Magnet-shroom", "Hypno-shroom", "Imitater", "Wall-nut"));
        session.setSelectedLoadoutOrder(List.of("Wall-nut", "Hypno-shroom", "Imitater", "Magnet-shroom"));
        session.noteImitaterTargetSeed("Magnet-shroom");
        session.tryPlant("Imitater", 2, 2, 1);
        for (int i = 0; i < 15; i++) {
            session.tick();
        }
        Plant plant = session.getBoard().getPlantAt(2, 2);
        assertEquals("Magnet-shroom", plant.getName());
    }

    @Test
    void imitaterMorphsToLastPlantedSeed() {
        session.setSelectedLoadout(Set.of("Magnet-shroom", "Hypno-shroom", "Imitater"));
        session.setSelectedLoadoutOrder(List.of("Hypno-shroom", "Cat-tail", "Imitater"));
        session.tryPlant("Magnet-shroom", 1, 2, 1);
        session.tryPlant("Imitater", 2, 2, 1);
        for (int i = 0; i < 15; i++) {
            session.tick();
        }
        Plant plant = session.getBoard().getPlantAt(2, 2);
        assertEquals("Magnet-shroom", plant.getName());
    }

    @Test
    void imitaterSkipsCatTailAndMorphsToPreviousPlant() {
        session.setSelectedLoadout(Set.of("Hypno-shroom", "Cat-tail", "Imitater"));
        session.setSelectedLoadoutOrder(List.of("Hypno-shroom", "Cat-tail", "Imitater"));
        session.tryPlant("Imitater", 2, 2, 1);
        for (int i = 0; i < 15; i++) {
            session.tick();
        }
        Plant plant = session.getBoard().getPlantAt(2, 2);
        assertEquals("Hypno-shroom", plant.getName());
    }

    @Test
    void imitaterMorphsToLoadoutTarget() {
        session.setSelectedLoadout(Set.of("Imitater", "Wall-nut"));
        session.setSelectedLoadoutOrder(List.of("Wall-nut", "Imitater"));
        session.tryPlant("Imitater", 2, 2, 1);
        for (int i = 0; i < 15; i++) {
            session.tick();
        }
        Plant plant = session.getBoard().getPlantAt(2, 2);
        assertEquals("Wall-nut", plant.getName());
        assertTrue(plant.isImitaterCopy());
        for (int i = 0; i < 30; i++) {
            session.tick();
        }
        assertTrue(plant.isImitaterCopy());
    }

    @Test
    void iceShroomSurvivesAndChillsNearbyZombies() {
        Zombie near = approachingZombie(3.2, 2);
        session.addZombie(near);
        session.tryPlant("Ice-shroom", 3, 2, 1);
        Plant ice = session.getBoard().getPlantAt(3, 2);
        assertNotNull(ice);
        assertTrue(ice.isAlive());
        for (int i = 0; i < 5; i++) {
            session.tick();
        }
        assertTrue(near.getCurrentSpeed() < near.getBaseSpeed());
        assertTrue(near.getCurrentSpeed() > 0.0);
    }

    @Test
    void lilyPadPlantFoodSpawnsNeighborsOnWater() {
        session.getBoard().setTile(4, 2, new io.github.finalwave.model.game.board.tile.LowBeachTile());
        session.getBoard().setTile(5, 2, new io.github.finalwave.model.game.board.tile.LowBeachTile());
        session.getBoard().setTile(4, 1, new io.github.finalwave.model.game.board.tile.LowBeachTile());
        session.tryPlant("Lily Pad", 4, 2, 1);
        session.setPlantFoodCount(5);
        assertTrue(session.usePlantFood(4, 2));
        for (int i = 0; i < 6; i++) {
            session.tick();
        }
        long lilyPads = session.getBoard().getAllPlants().stream()
                .filter(plant -> "Lily Pad".equals(plant.getName()))
                .count();
        assertTrue(lilyPads >= 2);
    }

    @Test
    void peaPodPlantFoodFiresFiveGiantPeasDuringLoop() {
        Plant pod = plantStackedPeaPod(1, 2, 1);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(pod.isPlantFooding());
        assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        for (int i = 0; i < PeaPodPlantFood.ON_TICKS - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        assertEquals(PeaPodPlantFood.Phase.LOOP, pod.plantFoodPhase());
        for (int i = 0; i < PeaPodPlantFood.GIANT_WINDUP_TICKS - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertTrue(first.isGiantPea());
        assertEquals(100, first.getDamage());
        assertEquals(pod.getRow(), first.getRow());
        assertEquals(pod.getCol() + 0.5 + PeaPodMuzzles.giantX() + 0.3, first.getX(), 0.0001);
        assertEquals(PeaPodMuzzles.giantY(), first.getLaneYOffset(), 0.0001);
        for (int shot = 2; shot <= PeaPodPlantFood.GIANT_SHOTS; shot++) {
            for (int i = 0; i < PeaPodPlantFood.GIANT_STAGGER_TICKS - 1; i++) {
                session.tick();
                assertEquals(shot - 1, session.getProjectileSystem().getProjectiles().size());
            }
            session.tick();
            assertEquals(shot, session.getProjectileSystem().getProjectiles().size());
        }
        for (Projectile shot : session.getProjectileSystem().getProjectiles()) {
            assertTrue(shot.isGiantPea());
            assertEquals(100, shot.getDamage());
        }
    }

    @Test
    void peashooterPlantFoodStillBurstsNormalPeas() {
        session.tryPlant("Peashooter", 1, 2, 1);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        List<Projectile> shots = session.getProjectileSystem().getProjectiles();
        assertEquals(3, shots.size());
        for (Projectile shot : shots) {
            assertFalse(shot.isGiantPea());
            assertEquals(20, shot.getDamage());
        }
    }

    @Test
    void fumeShroomFiresOneStationaryBubble() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Fume-shroom", 1, 2, 1));
        Plant fume = session.getBoard().getPlantAt(1, 2);
        assertNotNull(fume);
        placeMovingZombie(5, 2);
        for (int i = 0; i < ticksUntilFumeFirstShot(fume); i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertFalse(first.isFumePlantFood());
        assertEquals(ProjectileEffect.FUME, first.getEffect());
        assertTrue(first.getProfile().piercing());
        assertEquals(FumeMuzzles.ATTACK_CLOUD_TICKS - 1, first.getLifetimeTicks());
        double origin = first.getX();
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile still = session.getProjectileSystem().getProjectiles().getFirst();
        assertEquals(origin, still.getX(), 0.0001);
        assertEquals(FumeMuzzles.ATTACK_CLOUD_TICKS - 2, still.getLifetimeTicks());
    }

    @Test
    void fumeShroomDoesNotAttackZombieOutsideBubbleRange() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Fume-shroom", 1, 2, 1));
        placeMovingZombie(7, 2);
        for (int i = 0; i < 40; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void fumePlantFoodFiresWithoutZombiesInRange() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Fume-shroom", 1, 2, 1));
        Plant fume = session.getBoard().getPlantAt(1, 2);
        assertNotNull(fume);
        placeMovingZombie(7, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(fume.isPlantFooding());
        for (int i = 0; i < FumeShroomPlantFood.WINDUP_TICKS - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        assertTrue(session.getProjectileSystem().getProjectiles().getFirst().isFumePlantFood());
    }

    @Test
    void fumePlantFoodDoesNotKnockBackLaneZombie() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Fume-shroom", 1, 2, 1));
        Plant fume = session.getBoard().getPlantAt(1, 2);
        assertNotNull(fume);
        Zombie zombie = new Zombie.Builder("dummy")
                .maxHealth(200)
                .speed(0)
                .position(5, 2)
                .build();
        zombie.setState(ZombieState.MOVING);
        session.addZombie(zombie);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(fume.isPlantFooding());
        assertEquals(PeaPodPlantFood.Phase.LOOP, fume.plantFoodPhase());
        assertEquals(5.0, zombie.getX(), 0.0001);
    }

    @Test
    void fumePlantFoodFiresOneLongerBubbleDuringLoop() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Fume-shroom", 1, 2, 1));
        Plant fume = session.getBoard().getPlantAt(1, 2);
        assertNotNull(fume);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(fume.isPlantFooding());
        assertEquals(PeaPodPlantFood.Phase.LOOP, fume.plantFoodPhase());
        assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        for (int i = 0; i < FumeShroomPlantFood.WINDUP_TICKS - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertFalse(first.isGiantPea());
        assertTrue(first.isFumePlantFood());
        assertEquals(ProjectileEffect.FUME, first.getEffect());
        assertTrue(first.getProfile().piercing());
        assertEquals(20, first.getDamage());
        assertEquals(fume.getRow(), first.getRow());
        assertEquals(FumeMuzzles.plantFoodY(), first.getLaneYOffset(), 0.0001);
        assertEquals(fume.getCol() + 0.5 + FumeMuzzles.plantFoodX(), first.getX(), 0.0001);
        assertEquals(FumeMuzzles.PLANTFOOD_CLOUD_TICKS - 1, first.getLifetimeTicks());
        for (int i = 0; i < 5; i++) {
            session.tick();
            assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        }
        Projectile still = session.getProjectileSystem().getProjectiles().getFirst();
        assertTrue(still.isFumePlantFood());
        assertEquals(first.getX(), still.getX(), 0.0001);
    }

    @Test
    void cabbagePlantFoodFiresOneArcingCabbageAfterWindup() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Cabbage-pult", 1, 2, 1));
        Plant cabbage = session.getBoard().getPlantAt(1, 2);
        assertNotNull(cabbage);
        placeMovingZombie(6, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(cabbage.isPlantFooding());
        assertEquals(PeaPodPlantFood.Phase.LOOP, cabbage.plantFoodPhase());
        assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        for (int i = 0; i < CabbagePultPlantFood.WINDUP_TICKS - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertTrue(first.isCabbagePlantFood());
        assertFalse(first.isGiantPea());
        assertEquals(ProjectileEffect.CABBAGE, first.getEffect());
        assertEquals(ProjectileProfile.Trajectory.ARCING, first.getProfile().trajectory());
        assertEquals(200, first.getDamage());
        assertEquals(cabbage.getRow(), first.getRow());
        assertEquals(CabbageMuzzles.y(), first.getLaneYOffset(), 0.0001);
        assertEquals(cabbage.getCol() + 0.5 + CabbageMuzzles.x() + 0.25, first.getX(), 0.0001);
    }

    @Test
    void cabbagePlantFoodFiresWithoutLaneTarget() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Cabbage-pult", 1, 2, 1));
        Plant cabbage = session.getBoard().getPlantAt(1, 2);
        assertNotNull(cabbage);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(cabbage.isPlantFooding());
        for (int i = 0; i < CabbagePultPlantFood.WINDUP_TICKS - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        assertTrue(session.getProjectileSystem().getProjectiles().getFirst().isCabbagePlantFood());
        for (int i = 0; i < 20; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void kernelPultFiresArcingKernelAfterWindup() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Kernel-pult", 1, 2, 1));
        Plant kernel = session.getBoard().getPlantAt(1, 2);
        assertNotNull(kernel);
        placeMovingZombie(6, 2);
        for (int i = 0; i < ticksUntilKernelFirstShot(kernel); i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertTrue(first.getEffect() == ProjectileEffect.KERNEL
                || first.getEffect() == ProjectileEffect.BUTTER);
        assertEquals(ProjectileProfile.Trajectory.ARCING, first.getProfile().trajectory());
        assertEquals(kernel.getRow(), first.getRow());
        assertEquals(KernelMuzzles.y(), first.getLaneYOffset(), 0.0001);
        assertEquals(kernel.getCol() + 0.5 + KernelMuzzles.x() + 0.25, first.getX(), 0.0001);
    }

    @Test
    void kernelPlantFoodFiresButterStormAfterWindup() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Kernel-pult", 1, 2, 1));
        Plant kernel = session.getBoard().getPlantAt(1, 2);
        assertNotNull(kernel);
        placeMovingZombie(6, 2);
        placeMovingZombie(5, 3);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(kernel.isPlantFooding());
        assertEquals(PeaPodPlantFood.Phase.LOOP, kernel.plantFoodPhase());
        assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        for (int i = 0; i < KernelPultPlantFood.WINDUP_TICKS - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(2, session.getProjectileSystem().getProjectiles().size());
        for (Projectile shot : session.getProjectileSystem().getProjectiles()) {
            assertFalse(shot.isCabbagePlantFood());
            assertFalse(shot.isGiantPea());
            assertEquals(ProjectileEffect.BUTTER, shot.getEffect());
            assertEquals(ProjectileProfile.Trajectory.ARCING, shot.getProfile().trajectory());
            assertEquals(200, shot.getDamage());
            assertEquals(KernelMuzzles.plantFoodY(), shot.getLaneYOffset(), 0.0001);
        }
    }

    @Test
    void kernelPlantFoodFiresNoButterWithoutTargets() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Kernel-pult", 1, 2, 1));
        Plant kernel = session.getBoard().getPlantAt(1, 2);
        assertNotNull(kernel);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(kernel.isPlantFooding());
        for (int i = 0; i < KernelPultPlantFood.WINDUP_TICKS - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(0, session.getProjectileSystem().getProjectiles().size());
    }

    @Test
    void melonPultFiresArcingMelonAfterWindup() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Melon-pult", 1, 2, 1));
        Plant melon = session.getBoard().getPlantAt(1, 2);
        assertNotNull(melon);
        placeMovingZombie(6, 2);
        for (int i = 0; i < ticksUntilMelonFirstShot(melon); i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertFalse(first.isMelonPlantFood());
        assertEquals(ProjectileEffect.MELON, first.getEffect());
        assertEquals(ProjectileProfile.Trajectory.ARCING, first.getProfile().trajectory());
        assertEquals(melon.getRow(), first.getRow());
        assertEquals(MelonMuzzles.y(), first.getLaneYOffset(), 0.0001);
        assertEquals(melon.getCol() + 0.5 + MelonMuzzles.x() + 0.25, first.getX(), 0.0001);
    }

    @Test
    void melonPlantFoodFiresFourArcingMelonsOnSchedule() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Melon-pult", 1, 2, 1));
        Plant melon = session.getBoard().getPlantAt(1, 2);
        assertNotNull(melon);
        placeMovingZombie(6, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(melon.isPlantFooding());
        assertEquals(PeaPodPlantFood.Phase.LOOP, melon.plantFoodPhase());
        int[] shots = MelonPultPlantFood.SHOT_TICKS;
        int elapsed = 0;
        int expected = 0;
        for (int shotIndex = 0; shotIndex < shots.length; shotIndex++) {
            while (elapsed < shots[shotIndex] - 1) {
                session.tick();
                elapsed++;
                assertEquals(expected, session.getProjectileSystem().getProjectiles().size());
            }
            session.tick();
            elapsed++;
            expected++;
            assertEquals(expected, session.getProjectileSystem().getProjectiles().size());
            Projectile latest = session.getProjectileSystem().getProjectiles().getLast();
            assertTrue(latest.isMelonPlantFood());
            assertEquals(ProjectileEffect.MELON, latest.getEffect());
            assertEquals(ProjectileProfile.Trajectory.ARCING, latest.getProfile().trajectory());
            assertEquals(400, latest.getDamage());
            assertEquals(melon.getRow(), latest.getRow());
            assertEquals(MelonMuzzles.plantFoodY(), latest.getLaneYOffset(), 0.0001);
        }
    }

    @Test
    void melonPlantFoodFiresWithoutLaneTarget() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Melon-pult", 1, 2, 1));
        Plant melon = session.getBoard().getPlantAt(1, 2);
        assertNotNull(melon);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(melon.isPlantFooding());
        for (int i = 0; i < MelonPultPlantFood.SHOT_TICKS[0] - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        assertTrue(session.getProjectileSystem().getProjectiles().getFirst().isMelonPlantFood());
        for (int i = 0; i < 40; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void winterMelonFiresArcingMelonAfterWindup() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Winter Melon", 1, 2, 1));
        Plant winter = session.getBoard().getPlantAt(1, 2);
        assertNotNull(winter);
        placeMovingZombie(6, 2);
        for (int i = 0; i < ticksUntilMelonFirstShot(winter); i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertFalse(first.isMelonPlantFood());
        assertEquals(ProjectileEffect.WINTER_MELON, first.getEffect());
        assertEquals(ProjectileProfile.Trajectory.ARCING, first.getProfile().trajectory());
        assertEquals(winter.getRow(), first.getRow());
        assertEquals(MelonMuzzles.y(), first.getLaneYOffset(), 0.0001);
        assertEquals(winter.getCol() + 0.5 + MelonMuzzles.x() + 0.25, first.getX(), 0.0001);
    }

    @Test
    void winterMelonPlantFoodFiresFourArcingMelonsOnSchedule() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Winter Melon", 1, 2, 1));
        Plant winter = session.getBoard().getPlantAt(1, 2);
        assertNotNull(winter);
        placeMovingZombie(6, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(winter.isPlantFooding());
        assertEquals(PeaPodPlantFood.Phase.LOOP, winter.plantFoodPhase());
        int[] shots = MelonPultPlantFood.SHOT_TICKS;
        int elapsed = 0;
        int expected = 0;
        for (int shotIndex = 0; shotIndex < shots.length; shotIndex++) {
            while (elapsed < shots[shotIndex] - 1) {
                session.tick();
                elapsed++;
                assertEquals(expected, session.getProjectileSystem().getProjectiles().size());
            }
            session.tick();
            elapsed++;
            expected++;
            assertEquals(expected, session.getProjectileSystem().getProjectiles().size());
            Projectile latest = session.getProjectileSystem().getProjectiles().getLast();
            assertTrue(latest.isMelonPlantFood());
            assertEquals(ProjectileEffect.WINTER_MELON, latest.getEffect());
            assertEquals(ProjectileProfile.Trajectory.ARCING, latest.getProfile().trajectory());
            assertEquals(400, latest.getDamage());
            assertEquals(winter.getRow(), latest.getRow());
            assertEquals(MelonMuzzles.plantFoodY(), latest.getLaneYOffset(), 0.0001);
        }
    }

    @Test
    void winterMelonPlantFoodFiresWithoutLaneTarget() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Winter Melon", 1, 2, 1));
        Plant winter = session.getBoard().getPlantAt(1, 2);
        assertNotNull(winter);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(winter.isPlantFooding());
        for (int i = 0; i < MelonPultPlantFood.SHOT_TICKS[0] - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertTrue(first.isMelonPlantFood());
        assertEquals(ProjectileEffect.WINTER_MELON, first.getEffect());
        for (int i = 0; i < 40; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void pepperPultFiresArcingPepperAfterWindup() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pepper-pult", 1, 2, 1));
        Plant pepper = session.getBoard().getPlantAt(1, 2);
        assertNotNull(pepper);
        placeMovingZombie(6, 2);
        for (int i = 0; i < ticksUntilPepperFirstShot(pepper); i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertFalse(first.isPepperPlantFood());
        assertEquals(ProjectileEffect.PEPPER, first.getEffect());
        assertEquals(ProjectileProfile.Trajectory.ARCING, first.getProfile().trajectory());
        assertEquals(pepper.getRow(), first.getRow());
        assertEquals(PepperMuzzles.y(), first.getLaneYOffset(), 0.0001);
        assertEquals(pepper.getCol() + 0.5 + PepperMuzzles.x() + 0.25, first.getX(), 0.0001);
    }

    @Test
    void pepperPlantFoodFiresThreeArcingPeppersOnSchedule() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pepper-pult", 1, 2, 1));
        Plant pepper = session.getBoard().getPlantAt(1, 2);
        assertNotNull(pepper);
        placeMovingZombie(6, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(pepper.isPlantFooding());
        assertEquals(PeaPodPlantFood.Phase.LOOP, pepper.plantFoodPhase());
        int[] shots = PepperPultPlantFood.SHOT_TICKS;
        int elapsed = 0;
        int expected = 0;
        for (int shotIndex = 0; shotIndex < shots.length; shotIndex++) {
            while (elapsed < shots[shotIndex] - 1) {
                session.tick();
                elapsed++;
                assertEquals(expected, session.getProjectileSystem().getProjectiles().size());
            }
            session.tick();
            elapsed++;
            expected++;
            assertEquals(expected, session.getProjectileSystem().getProjectiles().size());
            Projectile latest = session.getProjectileSystem().getProjectiles().getLast();
            assertTrue(latest.isPepperPlantFood());
            assertEquals(ProjectileEffect.PEPPER, latest.getEffect());
            assertEquals(ProjectileProfile.Trajectory.ARCING, latest.getProfile().trajectory());
            assertEquals(250, latest.getDamage());
            assertEquals(pepper.getRow(), latest.getRow());
            assertEquals(PepperMuzzles.plantFoodY(shotIndex), latest.getLaneYOffset(), 0.0001);
            assertEquals(pepper.getCol() + 0.5 + PepperMuzzles.plantFoodX(shotIndex) + 0.25,
                    latest.getX(), 0.0001);
        }
    }

    @Test
    void pepperPlantFoodFiresWithoutLaneTarget() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pepper-pult", 1, 2, 1));
        Plant pepper = session.getBoard().getPlantAt(1, 2);
        assertNotNull(pepper);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(pepper.isPlantFooding());
        for (int i = 0; i < PepperPultPlantFood.SHOT_TICKS[0] - 1; i++) {
            session.tick();
            assertEquals(0, session.getProjectileSystem().getProjectiles().size());
        }
        session.tick();
        assertEquals(1, session.getProjectileSystem().getProjectiles().size());
        Projectile first = session.getProjectileSystem().getProjectiles().getFirst();
        assertTrue(first.isPepperPlantFood());
        assertEquals(ProjectileEffect.PEPPER, first.getEffect());
        for (int i = 0; i < 40; i++) {
            session.tick();
        }
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void potatoMinePlantFoodSpawnsForwardArmedClones() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Potato Mine", 1, 2, 1));
        Plant mine = session.getBoard().getPlantAt(1, 2);
        assertNotNull(mine);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(1, 2));
        assertTrue(mine.isPlantFooding());
        for (int i = 0; i < PotatoMinePlantFood.ON_TICKS; i++) {
            session.tick();
        }
        List<Plant> clones = session.getBoard().getAllPlants().stream()
                .filter(Plant::isPlantFoodSpawned)
                .toList();
        assertEquals(2, clones.size());
        for (Plant clone : clones) {
            assertTrue(clone.getCol() > mine.getCol());
            assertTrue(clone.isPlantFoodSpawned());
            assertTrue(clone.isArmedTrap());
        }
    }

    @Test
    void potatoMineDetonationDelaysExplosion() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Potato Mine", 1, 1, 1));
        Plant mine = session.getBoard().getPlantAt(1, 1);
        assertNotNull(mine);
        for (int i = 0; i < 150; i++) {
            session.tick();
        }
        assertTrue(mine.isArmedTrap());
        placeMovingZombie(1.5, 1);
        Zombie zombie = session.getZombies().getFirst();
        session.tick();
        assertTrue(mine.isAlive());
        assertTrue(mine.isAttacking());
        for (int i = 0; i < ExplosiveAbility.TRAP_DETONATION_WINDUP_TICKS - 1; i++) {
            session.tick();
            assertTrue(mine.isAlive());
        }
        session.tick();
        assertFalse(mine.isAlive());
        assertTrue(zombie.isDead() || zombie.getHealth() < 200);
    }

    @Test
    void cherryBombDetonationDelaysExplosion() {
        placeMovingZombie(3.5, 1);
        Zombie zombie = session.getZombies().getFirst();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Cherry Bomb", 3, 1, 1));
        Plant cherry = session.getBoard().getPlantAt(3, 1);
        assertNotNull(cherry);
        assertTrue(cherry.isAttacking());
        for (int i = 0; i < ExplosiveAbility.CHERRY_BOMB_DETONATION_WINDUP_TICKS - 1; i++) {
            session.tick();
            assertTrue(cherry.isAlive());
        }
        session.tick();
        assertFalse(cherry.isAlive());
        assertTrue(zombie.isDead() || zombie.getHealth() < 200);
    }

    @Test
    void cherryBombKillsZombieOnSameTile() {
        placeMovingZombie(3.0, 1);
        Zombie zombie = session.getZombies().getFirst();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Cherry Bomb", 3, 1, 1));
        Plant cherry = session.getBoard().getPlantAt(3, 1);
        assertNotNull(cherry);
        assertTrue(cherry.isAttacking());
        for (int i = 0; i < ExplosiveAbility.CHERRY_BOMB_DETONATION_WINDUP_TICKS - 1; i++) {
            session.tick();
            assertTrue(cherry.isAlive());
        }
        session.tick();
        assertFalse(cherry.isAlive());
        assertTrue(zombie.isDead());
    }

    @Test
    void squashSmashDamagesZombieToTheRight() {
        placeMovingZombie(2.0, 1);
        Zombie zombie = session.getZombies().getFirst();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Squash", 1, 1, 1));
        Plant squash = session.getBoard().getPlantAt(1, 1);
        assertNotNull(squash);
        SquashAbility ability = (SquashAbility) squash.getAbility();
        assertFalse(squash.isArmedTrap());
        assertEquals(SquashAbility.BASE_SMASH_CHARGES, ability.smashesRemaining());
        for (int i = 0; i < 10; i++) {
            session.tick();
            if (ability.isSmashing()) {
                break;
            }
        }
        assertTrue(ability.isSmashing());
        int healthBefore = zombie.getHealth();
        for (int i = 0; i < ticksUntilSquashLandingDamage(false); i++) {
            session.tick();
        }
        assertTrue(zombie.isDead() || zombie.getHealth() < healthBefore);
        for (int i = 0; i < ticksUntilSquashReturnHome(false); i++) {
            session.tick();
        }
        assertTrue(squash.isAlive());
        assertEquals(SquashAbility.Phase.IDLE, ability.phase());
        assertEquals(SquashAbility.BASE_SMASH_CHARGES - 1, ability.smashesRemaining());
    }

    @Test
    void squashSecondSmashConsumesOnLanding() {
        placeMovingZombie(2.0, 1);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Squash", 1, 1, 1));
        Plant squash = session.getBoard().getPlantAt(1, 1);
        SquashAbility ability = (SquashAbility) squash.getAbility();
        for (int i = 0; i < 10; i++) {
            session.tick();
            if (ability.isSmashing()) {
                break;
            }
        }
        assertTrue(ability.isSmashing());
        for (int i = 0; i < ticksUntilSquashLandingDamage(false) + ticksUntilSquashReturnHome(false); i++) {
            session.tick();
        }
        assertTrue(squash.isAlive());
        assertEquals(SquashAbility.BASE_SMASH_CHARGES - 1, ability.smashesRemaining());
        placeMovingZombie(2.0, 1);
        for (int i = 0; i < 10; i++) {
            session.tick();
            if (ability.isSmashing()) {
                break;
            }
        }
        assertTrue(ability.isSmashing());
        for (int i = 0; i < ticksUntilSquashLandingDamage(false); i++) {
            session.tick();
        }
        assertFalse(squash.isAlive());
    }

    @Test
    void squashPlantFoodTargetsOtherLane() {
        placeMovingZombie(3.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Squash", 2, 1, 1));
        Plant squash = session.getBoard().getPlantAt(2, 1);
        assertNotNull(squash);
        SquashAbility ability = (SquashAbility) squash.getAbility();
        session.addPlantFood(1);
        assertTrue(session.usePlantFood(2, 1));
        for (int i = 0; i < 5; i++) {
            session.tick();
            if (ability.isSmashing()) {
                break;
            }
        }
        assertTrue(ability.isSmashing());
        assertTrue(ability.plantFoodActive());
        assertEquals(2, ability.targetRow());
        int healthBefore = zombie.getHealth();
        tickUntilSquashIdle(true, 120);
        assertTrue(zombie.isDead() || zombie.getHealth() < healthBefore);
        assertTrue(squash.isAlive());
        assertEquals(SquashAbility.Phase.IDLE, ability.phase());
        assertEquals(SquashAbility.BASE_SMASH_CHARGES - 1, ability.smashesRemaining());
    }

    @Test
    void squashPlantFoodChainsUpToFiveZombiesAsOneCharge() {
        placeMovingZombie(3.0, 1);
        placeMovingZombie(3.0, 2);
        placeMovingZombie(2.0, 2);
        List<Zombie> targets = new ArrayList<>(session.getZombies());
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Squash", 2, 1, 1));
        Plant squash = session.getBoard().getPlantAt(2, 1);
        SquashAbility ability = (SquashAbility) squash.getAbility();
        session.addPlantFood(1);
        assertTrue(session.usePlantFood(2, 1));
        tickUntilSquashIdle(true, 400);
        assertTrue(squash.isAlive());
        assertEquals(SquashAbility.Phase.IDLE, ability.phase());
        assertEquals(SquashAbility.BASE_SMASH_CHARGES - 1, ability.smashesRemaining());
        int killed = 0;
        for (Zombie zombie : targets) {
            if (zombie.isDead()) {
                killed++;
            }
        }
        assertTrue(killed >= 3);
    }

    @Test
    void squashPfDoesNotReturnHomeAfterFirstKillWhenMoreTargetsExist() {
        placeMovingZombie(3.0, 1);
        placeMovingZombie(3.0, 2);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Squash", 2, 1, 1));
        Plant squash = session.getBoard().getPlantAt(2, 1);
        SquashAbility ability = (SquashAbility) squash.getAbility();
        session.addPlantFood(1);
        assertTrue(session.usePlantFood(2, 1));
        for (int i = 0; i < 5; i++) {
            session.tick();
            if (ability.isSmashing()) {
                break;
            }
        }
        assertTrue(ability.isSmashing());
        for (int i = 0; i < ticksUntilSquashLandingDamage(true); i++) {
            session.tick();
        }
        assertTrue(ability.plantFoodChain());
        assertNotEquals(SquashAbility.Phase.JUMP_UP_LEFT, ability.phase());
        assertNotEquals(SquashAbility.Phase.JUMP_DOWN_LEFT, ability.phase());
        assertTrue(ability.phase() == SquashAbility.Phase.TURN
                || ability.phase() == SquashAbility.Phase.JUMP_UP_RIGHT);
    }

    @Test
    void squashSegmentCoordsResetAfterReturn() {
        placeMovingZombie(2.0, 1);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Squash", 1, 1, 1));
        Plant squash = session.getBoard().getPlantAt(1, 1);
        SquashAbility ability = (SquashAbility) squash.getAbility();
        for (int i = 0; i < 5; i++) {
            session.tick();
            if (ability.isSmashing()) {
                break;
            }
        }
        for (int i = 0; i < ticksUntilSquashLandingDamage(false) + ticksUntilSquashReturnHome(false); i++) {
            session.tick();
        }
        assertEquals(SquashAbility.Phase.IDLE, ability.phase());
        assertEquals(-1, ability.segmentFromCol());
        assertEquals(-1, ability.segmentToCol());
    }

    @Test
    void grapeshotWaitsIdleThenAttacks() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Grapeshot", 3, 2, 1));
        Plant grapeshot = session.getBoard().getPlantAt(3, 2);
        assertNotNull(grapeshot);
        GrapeshotAbility ability = (GrapeshotAbility) grapeshot.getAbility();
        assertEquals(GrapeshotAbility.Phase.IDLE, ability.phase());
        assertFalse(grapeshot.isAttacking());
        for (int i = 0; i < GrapeshotMuzzles.idleTicks(); i++) {
            session.tick();
            assertFalse(grapeshot.isAttacking());
        }
        session.tick();
        assertTrue(grapeshot.isAttacking());
        assertEquals(GrapeshotAbility.Phase.ATTACK, ability.phase());
    }

    @Test
    void grapeshotExplodesAndSpawnsSevenGrapes() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Grapeshot", 3, 2, 1));
        for (int i = 0; i < ticksUntilGrapeshotDetonation(); i++) {
            session.tick();
        }
        assertNull(session.getBoard().getPlantAt(3, 2));
        long bouncing = session.getProjectileSystem().getProjectiles().stream()
                .filter(p -> p.getProfile().trajectory() == ProjectileProfile.Trajectory.BOUNCING)
                .count();
        assertEquals(GrapeshotMuzzles.GRAPE_COUNT, bouncing);
    }

    @Test
    void grapeshotExplosionHitsThreeByThree() {
        placeMovingZombie(3.0, 2);
        placeMovingZombie(4.0, 2);
        placeMovingZombie(6.0, 2);
        Zombie inRange = session.getZombies().get(0);
        Zombie inRangeAdjacent = session.getZombies().get(1);
        Zombie outOfRange = session.getZombies().get(2);
        int hpIn = inRange.getHealth();
        int hpAdj = inRangeAdjacent.getHealth();
        int hpOut = outOfRange.getHealth();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Grapeshot", 3, 2, 1));
        for (int i = 0; i < ticksUntilGrapeshotDetonation(); i++) {
            session.tick();
        }
        assertTrue(inRange.getHealth() < hpIn);
        assertTrue(inRangeAdjacent.getHealth() < hpAdj);
        assertEquals(hpOut, outOfRange.getHealth());
    }

    @Test
    void grapeshotGrapeBouncesOffEdge() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Grapeshot", 3, 2, 1));
        Plant grapeshot = session.getBoard().getPlantAt(3, 2);
        session.getProjectileSystem().spawnGrapeshotGrapes(
                grapeshot, 1, 1800, session.getBoard().getRows(), session.getBoard().getCols());
        Projectile grape = session.getProjectileSystem().getProjectiles().getFirst();
        grape.setX(session.getBoard().getCols() - 1.05);
        grape.setVelocity(0.5, 0.0);
        double velocityBefore = grape.getVelocityX();
        assertTrue(velocityBefore > 0);
        session.tick();
        assertTrue(grape.getVelocityX() < 0);
    }

    @Test
    void grapeshotGrapeDamagesZombieOnContact() {
        placeMovingZombie(5.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Grapeshot", 3, 2, 1));
        Plant grapeshot = session.getBoard().getPlantAt(3, 2);
        session.getProjectileSystem().spawnGrapeshotGrapes(
                grapeshot, 1, grapeshot.getStats().damage(),
                session.getBoard().getRows(), session.getBoard().getCols());
        Projectile grape = session.getProjectileSystem().getProjectiles().getFirst();
        grape.setX(5.0);
        grape.setRowPosition(2.0);
        grape.setVelocity(0.0, 0.0);
        int healthBefore = zombie.getHealth();
        session.tick();
        assertTrue(zombie.getHealth() < healthBefore);
        assertTrue(session.getProjectileSystem().getProjectiles().isEmpty());
    }

    @Test
    void squashDoesNotDetonateWhenZombieStepsOnTile() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Squash", 1, 1, 1));
        Plant squash = session.getBoard().getPlantAt(1, 1);
        assertNotNull(squash);
        placeMovingZombie(1.0, 1);
        for (int i = 0; i < 20; i++) {
            session.tick();
        }
        assertTrue(squash.isAlive());
        assertFalse(squash.isArmedTrap());
    }

    @Test
    void jalapenoWaitsIdleThenAttacks() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Jalapeno", 4, 2, 1));
        Plant jalapeno = session.getBoard().getPlantAt(4, 2);
        assertNotNull(jalapeno);
        JalapenoAbility ability = (JalapenoAbility) jalapeno.getAbility();
        assertEquals(JalapenoAbility.Phase.IDLE, ability.phase());
        assertFalse(jalapeno.isAttacking());
        for (int i = 0; i < JalapenoMuzzles.idleTicks(); i++) {
            session.tick();
            assertFalse(jalapeno.isAttacking());
        }
        session.tick();
        assertTrue(jalapeno.isAttacking());
        assertEquals(JalapenoAbility.Phase.ATTACK, ability.phase());
    }

    @Test
    void jalapenoRowFirePropagatesWithDelay() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Jalapeno", 4, 2, 1));
        for (int i = 0; i < ticksUntilJalapenoIgnite(); i++) {
            session.tick();
        }
        assertNull(session.getBoard().getPlantAt(4, 2));
        List<JalapenoFireMark> marks = session.getJalapenoFireSystem().drainFireMarks();
        assertTrue(marks.stream().anyMatch(m -> m.row() == 2 && m.col() == 4));
        assertFalse(marks.stream().anyMatch(m -> m.row() == 2 && m.col() == 2));
        int ticksForCol2 = JalapenoMuzzles.PROPAGATION_DELAY_TICKS * 2;
        for (int i = 0; i < ticksForCol2; i++) {
            session.tick();
        }
        marks = session.getJalapenoFireSystem().drainFireMarks();
        assertTrue(marks.stream().anyMatch(m -> m.row() == 2 && m.col() == 2));
    }

    @Test
    void jalapenoFireDamagesZombieOnTile() {
        placeMovingZombie(5.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        int healthBefore = zombie.getHealth();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Jalapeno", 4, 2, 1));
        for (int i = 0; i < ticksUntilJalapenoIgnite(); i++) {
            session.tick();
        }
        for (int i = 0; i < JalapenoMuzzles.PROPAGATION_DELAY_TICKS; i++) {
            session.tick();
        }
        assertTrue(zombie.getHealth() < healthBefore);
    }

    @Test
    void jalapenoFireDamagesAllZombiesInRow() {
        placeMovingZombie(1.5, 2);
        placeMovingZombie(5.5, 2);
        placeMovingZombie(7.3, 2);
        Zombie left = session.getZombies().get(0);
        Zombie center = session.getZombies().get(1);
        Zombie right = session.getZombies().get(2);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Jalapeno", 4, 2, 1));
        for (int i = 0; i < ticksUntilJalapenoIgnite(); i++) {
            session.tick();
        }
        int maxPropagationTicks = JalapenoMuzzles.PROPAGATION_DELAY_TICKS
                * (session.getBoard().getCols() - 1);
        for (int i = 0; i < maxPropagationTicks; i++) {
            session.tick();
        }
        assertTrue(left.isDead());
        assertTrue(center.isDead());
        assertTrue(right.isDead());
    }

    @Test
    void jalapenoFireMeltsIce() {
        IceTile ice = new IceTile();
        session.getBoard().setTile(5, 2, ice);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Jalapeno", 4, 2, 1));
        for (int i = 0; i < ticksUntilJalapenoIgnite(); i++) {
            session.tick();
        }
        assertTrue(session.getBoard().getTile(5, 2).isIce());
        for (int i = 0; i < JalapenoMuzzles.PROPAGATION_DELAY_TICKS; i++) {
            session.tick();
        }
        assertFalse(session.getBoard().getTile(5, 2).isIce());
    }

    @Test
    void jalapenoDoesNotUseThreeByThreeExplosion() {
        placeMovingZombie(4.0, 1);
        placeMovingZombie(4.0, 3);
        Zombie offRowA = session.getZombies().get(0);
        Zombie offRowB = session.getZombies().get(1);
        int healthA = offRowA.getHealth();
        int healthB = offRowB.getHealth();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Jalapeno", 4, 2, 1));
        for (int i = 0; i < ticksUntilJalapenoIgnite(); i++) {
            session.tick();
        }
        assertEquals(healthA, offRowA.getHealth());
        assertEquals(healthB, offRowB.getHealth());
    }

    @Test
    void doomShroomDoesNotExplodeOnPlant() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        Plant doom = session.getBoard().getPlantAt(4, 2);
        assertNotNull(doom);
        session.tick();
        assertTrue(doom.isAlive());
        assertInstanceOf(DoomShroomAbility.class, doom.getAbility());
    }

    @Test
    void doomShroomGrowsThroughThreeStages() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        Plant doom = session.getBoard().getPlantAt(4, 2);
        assertEquals(0, doom.getGrowthStage());
        for (int i = 0; i < DoomShroomMuzzles.stage1ToStage2Ticks(); i++) {
            session.tick();
        }
        assertEquals(1, doom.getGrowthStage());
        for (int i = 0; i < DoomShroomMuzzles.stage2ToStage3Ticks(); i++) {
            session.tick();
        }
        assertEquals(2, doom.getGrowthStage());
    }

    @Test
    void doomShroomDetonatesOnSameTileZombie() {
        placeMovingZombie(4.0, 2);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        Plant doom = session.getBoard().getPlantAt(4, 2);
        DoomShroomAbility ability = (DoomShroomAbility) doom.getAbility();
        tickUntilDoomShroomDetonates(doom);
        assertEquals(DoomShroomAbility.Phase.DONE, ability.phase());
        assertNull(session.getBoard().getPlantAt(4, 2));
    }

    @Test
    void doomShroomStage1ThreeByThreeDamage() {
        placeMovingZombie(4.0, 2);
        placeMovingZombie(5.0, 2);
        placeMovingZombie(6.0, 2);
        Zombie center = session.getZombies().get(0);
        Zombie adjacent = session.getZombies().get(1);
        Zombie outOfRange = session.getZombies().get(2);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        tickUntilDoomShroomDetonates(session.getBoard().getPlantAt(4, 2));
        assertTrue(center.isDead());
        assertTrue(adjacent.isDead());
        assertFalse(outOfRange.isDead());
    }

    @Test
    void doomShroomStage3FiveByFiveDamage() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        for (int i = 0; i < DoomShroomMuzzles.stage1ToStage2Ticks()
                + DoomShroomMuzzles.stage2ToStage3Ticks(); i++) {
            session.tick();
        }
        placeMovingZombie(6.0, 2);
        placeMovingZombie(2.0, 2);
        placeMovingZombie(4.0, 2);
        Zombie center = session.getZombies().get(2);
        Zombie inRange = session.getZombies().get(0);
        Zombie edgeInRange = session.getZombies().get(1);
        tickUntilDoomShroomDetonates(session.getBoard().getPlantAt(4, 2));
        assertTrue(center.isDead());
        assertTrue(inRange.isDead());
        assertTrue(edgeInRange.isDead());
    }

    @Test
    void doomShroomMediumSpawnsSeedlings() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        for (int i = 0; i < DoomShroomMuzzles.stage1ToStage2Ticks(); i++) {
            session.tick();
        }
        assertEquals(1, session.getBoard().getPlantAt(4, 2).getGrowthStage());
        placeMovingZombie(4.0, 2);
        tickUntilDoomShroomDetonates(session.getBoard().getPlantAt(4, 2));
        long seedlings = session.getBoard().getAllPlants().stream()
                .filter(Plant::isDoomShroom)
                .filter(p -> p.getGrowthStage() == 0)
                .count();
        assertEquals(1, seedlings);
    }

    @Test
    void doomShroomDamagesOnTileZombieAfterExplosionWindup() {
        placeMovingZombie(4.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        Plant doom = session.getBoard().getPlantAt(4, 2);
        int delay = DoomShroomMuzzles.explosionDamageDelayTicks();
        for (int i = 0; i < delay - 1; i++) {
            session.tick();
            assertTrue(doom.isAlive());
            assertFalse(zombie.isDead());
        }
        session.tick();
        assertTrue(zombie.isDead());
    }

    @Test
    void doomShroomPlantFoodAdvancesAllAndExplodes() {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 2, 1, 1));
        session.getCooldownTracker().resetAll();
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 6, 3, 1));
        Plant first = session.getBoard().getPlantAt(2, 1);
        Plant second = session.getBoard().getPlantAt(6, 3);
        session.addPlantFood(1);
        assertTrue(session.usePlantFood(2, 1));
        assertEquals(1, first.getGrowthStage());
        assertEquals(1, second.getGrowthStage());
        for (int i = 0; i < DoomShroomMuzzles.transformTicks()
                + DoomShroomMuzzles.explodeTicks(1) + 2; i++) {
            session.tick();
        }
        assertNull(session.getBoard().getPlantAt(2, 1));
        assertNull(session.getBoard().getPlantAt(6, 3));
        assertTrue(session.getBoard().getTile(2, 1).isCrater());
        assertTrue(session.getBoard().getTile(6, 3).isCrater());
    }

    @Test
    void doomShroomLeavesTimedCrater() {
        placeMovingZombie(4.0, 2);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        tickUntilDoomShroomDetonates(session.getBoard().getPlantAt(4, 2));
        assertTrue(session.getBoard().getTile(4, 2).isCrater());
        assertEquals(PlantPlacementResult.TILE_BLOCKED,
                session.tryPlant("Peashooter", 4, 2, 1));
        for (int i = 0; i < DoomShroomMuzzles.craterDurationTicks(); i++) {
            session.tick();
        }
        assertFalse(session.getBoard().getTile(4, 2).isCrater());
        assertInstanceOf(NormalTile.class, session.getBoard().getTile(4, 2));
        assertEquals(PlantPlacementResult.SUCCESS,
                session.tryPlant("Peashooter", 4, 2, 1));
    }

    @Test
    void doomShroomIgnoresOffRowZombiesAtDetonation() {
        placeMovingZombie(7.0, 2);
        placeMovingZombie(4.0, 0);
        Zombie outOfRangeLane = session.getZombies().get(0);
        Zombie outOfRangeRow = session.getZombies().get(1);
        int healthLane = outOfRangeLane.getHealth();
        int healthRow = outOfRangeRow.getHealth();
        placeMovingZombie(4.0, 2);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Doom-shroom", 4, 2, 1));
        tickUntilDoomShroomDetonates(session.getBoard().getPlantAt(4, 2));
        assertEquals(healthLane, outOfRangeLane.getHealth());
        assertEquals(healthRow, outOfRangeRow.getHealth());
    }

    @Test
    void tangleKelpGrabKillsZombieAndSurvives() {
        placeMovingZombie(4.0, 2);
        Plant kelp = plantTangleKelp(4, 2);
        session.tick();
        assertTrue(kelp.isTangleKelpGrabbing());
        tickUntilTangleKelpIdle(kelp);
        assertTrue(kelp.isAlive());
        assertTrue(kelp.isArmedTrap());
        assertEquals(0, session.getZombies().stream().filter(Zombie::isAlive).count());
    }

    @Test
    void tangleKelpGrabDamagesGargantuarWithoutSubmerge() {
        Zombie garg = new Zombie.Builder("ZombieGargantuar")
                .maxHealth(3000)
                .speed(0)
                .position(4.0, 2)
                .build();
        garg.setState(ZombieState.MOVING);
        session.addZombie(garg);
        Plant kelp = plantTangleKelp(4, 2);
        session.tick();
        tickUntilTangleKelpIdle(kelp);
        assertFalse(garg.isSubmerged());
        assertTrue(kelp.isAlive());
        assertTrue(kelp.isArmedTrap());
    }

    @Test
    void tangleKelpPlantFoodPullsFourRandomZombies() {
        for (int i = 0; i < 6; i++) {
            placeMovingZombie(i + 1.0, i % 5);
        }
        Plant kelp = plantTangleKelp(4, 2);
        session.addPlantFood(1);
        assertTrue(session.usePlantFood(4, 2));
        long dead = session.getZombies().stream().filter(Zombie::isDead).count();
        assertEquals(4, dead);
        List<TangleKelpGrabMark> marks = session.getTangleKelpGrabSystem().drainGrabMarks();
        assertEquals(4, marks.size());
    }

    @Test
    void tangleKelpPlantFoodDamagesGargantuarAmongTargets() {
        placeMovingZombie(1.0, 0);
        placeMovingZombie(2.0, 1);
        Zombie garg = new Zombie.Builder("ZombieGargantuar")
                .maxHealth(5000)
                .speed(0)
                .position(3.0, 2)
                .build();
        garg.setState(ZombieState.MOVING);
        session.addZombie(garg);
        placeMovingZombie(5.0, 3);
        Plant kelp = plantTangleKelp(4, 2);
        session.addPlantFood(1);
        assertTrue(session.usePlantFood(4, 2));
        assertFalse(garg.isSubmerged());
    }

    @Test
    void tangleKelpPlantFoodResetsMidGrab() {
        placeMovingZombie(4.0, 2);
        Plant kelp = plantTangleKelp(4, 2);
        session.tick();
        assertTrue(kelp.isTangleKelpGrabbing());
        session.addPlantFood(1);
        assertTrue(session.usePlantFood(4, 2));
        TangleKelpAbility ability = (TangleKelpAbility) kelp.getAbility();
        assertFalse(kelp.isTangleKelpGrabbing());
        assertEquals(TangleKelpAbility.Phase.PLANT_FOOD_ON, ability.phase());
    }

    @Test
    void icebergLettuceTrapFreezesZombieAndConsumesPlant() {
        Plant iceberg = plantIcebergLettuce(4, 2);
        placeMovingZombie(4.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        session.tick();
        assertTrue(zombie.getFreezeTicksRemaining() > 0);
        assertTrue(iceberg.isIcebergLettuceFreezing());
        tickUntilIcebergLettuceIdle(iceberg);
        assertNull(session.getBoard().getPlantAt(4, 2));
        assertTrue(zombie.getFreezeTicksRemaining() > 0);
    }

    @Test
    void icebergLettuceSkipsAirborneDodo() {
        plantIcebergLettuce(4, 2);
        Zombie dodo = new Zombie.Builder("ZombieIceAgeDodo")
                .maxHealth(200)
                .speed(0)
                .position(4.0, 2)
                .build();
        dodo.setState(ZombieState.MOVING);
        session.addZombie(dodo);
        session.tick();
        Plant iceberg = session.getBoard().getPlantAt(4, 2);
        assertNotNull(iceberg);
        assertTrue(iceberg.isAlive());
        assertTrue(iceberg.isArmedTrap());
        assertEquals(0, dodo.getFreezeTicksRemaining());
    }

    @Test
    void icebergLettucePlantFoodFreezesGroundedAndSurvives() {
        Plant iceberg = plantIcebergLettuce(4, 2);
        placeMovingZombie(3.0, 2);
        placeMovingZombie(5.0, 3);
        Zombie grounded = session.getZombies().getFirst();
        Zombie otherRow = session.getZombies().get(1);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilIcebergLettuceIdle(iceberg);
        iceberg = session.getBoard().getPlantAt(4, 2);
        assertNotNull(iceberg);
        assertTrue(iceberg.isAlive());
        assertTrue(iceberg.isArmedTrap());
        assertTrue(grounded.getFreezeTicksRemaining() > 0);
        assertTrue(otherRow.getFreezeTicksRemaining() > 0);
    }

    @Test
    void icebergLettucePlantFoodEnqueuesFlash() {
        Plant iceberg = plantIcebergLettuce(4, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilIcebergLettuceIdle(iceberg);
        assertFalse(session.getIcebergFlashSystem().drainFlashMarks().isEmpty());
    }

    @Test
    void icebergLettuceTrapChillsFrostbiteZombie() {
        session.setZombiesImmuneToChill(true);
        plantIcebergLettuce(4, 2);
        Zombie hunter = new Zombie.Builder("ZombieIceAgeHunter")
                .maxHealth(200)
                .speed(1.0)
                .position(4.0, 2)
                .build();
        hunter.setState(ZombieState.MOVING);
        session.addZombie(hunter);
        session.tick();
        assertEquals(0, hunter.getFreezeTicksRemaining());
        assertEquals(0.5, hunter.getCurrentSpeed() / hunter.getBaseSpeed(), 0.0001);
    }

    @Test
    void icebergLettucePlantFoodChillsFrostbiteZombies() {
        session.setZombiesImmuneToChill(true);
        Plant iceberg = plantIcebergLettuce(4, 2);
        Zombie frost = new Zombie.Builder("ZombieIceAgeHunter")
                .maxHealth(200)
                .speed(1.0)
                .position(3.0, 1)
                .build();
        frost.setState(ZombieState.MOVING);
        session.addZombie(frost);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilIcebergLettuceIdle(iceberg);
        assertEquals(0, frost.getFreezeTicksRemaining());
        assertEquals(0.5, frost.getCurrentSpeed() / frost.getBaseSpeed(), 0.0001);
    }

    @Test
    void bonkChoyPunchesRightZombie() {
        Plant bonk = plantBonkChoy(4, 2);
        placeMovingZombie(5.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        int health = zombie.getHealth();
        tickUntilBonkDamages(zombie);
        assertEquals(health - 15, zombie.getHealth());
        assertTrue(bonk.isAlive());
    }

    @Test
    void bonkChoyPunchesLeftZombie() {
        plantBonkChoy(4, 2);
        placeMovingZombie(3.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        int health = zombie.getHealth();
        tickUntilBonkDamages(zombie);
        assertEquals(health - 15, zombie.getHealth());
    }

    @Test
    void bonkChoyPunchesBothSides() {
        plantBonkChoy(4, 2);
        placeMovingZombie(5.0, 2);
        placeMovingZombie(3.0, 2);
        Zombie front = session.getZombies().get(0);
        Zombie behind = session.getZombies().get(1);
        int frontHealth = front.getHealth();
        int behindHealth = behind.getHealth();
        tickUntilBonkDamages(front);
        assertTrue(front.getHealth() < frontHealth);
        assertTrue(behind.getHealth() < behindHealth);
    }

    @Test
    void bonkChoyPunchesDiagonalTarget() {
        plantBonkChoy(4, 2);
        placeMovingZombie(5.0, 1);
        Zombie diagonal = session.getZombies().getFirst();
        int health = diagonal.getHealth();
        tickUntilBonkDamages(diagonal);
        assertEquals(health - 15, diagonal.getHealth());
        assertInstanceOf(BonkChoyAbility.class, session.getBoard().getPlantAt(4, 2).getAbility());
        assertEquals(BonkChoyAbility.PunchStyle.UP_RIGHT,
                ((BonkChoyAbility) session.getBoard().getPlantAt(4, 2).getAbility()).punchStyle());
    }

    @Test
    void bonkChoyDoesNotPunchWithoutTarget() {
        Plant bonk = plantBonkChoy(4, 2);
        for (int i = 0; i < 20; i++) {
            session.tick();
        }
        assertFalse(bonk.isAttacking());
    }

    @Test
    void bonkChoyPlantFoodDealsAreaDamage() {
        Plant bonk = plantBonkChoy(4, 2);
        Zombie zombie = new Zombie.Builder("dummy")
                .maxHealth(5000)
                .speed(0)
                .position(4.0, 2)
                .build();
        zombie.setState(ZombieState.MOVING);
        session.addZombie(zombie);
        int startHealth = zombie.getHealth();
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilBonkPlantFoodDone(bonk);
        assertTrue(bonk.isAlive());
        assertTrue(startHealth - zombie.getHealth() >= BonkChoyMuzzles.PLANT_FOOD_TOTAL_DAMAGE - 50);
    }

    @Test
    void wasabiWhipWhipsBehindZombie() {
        Plant whip = plantWasabiWhip(4, 2);
        placeMovingZombie(3.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        int health = zombie.getHealth();
        tickUntilWasabiDamages(zombie);
        assertEquals(health - 40, zombie.getHealth());
        assertEquals(WasabiWhipAbility.WhipStyle.LEFT,
                ((WasabiWhipAbility) whip.getAbility()).whipStyle());
        assertTrue(whip.isAlive());
    }

    @Test
    void wasabiWhipWhipsAheadZombie() {
        Plant whip = plantWasabiWhip(4, 2);
        placeMovingZombie(5.0, 2);
        Zombie zombie = session.getZombies().getFirst();
        int health = zombie.getHealth();
        tickUntilWasabiDamages(zombie);
        assertEquals(health - 40, zombie.getHealth());
        assertEquals(WasabiWhipAbility.WhipStyle.RIGHT,
                ((WasabiWhipAbility) whip.getAbility()).whipStyle());
    }

    @Test
    void wasabiWhipPrefersBehindWhenBothSides() {
        plantWasabiWhip(4, 2);
        placeMovingZombie(5.0, 2);
        placeMovingZombie(3.0, 2);
        Zombie front = session.getZombies().get(0);
        Zombie behind = session.getZombies().get(1);
        int frontHealth = front.getHealth();
        int behindHealth = behind.getHealth();
        tickUntilWasabiDamages(behind);
        assertEquals(behindHealth - 40, behind.getHealth());
        assertEquals(frontHealth, front.getHealth());
        assertEquals(WasabiWhipAbility.WhipStyle.LEFT,
                ((WasabiWhipAbility) session.getBoard().getPlantAt(4, 2).getAbility()).whipStyle());
    }

    @Test
    void wasabiWhipWhipsUpRightDiagonal() {
        Plant whip = plantWasabiWhip(4, 2);
        Zombie diagonal = placeDummyZombie(5.0, 1, 500);
        int health = diagonal.getHealth();
        tickUntilWasabiDamages(diagonal);
        assertEquals(health - 40, diagonal.getHealth());
        assertEquals(WasabiWhipAbility.WhipStyle.UP_RIGHT,
                ((WasabiWhipAbility) whip.getAbility()).whipStyle());
    }

    @Test
    void wasabiWhipWhipsUpLeftDiagonal() {
        Plant whip = plantWasabiWhip(4, 2);
        Zombie diagonal = placeDummyZombie(3.0, 1, 500);
        int health = diagonal.getHealth();
        tickUntilWasabiDamages(diagonal);
        assertEquals(health - 40, diagonal.getHealth());
        assertEquals(WasabiWhipAbility.WhipStyle.UP_LEFT,
                ((WasabiWhipAbility) whip.getAbility()).whipStyle());
    }

    @Test
    void wasabiWhipWhipsDownRightDiagonal() {
        Plant whip = plantWasabiWhip(4, 2);
        Zombie diagonal = placeDummyZombie(5.0, 3, 500);
        int health = diagonal.getHealth();
        tickUntilWasabiDamages(diagonal);
        assertEquals(health - 40, diagonal.getHealth());
        assertEquals(WasabiWhipAbility.WhipStyle.DOWN_RIGHT,
                ((WasabiWhipAbility) whip.getAbility()).whipStyle());
    }

    @Test
    void wasabiWhipSplashesHalfDamageToAdjacentRows() {
        plantWasabiWhip(4, 2);
        Zombie ownLane = placeDummyZombie(5.0, 2, 500);
        Zombie above = placeDummyZombie(5.0, 1, 500);
        int ownHealth = ownLane.getHealth();
        int aboveHealth = above.getHealth();
        tickUntilWasabiDamages(ownLane);
        assertEquals(ownHealth - 40, ownLane.getHealth());
        assertEquals(aboveHealth - 20, above.getHealth());
    }

    @Test
    void wasabiWhipHitsTwoTilesAheadButNotThree() {
        plantWasabiWhip(4, 2);
        Zombie inRange = placeDummyZombie(6.0, 2, 500);
        Zombie outOfRange = placeDummyZombie(7.0, 2, 500);
        int inHealth = inRange.getHealth();
        int outHealth = outOfRange.getHealth();
        tickUntilWasabiDamages(inRange);
        assertEquals(inHealth - 40, inRange.getHealth());
        assertEquals(outHealth, outOfRange.getHealth());
    }

    @Test
    void wasabiWhipPlantFoodDealsAreaDamage() {
        Plant whip = plantWasabiWhip(4, 2);
        Zombie zombie = placeDummyZombie(4.0, 2, 5000);
        int startHealth = zombie.getHealth();
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilWasabiPlantFoodDone(whip);
        assertTrue(whip.isAlive());
        assertTrue(startHealth - zombie.getHealth() >= WasabiWhipMuzzles.PLANT_FOOD_TOTAL_DAMAGE - 50);
    }

    @Test
    void wasabiWhipIgnoresFrostStacks() {
        Plant whip = plantWasabiWhip(4, 2);
        assertTrue(whip.hasTag(PlantTag.FIRE));
        session.addPlantFrostStack(whip);
        session.addPlantFrostStack(whip);
        session.addPlantFrostStack(whip);
        assertEquals(0, whip.getHostileIceStacks(null));
        assertTrue(whip.isAlive());
        assertFalse(whip.isDisabled());
    }

    @Test
    void kiwibeastStageOneHitsThreeByThree() {
        Plant kiwi = plantKiwibeast(4, 2);
        Zombie inner = placeDummyZombie(5.0, 2, 500);
        Zombie outer = placeDummyZombie(6.0, 2, 500);
        int innerHealth = inner.getHealth();
        int outerHealth = outer.getHealth();
        tickUntilKiwibeastDamages(inner);
        assertEquals(innerHealth - KiwibeastMuzzles.STAGE1_DAMAGE, inner.getHealth());
        assertEquals(outerHealth, outer.getHealth());
        assertEquals(1, kiwi.kiwibeastStage());
        assertInstanceOf(KiwibeastAbility.class, kiwi.getAbility());
    }

    @Test
    void kiwibeastGrowsToStageTwoAfterThreeHundredDamage() {
        Plant kiwi = plantKiwibeast(4, 2);
        kiwi.takeDamage(KiwibeastMuzzles.STAGE2_DAMAGE_TAKEN);
        assertEquals(2, kiwi.kiwibeastStage());
        Zombie inner = placeDummyZombie(6.0, 2, 500);
        Zombie outer = placeDummyZombie(7.0, 2, 500);
        int innerHealth = inner.getHealth();
        int outerHealth = outer.getHealth();
        tickUntilKiwibeastDamages(inner);
        assertEquals(innerHealth - KiwibeastMuzzles.STAGE2_DAMAGE, inner.getHealth());
        assertEquals(outerHealth, outer.getHealth());
    }

    @Test
    void kiwibeastGrowsToStageThreeAfterThousandDamage() {
        Plant kiwi = plantKiwibeast(4, 2);
        kiwi.takeDamage(KiwibeastMuzzles.STAGE3_DAMAGE_TAKEN);
        assertEquals(3, kiwi.kiwibeastStage());
        Zombie inner = placeDummyZombie(7.0, 2, 500);
        Zombie outer = placeDummyZombie(8.0, 2, 500);
        int innerHealth = inner.getHealth();
        int outerHealth = outer.getHealth();
        tickUntilKiwibeastDamages(inner);
        assertEquals(innerHealth - KiwibeastMuzzles.STAGE3_DAMAGE, inner.getHealth());
        assertEquals(outerHealth, outer.getHealth());
    }

    @Test
    void kiwibeastStageUpKnocksEatingZombieBack() {
        plantKiwibeast(4, 2);
        Zombie eater = placeDummyZombie(4.5, 2, 500);
        eater.setState(ZombieState.EATING);
        double startX = eater.getX();
        session.getBoard().getPlantAt(4, 2).takeDamage(KiwibeastMuzzles.STAGE2_DAMAGE_TAKEN);
        assertEquals(startX + 1.0, eater.getX(), 0.0001);
        assertEquals(ZombieState.MOVING, eater.getState());
    }

    @Test
    void kiwibeastDoesNotBounceWithoutTarget() {
        Plant kiwi = plantKiwibeast(4, 2);
        for (int i = 0; i < 40; i++) {
            session.tick();
        }
        assertFalse(kiwi.isAttacking());
    }

    @Test
    void kiwibeastPlantFoodBouncesThreeTimesAndForcesStageThree() {
        Plant kiwi = plantKiwibeast(4, 2);
        Zombie zombie = placeDummyZombie(4.0, 2, 5000);
        double startX = zombie.getX();
        int startHealth = zombie.getHealth();
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilKiwibeastPlantFoodDone(kiwi);
        assertEquals(3, kiwi.kiwibeastStage());
        assertEquals(startHealth - KiwibeastMuzzles.PLANT_FOOD_DAMAGE * 3, zombie.getHealth());
        assertEquals(startX + 1.0, zombie.getX(), 0.0001);
        assertFalse(kiwi.isPlantFooding());
        assertTrue(kiwi.isAlive());
    }

    @Test
    void wallNutPlantFoodGrantsEightThousandSmashArmor() {
        Plant nut = plantWallNut(4, 2);
        int body = nut.getHealth();
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        assertTrue(nut.hasSmashArmor());
        assertEquals(WallNutPlantFood.DEFAULT_ARMOR, nut.smashArmorHealth());
        nut.takeDamage(WallNutPlantFood.DEFAULT_ARMOR);
        assertEquals(body, nut.getHealth());
        assertFalse(nut.hasSmashArmor());
        nut.takeDamage(1);
        assertEquals(body - 1, nut.getHealth());
        assertTrue(nut.isAlive());
    }

    @Test
    void wallNutPlantFoodReplacesSmashArmorInsteadOfStacking() {
        Plant nut = plantWallNut(4, 2);
        session.setPlantFoodCount(2);
        assertTrue(session.usePlantFood(4, 2));
        nut.takeDamage(1000);
        assertEquals(WallNutPlantFood.DEFAULT_ARMOR - 1000, nut.smashArmorHealth());
        assertTrue(session.usePlantFood(4, 2));
        assertEquals(WallNutPlantFood.DEFAULT_ARMOR, nut.smashArmorHealth());
        int totalArmor = 0;
        for (var layer : nut.getArmorLayers()) {
            if (!layer.isDestroyed()) {
                totalArmor += layer.getHealth();
            }
        }
        assertEquals(WallNutPlantFood.DEFAULT_ARMOR, totalArmor);
    }

    @Test
    void wallNutGargantuarSmashStripsArmorThenKills() {
        Plant nut = plantWallNut(4, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        Zombie garg = smashZombie("ZombieGargantuar", 4.5, 2,
                new TransformBehavior(TransformBehavior.TransformType.SMASH, 1));
        garg.onTickUpdate(context);
        assertTrue(nut.isAlive());
        assertFalse(nut.hasSmashArmor());
        assertEquals(nut.getMaxHealth(), nut.getHealth());
        garg.onTickUpdate(context);
        garg.onTickUpdate(context);
        assertFalse(nut.isAlive());
    }

    @Test
    void wallNutAllStarSmashStripsArmor() {
        Plant nut = plantWallNut(4, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        Zombie allStar = smashZombie("ZombieModernAllStar", 4.5, 2,
                new ContactAttackBehavior(true, 0.5));
        allStar.onTickUpdate(context);
        assertTrue(nut.isAlive());
        assertFalse(nut.hasSmashArmor());
        assertEquals(nut.getMaxHealth(), nut.getHealth());
    }

    @Test
    void wallNutTroglobiteContactInstakillsThroughArmor() {
        Plant nut = plantWallNut(4, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        Zombie troglobite = smashZombie("ZombieIceAgeTroglobite", 4.5, 2,
                new ContactAttackBehavior(false, 1.0));
        troglobite.onTickUpdate(context);
        assertFalse(nut.isAlive());
    }

    @Test
    void wallNutUnarmoredSmashKills() {
        Plant nut = plantWallNut(4, 2);
        Zombie garg = smashZombie("ZombieGargantuar", 4.5, 2,
                new TransformBehavior(TransformBehavior.TransformType.SMASH, 1));
        garg.onTickUpdate(context);
        assertFalse(nut.isAlive());
    }

    @Test
    void wallNutDamageStageFollowsHealthBands() {
        Plant nut = plantWallNut(4, 2);
        assertEquals(0, nut.wallNutDamageStage());
        nut.takeDamage(1000);
        assertEquals(1, nut.wallNutDamageStage());
        nut.takeDamage(1000);
        assertEquals(2, nut.wallNutDamageStage());
        nut.takeDamage(1000);
        assertEquals(3, nut.wallNutDamageStage());
    }

    @Test
    void tallNutDamageStageFollowsHealthBands() {
        Plant nut = plantTallNut(4, 2);
        assertEquals(0, nut.tallNutDamageStage());
        int third = nut.getMaxHealth() / 3 + 1;
        nut.takeDamage(third);
        assertEquals(1, nut.tallNutDamageStage());
        nut.takeDamage(third);
        assertEquals(2, nut.tallNutDamageStage());
    }

    @Test
    void endurianSpikesHitThreeByThree() {
        Plant endurian = plantEndurian(4, 2);
        Zombie inner = placeDummyZombie(5.0, 2, 500);
        Zombie outer = placeDummyZombie(6.0, 2, 500);
        int innerHealth = inner.getHealth();
        int outerHealth = outer.getHealth();
        tickUntilEndurianDamages(inner);
        assertEquals(innerHealth - endurian.getStats().damage(), inner.getHealth());
        assertEquals(outerHealth, outer.getHealth());
        assertInstanceOf(EndurianAbility.class, endurian.getAbility());
    }

    @Test
    void endurianDoesNotSpikeWithoutTarget() {
        Plant endurian = plantEndurian(4, 2);
        for (int i = 0; i < 40; i++) {
            session.tick();
        }
        assertFalse(endurian.isAttacking());
    }

    @Test
    void endurianPlantFoodDoublesSpikeDamageAndGrantsArmor() {
        Plant endurian = plantEndurian(4, 2);
        int body = endurian.getHealth();
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilEndurianPlantFoodDone(endurian);
        assertTrue(endurian.hasSmashArmor());
        assertEquals(EndurianPlantFood.DEFAULT_ARMOR, endurian.smashArmorHealth());
        Zombie inner = placeDummyZombie(5.0, 2, 500);
        int innerHealth = inner.getHealth();
        tickUntilEndurianDamages(inner);
        assertEquals(innerHealth - endurian.getStats().damage() * EndurianMuzzles.ARMORED_DAMAGE_MULTIPLIER,
                inner.getHealth());
        endurian.takeDamage(EndurianPlantFood.DEFAULT_ARMOR);
        assertEquals(body, endurian.getHealth());
        assertFalse(endurian.hasSmashArmor());
    }

    @Test
    void endurianGargantuarSmashStripsArmorThenKills() {
        Plant endurian = plantEndurian(4, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        Zombie garg = smashZombie("ZombieGargantuar", 4.5, 2,
                new TransformBehavior(TransformBehavior.TransformType.SMASH, 1));
        garg.onTickUpdate(context);
        assertTrue(endurian.isAlive());
        assertFalse(endurian.hasSmashArmor());
        assertEquals(endurian.getMaxHealth(), endurian.getHealth());
        garg.onTickUpdate(context);
        garg.onTickUpdate(context);
        assertFalse(endurian.isAlive());
    }

    @Test
    void endurianAllStarContactInstakillsThroughArmor() {
        Plant endurian = plantEndurian(4, 2);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        Zombie allStar = smashZombie("ZombieModernAllStar", 4.5, 2,
                new ContactAttackBehavior(true, 0.5));
        allStar.onTickUpdate(context);
        assertFalse(endurian.isAlive());
    }

    @Test
    void endurianPlantFoodReplacesSmashArmorInsteadOfStacking() {
        Plant endurian = plantEndurian(4, 2);
        session.setPlantFoodCount(2);
        assertTrue(session.usePlantFood(4, 2));
        endurian.takeDamage(1000);
        assertEquals(EndurianPlantFood.DEFAULT_ARMOR - 1000, endurian.smashArmorHealth());
        assertTrue(session.usePlantFood(4, 2));
        assertEquals(EndurianPlantFood.DEFAULT_ARMOR, endurian.smashArmorHealth());
        int totalArmor = 0;
        for (var layer : endurian.getArmorLayers()) {
            if (!layer.isDestroyed()) {
                totalArmor += layer.getHealth();
            }
        }
        assertEquals(EndurianPlantFood.DEFAULT_ARMOR, totalArmor);
    }

    @Test
    void endurianDamageStageFollowsHealthBands() {
        Plant endurian = plantEndurian(4, 2);
        assertEquals(0, endurian.endurianDamageStage());
        endurian.takeDamage(750);
        assertEquals(1, endurian.endurianDamageStage());
        endurian.takeDamage(750);
        assertEquals(2, endurian.endurianDamageStage());
        endurian.takeDamage(750);
        assertEquals(3, endurian.endurianDamageStage());
    }

    @Test
    void phatBeetShockwaveHitsThreeByThreeOnly() {
        Plant beet = plantPhatBeet(4, 2);
        Zombie inner = placeDummyZombie(5.0, 2, 500);
        Zombie outer = placeDummyZombie(6.0, 2, 500);
        int innerHealth = inner.getHealth();
        int outerHealth = outer.getHealth();
        tickUntilPhatBeetDamages(inner);
        assertEquals(innerHealth - 15, inner.getHealth());
        assertEquals(outerHealth, outer.getHealth());
        assertTrue(beet.isAlive());
        assertInstanceOf(PhatBeetAbility.class, beet.getAbility());
    }

    @Test
    void phatBeetCritsOnceInFirstSixAttacks() {
        plantPhatBeet(4, 2);
        Zombie zombie = placeDummyZombie(5.0, 2, 5000);
        int startHealth = zombie.getHealth();
        int hits = 0;
        for (int i = 0; i < 200; i++) {
            int health = zombie.getHealth();
            session.tick();
            if (zombie.getHealth() < health) {
                hits++;
                if (hits == 6) {
                    break;
                }
            }
        }
        assertEquals(6, hits);
        assertEquals(120, startHealth - zombie.getHealth());
    }

    @Test
    void phatBeetPlantFoodHitsInnerAndOuterRings() {
        Plant beet = plantPhatBeet(4, 2);
        Zombie inner = placeDummyZombie(5.0, 2, 5000);
        Zombie outer = placeDummyZombie(6.0, 2, 5000);
        Zombie beyond = placeDummyZombie(7.0, 2, 5000);
        int innerHealth = inner.getHealth();
        int outerHealth = outer.getHealth();
        int beyondHealth = beyond.getHealth();
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilPhatBeetPlantFoodDone(beet);
        assertEquals(innerHealth - PhatBeetMuzzles.INNER_PLANT_FOOD_DAMAGE, inner.getHealth());
        assertEquals(outerHealth - PhatBeetMuzzles.OUTER_PLANT_FOOD_DAMAGE, outer.getHealth());
        assertEquals(beyondHealth, beyond.getHealth());
        assertTrue(beet.isAlive());
        assertFalse(beet.isPlantFooding());
    }

    @Test
    void chomperSwallowsEdibleThenChews() {
        Plant chomper = plantChomper(4, 2);
        Zombie zombie = placeDummyZombie(5.0, 2, 500);
        tickUntilChomperChewing(chomper);
        assertFalse(zombie.isAlive());
        assertTrue(chomper.isChomperChewing());
        assertFalse(chomper.isPlantFooding());
        assertTrue(chomper.isAlive());
    }

    @Test
    void chomperSwallowsTheMomentSpecialStarts() {
        Plant chomper = plantChomper(4, 2);
        Zombie zombie = placeDummyZombie(5.0, 2, 500);
        session.tick();
        assertFalse(zombie.isAlive());
        assertTrue(zombie.isSwallowed());
        assertEquals(ChomperAbility.Phase.SWALLOW, chomper.chomperPhase());
        assertFalse(chomper.isChomperChewing());
    }

    @Test
    void chomperSwallowsSameTileEdible() {
        Plant chomper = plantChomper(4, 2);
        Zombie zombie = placeDummyZombie(4.2, 2, 500);
        tickUntilChomperChewing(chomper);
        assertFalse(zombie.isAlive());
        assertTrue(chomper.isChomperChewing());
        assertFalse(chomper.isPlantFooding());
    }

    @Test
    void chomperPlantFoodDoesNotKillInstantly() {
        Plant chomper = plantChomper(4, 2);
        Zombie edible = placeDummyZombie(5.0, 2, 500);
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        assertTrue(edible.isAlive());
        for (int i = 0; i < 5; i++) {
            session.tick();
        }
        assertTrue(edible.isAlive());
        assertTrue(chomper.isPlantFooding());
        tickUntilChomperPlantFoodDone(chomper);
        assertFalse(edible.isAlive());
        assertFalse(chomper.isPlantFooding());
    }

    @Test
    void chomperSwallowsTwoEdiblesAtOnce() {
        Plant chomper = plantChomper(4, 2);
        Zombie first = placeDummyZombie(4.4, 2, 500);
        Zombie second = placeDummyZombie(5.0, 2, 500);
        tickUntilChomperChewing(chomper);
        assertFalse(first.isAlive());
        assertFalse(second.isAlive());
        assertTrue(first.isSwallowed());
        assertTrue(second.isSwallowed());
        assertTrue(chomper.isChomperChewing());
        assertFalse(chomper.isPlantFooding());
    }

    @Test
    void chomperSwallowsTwoEdiblesOnSameFrontTile() {
        Plant chomper = plantChomper(4, 2);
        Zombie first = placeDummyZombie(5.2, 2, 500);
        Zombie second = placeDummyZombie(5.7, 2, 500);
        tickUntilChomperChewing(chomper);
        assertFalse(first.isAlive());
        assertFalse(second.isAlive());
        assertTrue(first.isSwallowed());
        assertTrue(second.isSwallowed());
        assertTrue(chomper.isChomperChewing());
    }

    @Test
    void chomperPullsFrontTileBeforeZombieEats() {
        Plant chomper = plantChomper(4, 2);
        int startHealth = chomper.getHealth();
        Zombie zombie = new Zombie.Builder("dummy")
                .maxHealth(500)
                .speed(0.5)
                .position(5.4, 2)
                .addBehavior(new MovementBehavior())
                .build();
        zombie.setState(ZombieState.MOVING);
        session.addZombie(zombie);
        tickUntilChomperChewing(chomper);
        assertFalse(zombie.isAlive());
        assertTrue(zombie.isSwallowed());
        assertEquals(startHealth, chomper.getHealth());
    }

    @Test
    void chomperBitesInedibleGargantuar() {
        Plant chomper = plantChomper(4, 2);
        Zombie gargantuar = placeNamedZombie("Gargantuar", 5.0, 2, 5000);
        int startHealth = gargantuar.getHealth();
        tickUntilHealthDrops(gargantuar);
        assertEquals(startHealth - ChomperMuzzles.BITE_DAMAGE, gargantuar.getHealth());
        assertTrue(gargantuar.isAlive());
        assertFalse(chomper.isChomperChewing());
        assertTrue(chomper.isAlive());
    }

    @Test
    void chomperPlantFoodPullsNearestAndBurpsLane() {
        Plant chomper = plantChomper(4, 2);
        Zombie edible = placeDummyZombie(5.0, 2, 500);
        Zombie gargantuar = placeNamedZombie("Gargantuar", 5.5, 2, 5000);
        Zombie third = placeDummyZombie(6.0, 2, 500);
        Zombie otherLane = placeDummyZombie(5.0, 1, 500);
        Zombie survivor = placeDummyZombie(9.0, 2, 500);
        int gargantuarHealth = gargantuar.getHealth();
        int otherLaneHealth = otherLane.getHealth();
        double survivorX = survivor.getX();
        session.setPlantFoodCount(1);
        assertTrue(session.usePlantFood(4, 2));
        tickUntilChomperPlantFoodDone(chomper);
        assertFalse(edible.isAlive());
        assertTrue(edible.isSwallowed());
        assertFalse(third.isAlive());
        assertTrue(third.isSwallowed());
        assertEquals(gargantuarHealth - ChomperMuzzles.PLANT_FOOD_INEDIBLE_DAMAGE, gargantuar.getHealth());
        assertFalse(gargantuar.isSwallowed());
        assertTrue(otherLane.isAlive());
        assertEquals(otherLaneHealth, otherLane.getHealth());
        assertTrue(survivor.isAlive());
        assertTrue(survivor.getX() > survivorX);
        assertFalse(chomper.isPlantFooding());
        assertTrue(chomper.isAlive());
    }

    private Plant plantChomper(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Chomper", col, row, 1));
        Plant chomper = session.getBoard().getPlantAt(col, row);
        assertNotNull(chomper);
        assertInstanceOf(ChomperAbility.class, chomper.getAbility());
        return chomper;
    }

    private Zombie placeNamedZombie(String type, double x, int row, int health) {
        Zombie zombie = new Zombie.Builder(type)
                .maxHealth(health)
                .speed(0)
                .position(x, row)
                .build();
        zombie.setState(ZombieState.MOVING);
        session.addZombie(zombie);
        return zombie;
    }

    private void tickUntilChomperChewing(Plant chomper) {
        int swallowTicks = ChomperMuzzles.phaseTicks(ChomperAbility.Phase.SWALLOW) + 10;
        for (int i = 0; i < swallowTicks; i++) {
            session.tick();
            if (chomper.isChomperChewing()) {
                return;
            }
        }
    }

    private void tickUntilChomperPlantFoodDone(Plant chomper) {
        int ticks = ChomperMuzzles.phaseTicks(ChomperAbility.Phase.PF_ON)
                + ChomperMuzzles.phaseTicks(ChomperAbility.Phase.PF_PULL)
                + ChomperMuzzles.phaseTicks(ChomperAbility.Phase.PF_OFF)
                + ChomperMuzzles.phaseTicks(ChomperAbility.Phase.PF_BURP)
                + ChomperMuzzles.phaseTicks(ChomperAbility.Phase.PF_BURP_END)
                + 30;
        for (int i = 0; i < ticks; i++) {
            session.tick();
            if (chomper == null || !chomper.isAlive()) {
                return;
            }
            if (!chomper.isPlantFooding()) {
                return;
            }
            chomper = session.getBoard().getPlantAt(chomper.getCol(), chomper.getRow());
        }
    }

    private void tickUntilHealthDrops(Zombie zombie) {
        int health = zombie.getHealth();
        for (int i = 0; i < 40; i++) {
            session.tick();
            if (zombie.getHealth() < health) {
                return;
            }
        }
    }

    private Plant plantBonkChoy(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Bonk Choy", col, row, 1));
        Plant bonk = session.getBoard().getPlantAt(col, row);
        assertNotNull(bonk);
        assertInstanceOf(BonkChoyAbility.class, bonk.getAbility());
        return bonk;
    }

    private Plant plantWasabiWhip(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Wasabi Whip", col, row, 1));
        Plant whip = session.getBoard().getPlantAt(col, row);
        assertNotNull(whip);
        assertInstanceOf(WasabiWhipAbility.class, whip.getAbility());
        return whip;
    }

    private void tickUntilWasabiDamages(Zombie zombie) {
        int health = zombie.getHealth();
        for (int i = 0; i < 40; i++) {
            session.tick();
            if (zombie.getHealth() < health) {
                return;
            }
        }
    }

    private void tickUntilWasabiPlantFoodDone(Plant whip) {
        for (int i = 0; i < WasabiWhipMuzzles.plantFoodDurationTicks() + 20; i++) {
            session.tick();
            if (whip == null || !whip.isAlive()) {
                return;
            }
            if (!whip.isPlantFooding()) {
                return;
            }
            whip = session.getBoard().getPlantAt(whip.getCol(), whip.getRow());
        }
    }

    private Plant plantPhatBeet(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Phat Beet", col, row, 1));
        Plant beet = session.getBoard().getPlantAt(col, row);
        assertNotNull(beet);
        assertInstanceOf(PhatBeetAbility.class, beet.getAbility());
        return beet;
    }

    private Plant plantKiwibeast(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Kiwibeast", col, row, 1));
        Plant kiwi = session.getBoard().getPlantAt(col, row);
        assertNotNull(kiwi);
        assertInstanceOf(KiwibeastAbility.class, kiwi.getAbility());
        return kiwi;
    }

    private Plant plantWallNut(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Wall-nut", col, row, 1));
        Plant nut = session.getBoard().getPlantAt(col, row);
        assertNotNull(nut);
        assertTrue(nut.isWallNut());
        return nut;
    }

    private Plant plantTallNut(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Tall-nut", col, row, 1));
        Plant nut = session.getBoard().getPlantAt(col, row);
        assertNotNull(nut);
        assertTrue(nut.isTallNut());
        return nut;
    }

    private Plant plantEndurian(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Endurian", col, row, 1));
        Plant endurian = session.getBoard().getPlantAt(col, row);
        assertNotNull(endurian);
        assertTrue(endurian.isEndurian());
        assertInstanceOf(EndurianAbility.class, endurian.getAbility());
        return endurian;
    }

    private Zombie smashZombie(String type, double x, int row, ZombieBehavior behavior) {
        Zombie zombie = new Zombie.Builder(type)
                .maxHealth(500)
                .speed(0)
                .position(x, row)
                .addBehavior(behavior)
                .build();
        session.addZombie(zombie);
        return zombie;
    }

    private void tickUntilKiwibeastDamages(Zombie zombie) {
        int health = zombie.getHealth();
        for (int i = 0; i < 50; i++) {
            session.tick();
            if (zombie.getHealth() < health) {
                return;
            }
        }
    }

    private void tickUntilKiwibeastPlantFoodDone(Plant kiwi) {
        for (int i = 0; i < KiwibeastMuzzles.plantFoodDurationTicks() + 20; i++) {
            session.tick();
            if (kiwi == null || !kiwi.isAlive()) {
                return;
            }
            if (!kiwi.isPlantFooding()) {
                return;
            }
            kiwi = session.getBoard().getPlantAt(kiwi.getCol(), kiwi.getRow());
        }
    }

    private void tickUntilEndurianDamages(Zombie zombie) {
        int health = zombie.getHealth();
        for (int i = 0; i < 50; i++) {
            session.tick();
            if (zombie.getHealth() < health) {
                return;
            }
        }
    }

    private void tickUntilEndurianPlantFoodDone(Plant endurian) {
        for (int i = 0; i < EndurianMuzzles.plantFoodOnTicks() + 20; i++) {
            session.tick();
            if (endurian == null || !endurian.isAlive()) {
                return;
            }
            if (!endurian.isPlantFooding()) {
                return;
            }
            endurian = session.getBoard().getPlantAt(endurian.getCol(), endurian.getRow());
        }
    }

    private Zombie placeDummyZombie(double x, int row, int health) {
        Zombie zombie = new Zombie.Builder("dummy")
                .maxHealth(health)
                .speed(0)
                .position(x, row)
                .build();
        zombie.setState(ZombieState.MOVING);
        session.addZombie(zombie);
        return zombie;
    }

    private void tickUntilPhatBeetDamages(Zombie zombie) {
        int health = zombie.getHealth();
        for (int i = 0; i < 40; i++) {
            session.tick();
            if (zombie.getHealth() < health) {
                return;
            }
        }
    }

    private void tickUntilPhatBeetPlantFoodDone(Plant beet) {
        for (int i = 0; i < PhatBeetMuzzles.plantFoodDurationTicks() + 20; i++) {
            session.tick();
            if (beet == null || !beet.isAlive()) {
                return;
            }
            if (!beet.isPlantFooding()) {
                return;
            }
            beet = session.getBoard().getPlantAt(beet.getCol(), beet.getRow());
        }
    }

    private void tickUntilBonkDamages(Zombie zombie) {
        int health = zombie.getHealth();
        for (int i = 0; i < 40; i++) {
            session.tick();
            if (zombie.getHealth() < health) {
                return;
            }
        }
    }

    private void tickUntilBonkPlantFoodDone(Plant bonk) {
        for (int i = 0; i < BonkChoyMuzzles.plantFoodDurationTicks() + 20; i++) {
            session.tick();
            if (bonk == null || !bonk.isAlive()) {
                return;
            }
            if (!bonk.isPlantFooding()) {
                return;
            }
            bonk = session.getBoard().getPlantAt(bonk.getCol(), bonk.getRow());
        }
    }

    private Plant plantIcebergLettuce(int col, int row) {
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Iceberg Lettuce", col, row, 1));
        Plant iceberg = session.getBoard().getPlantAt(col, row);
        assertNotNull(iceberg);
        assertInstanceOf(IcebergLettuceAbility.class, iceberg.getAbility());
        assertTrue(iceberg.isArmedTrap());
        return iceberg;
    }

    private void tickUntilIcebergLettuceIdle(Plant iceberg) {
        for (int i = 0; i < IcebergLettuceMuzzles.fullPlantFoodTicks() + 30; i++) {
            session.tick();
            if (iceberg == null || !iceberg.isAlive()) {
                return;
            }
            if (iceberg.getAbility() instanceof IcebergLettuceAbility ability
                    && ability.phase() == IcebergLettuceAbility.Phase.IDLE
                    && !ability.isPlantFoodActive()
                    && !ability.isFreezing()) {
                return;
            }
            iceberg = session.getBoard().getPlantAt(iceberg.getCol(), iceberg.getRow());
        }
    }

    private Plant plantTangleKelp(int col, int row) {
        session.getBoard().setTile(col, row, new LowBeachTile());
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Tangle Kelp", col, row, 1));
        Plant kelp = session.getBoard().getPlantAt(col, row);
        assertNotNull(kelp);
        assertInstanceOf(TangleKelpAbility.class, kelp.getAbility());
        return kelp;
    }

    private void tickUntilTangleKelpIdle(Plant kelp) {
        for (int i = 0; i < TangleKelpMuzzles.fullGrabTicks() + 30; i++) {
            session.tick();
            if (kelp == null || !kelp.isAlive()) {
                return;
            }
            if (kelp.getAbility() instanceof TangleKelpAbility ability
                    && ability.phase() == TangleKelpAbility.Phase.IDLE) {
                return;
            }
            kelp = session.getBoard().getPlantAt(kelp.getCol(), kelp.getRow());
        }
    }

    private void tickUntilDoomShroomDetonates(Plant doom) {
        for (int i = 0; i < 200; i++) {
            if (doom == null || !doom.isAlive()) {
                return;
            }
            session.tick();
            doom = session.getBoard().getPlantAt(doom.getCol(), doom.getRow());
        }
    }

    private int ticksUntilJalapenoIgnite() {
        return JalapenoMuzzles.idleTicks() + JalapenoMuzzles.attackTicks() + 2;
    }

    private int ticksUntilGrapeshotDetonation() {
        return GrapeshotMuzzles.idleTicks() + GrapeshotMuzzles.attackSpawnTicks() + 2;
    }

    private int ticksUntilSquashLandingDamage(boolean plantFood) {
        int jumpUp = Math.max(1, Math.round(SquashAbility.JUMP_UP_SECONDS * GameSession.TICKS_PER_SECOND));
        float downSeconds = plantFood
                ? SquashAbility.PF_JUMP_DOWN_RIGHT_SECONDS
                : SquashAbility.JUMP_DOWN_SECONDS;
        int jumpDown = Math.max(1, Math.round(downSeconds * GameSession.TICKS_PER_SECOND));
        return jumpUp + jumpDown + 2;
    }

    private int ticksUntilSquashReturnHome(boolean plantFood) {
        int turn = Math.max(1, Math.round(SquashAbility.TURN_SECONDS * GameSession.TICKS_PER_SECOND));
        int jumpUp = Math.max(1, Math.round(SquashAbility.JUMP_UP_SECONDS * GameSession.TICKS_PER_SECOND));
        float downSeconds = plantFood
                ? SquashAbility.PF_JUMP_DOWN_LEFT_SECONDS
                : SquashAbility.JUMP_DOWN_SECONDS;
        int jumpDown = Math.max(1, Math.round(downSeconds * GameSession.TICKS_PER_SECOND));
        return turn + jumpUp + jumpDown + 4;
    }

    private void tickUntilSquashIdle(boolean plantFood, int maxTicks) {
        for (int i = 0; i < maxTicks; i++) {
            session.tick();
            Plant squash = findSquashOnBoard();
            if (squash == null) {
                return;
            }
            if (squash.getAbility() instanceof SquashAbility ability
                    && ability.phase() == SquashAbility.Phase.IDLE) {
                return;
            }
        }
    }

    private Plant findSquashOnBoard() {
        for (Plant plant : session.getBoard().getAllPlants()) {
            if (plant.isSquash()) {
                return plant;
            }
        }
        return null;
    }

    private int ticksUntilPeaPodFirstShot(Plant pod) {
        int intervalTicks = (int) Math.round(pod.getStats().actionInterval() * 10);
        return intervalTicks + ProjectileAttackAbility.PEA_POD_MUZZLE_TICKS - 1;
    }

    private int ticksUntilFumeFirstShot(Plant fume) {
        int intervalTicks = (int) Math.round(fume.getStats().actionInterval() * 10);
        return intervalTicks + ProjectileAttackAbility.FUME_MUZZLE_TICKS - 1;
    }

    private int ticksUntilKernelFirstShot(Plant kernel) {
        int intervalTicks = (int) Math.round(kernel.getStats().actionInterval() * 10);
        return intervalTicks + KernelMuzzles.ATTACK_WINDUP_TICKS - 1;
    }

    private int ticksUntilMelonFirstShot(Plant melon) {
        int intervalTicks = (int) Math.round(melon.getStats().actionInterval() * 10);
        return intervalTicks + MelonMuzzles.ATTACK_WINDUP_TICKS - 1;
    }

    private int ticksUntilPepperFirstShot(Plant pepper) {
        int intervalTicks = (int) Math.round(pepper.getStats().actionInterval() * 10);
        return intervalTicks + PepperMuzzles.ATTACK_WINDUP_TICKS - 1;
    }

    private Plant plantStackedPeaPod(int col, int row, int heads) {
        session.addSunBalance(heads * 125);
        assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pea Pod", col, row, 1));
        for (int extra = 1; extra < heads; extra++) {
            session.getCooldownTracker().resetAll();
            assertEquals(PlantPlacementResult.SUCCESS, session.tryPlant("Pea Pod", col, row, 1));
        }
        Plant pod = session.getBoard().getPlantAt(col, row);
        assertNotNull(pod);
        assertEquals(heads, pod.getStackCount());
        return pod;
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

    private void tickUntilSunBurst(int ticks) {
        for (int i = 0; i < ticks; i++) {
            session.tick();
        }
    }

    private int totalSunValue() {
        return session.getSunItems().stream().mapToInt(Sun::getValue).sum();
    }

    private Zombie chewingZombie(double x, int row) {
        Zombie zombie = new Zombie.Builder("dummy")
                .maxHealth(5000)
                .speed(0)
                .damage(100)
                .position(x, row)
                .addBehavior(new MovementBehavior())
                .build();
        zombie.setState(ZombieState.MOVING);
        return zombie;
    }

    private Zombie approachingZombie(double x, int row) {
        Zombie zombie = new Zombie.Builder("dummy")
                .maxHealth(5000)
                .speed(0.3)
                .damage(100)
                .position(x, row)
                .addBehavior(new MovementBehavior())
                .build();
        zombie.setState(ZombieState.MOVING);
        return zombie;
    }

    private Zombie eatingZombie(double x, int row) {
        return chewingZombie(x, row);
    }
}
