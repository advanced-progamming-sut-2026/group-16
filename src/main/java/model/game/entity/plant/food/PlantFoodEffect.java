package model.game.entity.plant.food;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;

public interface PlantFoodEffect {

    void apply(Plant plant, GameContext context);
}
