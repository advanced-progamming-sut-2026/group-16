package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.game.board.BoardGameContext;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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
