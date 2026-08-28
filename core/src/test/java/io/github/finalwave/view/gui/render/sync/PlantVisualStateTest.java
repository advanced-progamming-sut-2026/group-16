package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PlantVisualStateTest {

    private GameSession session;

    @BeforeEach
    void setUp() throws IOException {
        PlantRegistry registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        session = new GameSession(registry, 9990);
        session.start();
    }

    @Test
    void sunflowerSpecialOnSunPulse() {
        session.tryPlant("Sunflower", 1, 1, 1);
        Plant plant = session.getBoard().getPlantAt(1, 1);
        plant.beginSunProduce(10);
        assertArrayEquals(new String[]{"special", "idle"},
                PlantVisualState.preferredClips(plant, false, false, "idle"));
    }

    @Test
    void puffShroomFiresSpecialStage() {
        session.tryPlant("Puff-shroom", 1, 2, 1);
        Plant plant = session.getBoard().getPlantAt(1, 2);
        String[] clips = PlantVisualState.preferredClips(plant, true, false, "idle_stage1");
        assertTrue(clips[0].startsWith("special_stage"));
    }

    @Test
    void peaPodUsesStackClips() {
        session.tryPlant("Pea Pod", 2, 2, 1);
        Plant plant = session.getBoard().getPlantAt(2, 2);
        plant.addStack();
        assertArrayEquals(new String[]{"idle2", "idle"}, PlantVisualState.idleNames(plant));
        String[] fire = PlantVisualState.preferredClips(plant, true, false, "idle2");
        assertTrue(fire[0].startsWith("attack"));
    }

    @Test
    void citronChargesBeforeShot() {
        session.tryPlant("Citron", 1, 3, 1);
        Plant plant = session.getBoard().getPlantAt(1, 3);
        assertTrue(plant.getChargeTicksRemaining() > 0);
        String[] clips = PlantVisualState.preferredClips(plant, false, false, "idle");
        assertTrue(clips[0].equals("charge") || clips[0].equals("idle"));
    }

    @Test
    void cactusDucksWhenZombieOnTile() {
        session.tryPlant("Cactus", 3, 2, 1);
        Plant plant = session.getBoard().getPlantAt(3, 2);
        Zombie zombie = new Zombie.Builder("dummy")
                .maxHealth(200)
                .speed(0)
                .position(3.2, 2)
                .build();
        zombie.setState(ZombieState.MOVING);
        assertTrue(PlantVisualState.cactusDown(plant, List.of(zombie)));
        String[] clips = PlantVisualState.preferredClips(plant, false, true, "idle", List.of(zombie));
        assertTrue(clips[0].startsWith("down"));
    }

    @Test
    void sunflowerPlantFoodPrefersPlantfoodClips() {
        session.tryPlant("Sunflower", 1, 1, 1);
        Plant plant = session.getBoard().getPlantAt(1, 1);
        plant.beginPlantFood(15, 5);
        String[] intro = PlantVisualState.preferredClips(plant, false, false, "idle");
        assertTrue(intro[0].startsWith("plantfood"));
    }

    @Test
    void repeaterPlantFoodUsesPlantfoodThenPlantfood2() {
        session.tryPlant("Repeater", 1, 2, 1);
        Plant plant = session.getBoard().getPlantAt(1, 2);
        plant.beginPlantFood(30, 0, 24);
        String[] barrage = PlantVisualState.preferredClips(plant, false, false, "idle");
        assertTrue(barrage[0].equals("plantfood"));
        for (int i = 0; i < 6; i++) {
            plant.onTickUpdate(session.getContext());
        }
        String[] finale = PlantVisualState.preferredClips(plant, false, false, "plantfood");
        assertTrue(finale[0].equals("plantfood2"));
    }

    @Test
    void snowPeaPlantFoodUsesOnOffClips() {
        session.tryPlant("Snow Pea", 1, 1, 1);
        Plant plant = session.getBoard().getPlantAt(1, 1);
        plant.beginPlantFood(74, 11, 3);
        assertTrue(PlantVisualState.preferredClips(plant, false, false, "idle")[0].equals("plantfood_on"));
        for (int i = 0; i < 12; i++) {
            plant.onTickUpdate(session.getContext());
        }
        assertTrue(PlantVisualState.preferredClips(plant, false, false, "plantfood_on")[0].equals("plantfood"));
    }

    @Test
    void caulipowerPlantFoodUsesStartLoopEnd() {
        session.tryPlant("Caulipower", 1, 1, 1);
        Plant plant = session.getBoard().getPlantAt(1, 1);
        plant.beginPlantFood(44, 4, 4);
        assertTrue(PlantVisualState.preferredClips(plant, false, false, "idle")[0].equals("plantfood_start"));
        for (int i = 0; i < 5; i++) {
            plant.onTickUpdate(session.getContext());
        }
        assertTrue(PlantVisualState.preferredClips(plant, false, false, "plantfood_start")[0]
                .startsWith("plantfood_loop"));
    }

    @Test
    void bowlingBulbReloadClipWhileReloading() {
        session.tryPlant("Bowling Bulb", 1, 0, 1);
        Plant plant = session.getBoard().getPlantAt(1, 0);
        plant.setBowlingAmmo(3);
        plant.setBowlingReloading(true);
        String[] clips = PlantVisualState.preferredClips(plant, false, false, "idle");
        assertTrue(clips[0].equals("reload3"));
    }

    @Test
    void sunShroomPlantFoodUsesMaxStageClip() {
        session.tryPlant("Sun-shroom", 1, 1, 1);
        Plant plant = session.getBoard().getPlantAt(1, 1);
        plant.beginPlantFood(25, 0);
        assertArrayEquals(new String[]{"plantfood_stage3", "idle_stage3"},
                PlantVisualState.preferredClips(plant, false, false, "idle"));
    }

    @Test
    void citronRecoveryClipAfterFire() {
        session.tryPlant("Citron", 1, 3, 1);
        Plant plant = session.getBoard().getPlantAt(1, 3);
        plant.setRecoveryTicksRemaining(10);
        assertArrayEquals(new String[]{"recovery", "idle"},
                PlantVisualState.preferredClips(plant, false, false, "idle"));
    }

    @Test
    void bowlingBulbFiresSpecial() {
        session.tryPlant("Bowling Bulb", 1, 0, 1);
        Plant plant = session.getBoard().getPlantAt(1, 0);
        plant.setChargeTicksRemaining(0);
        String[] clips = PlantVisualState.preferredClips(plant, true, false, "idle");
        assertTrue(clips[0].equals("special") || clips[0].equals("attack"));
    }
}
