package model.game.entity.zombie;

import model.game.entity.GameContext;

public interface ZombieBehavior {

    void execute(Zombie zombie, GameContext context);
}