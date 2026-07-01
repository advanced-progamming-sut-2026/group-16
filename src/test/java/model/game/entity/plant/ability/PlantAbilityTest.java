package model.game.entity.plant.ability;

import model.definition.PlantRegistry;
import model.game.board.BoardGameContext;
import model.game.GameSession;
import model.game.board.PlantPlacementResult;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PlantAbilityTest {

    private PlantRegistry registry;
    private PlantFactory factory;
    private GameSession session;
    private BoardGameContext context;

    @BeforeEach
    void setUp() throws IOException {
        registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        factory = new PlantFactory();
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
    void actionIntervalUsesSecondsNotTicks() {
        session.tryPlant("Peashooter", 1, 2, 1);
        for (int i = 0; i < 14; i++) {
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
        for (int i = 0; i < 15; i++) {
            session.tick();
        }
        assertEquals(model.game.entity.projectile.ProjectileEffect.FIRE,
                session.getProjectileSystem().getProjectiles().getFirst().getEffect());
    }
}
