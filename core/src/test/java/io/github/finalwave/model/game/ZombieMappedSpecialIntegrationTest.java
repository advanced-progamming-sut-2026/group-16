package io.github.finalwave.model.game;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantFactory;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.zombie.ArcadeObstacle;
import io.github.finalwave.model.game.entity.zombie.PianoObstacle;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieFactory;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.model.item.SunType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ZombieMappedSpecialIntegrationTest {

    private PlantRegistry plants;
    private ZombieRegistry zombieDefinitions;
    private ZombieFactory zombies;
    private PlantFactory plantFactory;

    @BeforeEach
    void loadDefinitions() throws IOException {
        plants = new PlantRegistry();
        plants.loadFromJson("src/main/resources/plants.json");
        zombieDefinitions = new ZombieRegistry();
        zombieDefinitions.loadFromJson("src/test/resources/zombies.json");
        zombieDefinitions.loadArmorFromJson("src/test/resources/ArmorTypeData.json");
        zombies = new ZombieFactory(zombieDefinitions);
        plantFactory = new PlantFactory();
    }

    @Test
    void raAndExplorerPerformTheirWorldInteractions() {
        GameSession raSession = session();
        raSession.spawnSunItem(new Sun(1, 1, 100, SunType.NORMAL, false));
        Zombie ra = zombies.createZombie("ZombieRa", 8, 1);
        raSession.addZombie(ra);
        run(raSession, 30);
        assertTrue(raSession.getSunItems().isEmpty());
        ra.takeDirectDamage(ra.getHealth());
        assertEquals(150, raSession.getSunBalance());

        GameSession explorerSession = session();
        Plant target = place(explorerSession, "Peashooter", 3, 1);
        explorerSession.addZombie(zombies.createZombie("ZombieExplorer", 3.5, 1));
        run(explorerSession, 1);
        assertTrue(target.isDead());
    }

    @Test
    void tombRaiserDodoAndTroglobiteChangeTheBoard() {
        GameSession tombSession = session();
        tombSession.addZombie(zombies.createZombie("ZombieTombRaiser", 8, 2));
        tombSession.start();
        for (int i = 0; i < 60; i++) {
            tombSession.tick();
        }
        assertEquals(0, countGraves(tombSession.getBoard()));
        assertFalse(tombSession.getPendingGraveLandings().isEmpty());
        for (int i = 0; i < 35; i++) {
            tombSession.tick();
        }
        assertTrue(countGraves(tombSession.getBoard()) >= 2);

        GameSession dodoSession = session();
        Plant wall = place(dodoSession, "Wall-nut", 3, 1);
        Zombie dodo = zombies.createZombie("ZombieIceAgeDodo", 3.5, 1);
        dodoSession.addZombie(dodo);
        run(dodoSession, 1);
        assertTrue(wall.isAlive());
        assertTrue(dodo.getX() < 3.5);

        GameSession iceSession = session();
        iceSession.addZombie(zombies.createZombie("ZombieIceAgeTroglobite", 8, 1));
        run(iceSession, 1);
        long ice = java.util.stream.IntStream.range(0, iceSession.getBoard().getCols())
                .filter(col -> iceSession.getBoard().getTile(col, 1).isIce()).count();
        assertEquals(3, ice);
        assertEquals("push", iceSession.getZombies().getFirst().getPresentationClip());
    }

    @Test
    void fishermanHunterAndKingUseValidTargets() {
        GameSession fishSession = session();
        Plant hooked = place(fishSession, "Peashooter", 3, 1);
        fishSession.addZombie(zombies.createZombie("ZombieBeachFisherman", 8, 1));
        run(fishSession, 1);
        assertEquals(4, hooked.getCol());

        GameSession hunterSession = session();
        place(hunterSession, "Peashooter", 3, 1);
        hunterSession.addZombie(zombies.createZombie("ZombieIceAgeHunter", 6, 1));
        run(hunterSession, 1);
        assertTrue(hunterSession.getProjectileSystem().getProjectiles().stream()
                .anyMatch(projectile -> projectile.getEffect() == ProjectileEffect.SNOWBALL));

        GameSession kingSession = session();
        Zombie king = zombies.createZombie("ZombieDarkKing", 8, 2);
        Zombie basic = zombies.createZombie("ZombieDarkArmor3", 8, 2);
        Zombie special = zombies.createZombie("ZombieDarkJuggler", 8, 2);
        kingSession.addZombie(king);
        kingSession.addZombie(basic);
        kingSession.addZombie(special);
        run(kingSession, 1);
        assertTrue(basic.hasArmorAlias("KingKnightHelm"));
        assertFalse(special.hasArmorAlias("KingKnightHelm"));
        assertFalse(king.hasArmorAlias("KingKnightHelm"));
    }

    @Test
    void janePianoAndArcadeHaveDistinctRuntimeEffects() {
        GameSession janeSession = session();
        Zombie jane = zombies.createZombie("ZombieLostCityJane", 5, 1);
        Projectile lobbed = new Projectile(1, 4.8, 10, ProjectileProfile.arcing(),
                ProjectileEffect.GENERIC, null, 0);
        Projectile straight = new Projectile(1, 4.8, 10, ProjectileProfile.straight(),
                ProjectileEffect.PEA, null, 0);
        assertTrue(jane.interceptProjectile(lobbed, janeSession.getContext()));
        assertFalse(jane.interceptProjectile(straight, janeSession.getContext()));

        GameSession pianoSession = session();
        Zombie piano = zombies.createZombie("ZombiePiano", 7, 2);
        Zombie nearby = zombies.createZombie("ZombieDefault", 6.5, 2);
        pianoSession.addZombie(piano);
        pianoSession.addZombie(nearby);
        run(pianoSession, 1);
        assertNotEquals(2, nearby.getRow());
        PianoObstacle instrument = pianoSession.getPianoObstacles().getFirst();
        assertEquals(piano.getX() - PianoObstacle.FOLLOW_OFFSET, instrument.getX(), 0.0001);
        assertEquals(piano.getRow(), instrument.getRow());

        GameSession arcadeSession = session();
        Zombie arcade = zombies.createZombie("ZombieArcade", 5, 1);
        arcadeSession.addZombie(arcade);
        run(arcadeSession, 1);
        ArcadeObstacle machine = arcadeSession.getArcadeObstacles().getFirst();
        assertTrue(arcade.getArmorLayers().isEmpty());
        assertEquals(ArcadeObstacle.BUCKET_EQUIVALENT_HEALTH, machine.getHealth());
        assertEquals(arcade.getX() - ArcadeObstacle.FOLLOW_OFFSET, machine.getX(), 0.0001);
        assertEquals("push", arcade.getPresentationClip());
    }

    @Test
    void allStarAndCrystalSkullExecuteActionsRatherThanOnlyMapClasses() {
        GameSession allStarSession = session();
        Plant target = place(allStarSession, "Peashooter", 3, 1);
        Zombie allStar = zombies.createZombie("ZombieModernAllStar", 3.5, 1);
        double initialSpeed = allStar.getCurrentSpeed();
        allStarSession.addZombie(allStar);
        run(allStarSession, 1);
        assertTrue(target.isDead());
        assertEquals(initialSpeed * 0.5, allStar.getCurrentSpeed(), 0.0001);

        GameSession crystalSession = session();
        Plant laserTarget = place(crystalSession, "Peashooter", 4, 1);
        crystalSession.addZombie(zombies.createZombie("ZombieCrystalSkull", 8, 1));
        run(crystalSession, 70);
        assertTrue(laserTarget.isDead());
        assertTrue(crystalSession.getSunBalance() < 50);
    }

    @Test
    void octopusCoversThePlantAndWizardSheepRestoresOnDeath() {
        GameSession octoSession = session();
        Plant bound = place(octoSession, "Peashooter", 3, 1);
        octoSession.addZombie(zombies.createZombie("ZombieBeachOctopus", 6, 1));
        run(octoSession, 1);
        assertTrue(octoSession.getPlantCoverings().stream().anyMatch(covering ->
                covering.getType() == PlantCovering.Type.OCTOPUS
                        && covering.getCoveredPlant() == bound));
        assertTrue(bound.isDisabled());
    }

    private GameSession session() {
        return new GameSession(plants, new GameBoard(), 50,
                zombieDefinitions, 1, new Random(7));
    }

    private Plant place(GameSession session, String name, int col, int row) {
        Plant plant = plantFactory.createBaseLevel(plants.getDefinition(name), col, row);
        session.getBoard().placePlant(plant);
        plant.onPlanted(session.getContext());
        return plant;
    }

    private static void run(GameSession session, int ticks) {
        session.start();
        for (int i = 0; i < ticks; i++) {
            session.tick();
        }
    }

    private static long countGraves(GameBoard board) {
        long result = 0;
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                if (board.getTile(col, row).isGrave()) {
                    result++;
                }
            }
        }
        return result;
    }
}
