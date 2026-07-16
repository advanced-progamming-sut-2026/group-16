package model.game;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.board.GameBoard;
import model.game.board.tile.LowBeachTile;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantCovering;
import model.game.entity.plant.PlantFactory;
import model.game.entity.projectile.Projectile;
import model.game.entity.projectile.ProjectileEffect;
import model.game.entity.projectile.ProjectileProfile;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ArcadeObstacle;
import model.game.entity.zombie.ZombieBehavior;
import model.game.entity.zombie.ZombieState;
import model.game.entity.zombie.behavior.MovementBehavior;
import model.game.entity.zombie.behavior.PlantControlBehavior;
import model.game.entity.zombie.behavior.ProjectileDefenseBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ZombieRuntimeIntegrationTest {

    private PlantRegistry plantRegistry;
    private PlantFactory plantFactory;

    @BeforeEach
    void loadPlants() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        plantFactory = new PlantFactory();
    }

    @Test
    void summonUsesSessionFactoryPositionAndDifficulty() throws IOException {
        ZombieRegistry zombies = new ZombieRegistry();
        zombies.loadFromJson("src/test/resources/zombies.json");
        zombies.loadArmorFromJson("src/test/resources/ArmorTypeData.json");
        GameSession session = new GameSession(
                plantRegistry, new GameBoard(), 50, zombies, 3);
        Zombie summoner = session.spawnZombieOfType("ZombieGargantuar", 2, 8);
        summoner.takeDirectDamage(summoner.getHealth() / 2 + 1);

        session.start();
        session.tick();

        Zombie imp = session.getZombies().stream()
                .filter(zombie -> zombie != summoner)
                .findFirst().orElseThrow();
        int baseHealth = zombies.getDefinition("ZombieImp").getHitpoints();
        assertEquals("ZombieImp", imp.getType());
        assertEquals(2.0, imp.getX(), 0.0001);
        assertEquals(2, imp.getRow());
        assertEquals((int) Math.round(baseHealth * 1.2), imp.getMaxHealth());
    }

    @Test
    void preTickDeathRunsCleanupExactlyOnce() {
        AtomicInteger deaths = new AtomicInteger();
        ZombieBehavior cleanup = new ZombieBehavior() {
            @Override
            public void execute(Zombie zombie, model.game.entity.GameContext context) {
            }

            @Override
            public void onDeath(Zombie zombie, model.game.entity.GameContext context) {
                deaths.incrementAndGet();
            }
        };
        GameSession session = new GameSession(plantRegistry);
        Zombie zombie = new Zombie.Builder("fragile")
                .maxHealth(1).addBehavior(cleanup).build();
        session.addZombie(zombie);

        zombie.takeDirectDamage(1);
        session.handleZombieKilled(zombie);
        session.start();
        session.tick();

        assertEquals(1, deaths.get());
        assertTrue(session.getZombies().isEmpty());
    }

    @Test
    void hunterHitsAggregateAcrossShootersAndCreateDamageableIceCovering() {
        GameSession session = new GameSession(plantRegistry);
        Plant plant = placePlant(session, "Peashooter", 2, 1);
        session.start();

        for (int hit = 1; hit <= 3; hit++) {
            Zombie hunter = new Zombie.Builder("hunter-" + hit).build();
            session.getContext().spawnProjectile(hunter, 1, 3.0, 0, "snowball");
            session.tick();
        }
        assertEquals(0, plant.getHostileIceStacks("any-hunter"));
        assertTrue(plant.isDisabled());
        PlantCovering covering = assertInstanceOf(PlantCovering.class,
                session.getPlantCoverings().getFirst());
        assertEquals(PlantCovering.Type.HUNTER_ICE, covering.getType());
        assertEquals(600, covering.getMaxHealth());

        Plant source = placePlant(session, "Peashooter", 0, 1);
        session.getProjectileSystem().spawn(new Projectile(
                1, 1.7, 1, ProjectileProfile.straight(),
                ProjectileEffect.FIRE, source, 0));
        session.tick();
        assertTrue(session.getPlantCoverings().isEmpty());
        assertFalse(plant.isDisabled());
    }

    @Test
    void jesterReflectionReturnsAnIceProjectileToPlants() {
        GameSession session = new GameSession(plantRegistry);
        Plant source = placePlant(session, "Peashooter", 1, 1);
        Zombie jester = new Zombie.Builder("jester")
                .position(3, 1)
                .addBehavior(new ProjectileDefenseBehavior(
                        ProjectileDefenseBehavior.Mode.JESTER, 1.1))
                .build();
        session.addZombie(jester);
        session.getProjectileSystem().spawn(new Projectile(
                1, 2.7, 7, ProjectileProfile.straight(),
                ProjectileEffect.ICE, source, 0));
        int healthBefore = source.getHealth();

        session.start();
        session.tick();
        assertEquals(healthBefore, source.getHealth());
        assertTrue(session.getProjectileSystem().getProjectiles().stream()
                .anyMatch(Projectile::isFromZombie));
        for (int i = 0; i < 5; i++) {
            session.tick();
        }

        assertEquals(healthBefore - 7, source.getHealth());
        assertEquals(0, source.getHostileIceStacks(jester.getId()),
                "reflected ice is not a Hunter snowball");
    }

    @Test
    void wizardCleanupDoesNotRemoveOctopusCovering() {
        GameSession session = new GameSession(plantRegistry);
        Plant plant = placePlant(session, "Peashooter", 2, 1);
        Zombie wizard = new Zombie.Builder("wizard").maxHealth(1).position(4, 1)
                .addBehavior(new PlantControlBehavior(
                        PlantControlBehavior.Mode.WIZARD, 1, 5)).build();
        Zombie octopus = new Zombie.Builder("octopus").maxHealth(1).position(4, 1)
                .addBehavior(new PlantControlBehavior(
                        PlantControlBehavior.Mode.OCTOPUS, 1, 5)).build();
        session.addZombie(octopus);
        session.addZombie(wizard);
        session.start();
        session.tick();
        assertTrue(plant.isDisabledBy(wizard.getId()));
        PlantCovering covering = session.getPlantCoverings().getFirst();
        assertEquals(PlantCovering.Type.OCTOPUS, covering.getType());

        wizard.takeDirectDamage(1);
        octopus.takeDirectDamage(1);

        assertFalse(plant.isDisabledBy(wizard.getId()));
        assertTrue(covering.isAlive());
        assertTrue(plant.isDisabled());

        covering.takeDamage(covering.getHealth());
        assertFalse(plant.isDisabled());
    }

    @Test
    void arcadeMachineIsIndependentAndSurvivesItsPusher() {
        GameSession session = new GameSession(plantRegistry);
        Plant target = placePlant(session, "Peashooter", 3, 1);
        Zombie arcade = new Zombie.Builder("arcade").maxHealth(1).position(3.5, 1)
                .addBehavior(new model.game.entity.zombie.behavior.ArcadePushBehavior())
                .build();
        session.addZombie(arcade);
        session.start();
        session.tick();

        ArcadeObstacle machine = session.getArcadeObstacles().getFirst();
        assertTrue(target.isDead());
        assertEquals(ArcadeObstacle.BUCKET_EQUIVALENT_HEALTH, machine.getHealth());
        assertTrue(arcade.getArmorLayers().isEmpty());

        arcade.takeDirectDamage(1);
        assertNull(machine.getPusherId());
        assertTrue(machine.isAlive());
        session.tick();
        assertTrue(session.getArcadeObstacles().contains(machine));
    }

    @Test
    void tombRaiserUsesInjectedRandomForDistinctEmptyCells() {
        GameSession first = new GameSession(plantRegistry, new GameBoard(), 50,
                (model.game.entity.zombie.ZombieFactory) null, 1, new Random(17));
        GameSession second = new GameSession(plantRegistry, new GameBoard(), 50,
                (model.game.entity.zombie.ZombieFactory) null, 1, new Random(17));

        first.getContext().createGraves(4);
        second.getContext().createGraves(4);

        for (int row = 0; row < first.getBoard().getRows(); row++) {
            for (int col = 0; col < first.getBoard().getCols(); col++) {
                assertEquals(first.getBoard().getTile(col, row).isGrave(),
                        second.getBoard().getTile(col, row).isGrave());
            }
        }
        long graves = java.util.stream.IntStream.range(0, first.getBoard().getRows())
                .boxed().flatMap(row -> java.util.stream.IntStream
                        .range(0, first.getBoard().getCols())
                        .mapToObj(col -> first.getBoard().getTile(col, row)))
                .filter(tile -> tile.isGrave()).count();
        assertEquals(4, graves);
    }

    @Test
    void snorkelUsesWaterAndSurfacesWhileEating() {
        GameBoard board = new GameBoard();
        board.setTile(3, 1, new LowBeachTile());
        GameSession session = new GameSession(plantRegistry, board, 50);
        Zombie snorkel = new Zombie.Builder("snorkel").position(3.5, 1)
                .addBehavior(new ProjectileDefenseBehavior(
                        ProjectileDefenseBehavior.Mode.SNORKEL, 1.0))
                .addBehavior(new MovementBehavior())
                .build();
        session.addZombie(snorkel);
        Projectile straight = new Projectile(1, 2, 1, ProjectileProfile.straight(),
                ProjectileEffect.PEA, null, 0);
        session.start();
        session.tick();
        assertTrue(snorkel.isSubmerged());
        assertTrue(snorkel.interceptProjectile(straight, session.getContext()));

        placePlant(session, "Sea-shroom", 3, 1);
        session.tick();

        assertEquals(ZombieState.EATING, snorkel.getState());
        assertFalse(snorkel.isSubmerged());
        assertFalse(snorkel.interceptProjectile(straight, session.getContext()));
    }

    private Plant placePlant(GameSession session, String name, int col, int row) {
        Plant plant = plantFactory.createBaseLevel(
                plantRegistry.getDefinition(name), col, row);
        session.getBoard().placePlant(plant);
        plant.onPlanted(session.getContext());
        return plant;
    }
}
