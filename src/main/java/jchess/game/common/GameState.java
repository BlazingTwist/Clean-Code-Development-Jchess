package jchess.game.common;

import jchess.ecs.Entity;

import java.awt.Point;
import java.util.function.BiFunction;

public class GameState {
    private final BiFunction<Integer, Integer, Entity> entityGetter;
    private int activePlayerId;

    public GameState(BiFunction<Integer, Integer, Entity> entityGetter) {
        this.entityGetter = entityGetter;
    }

    public void nextPlayer() {
        this.activePlayerId = (activePlayerId + 1) % 3;
    }

    public int activePlayerId() {
        return activePlayerId;
    }

    public Entity getByPosition(Point position) {
        return entityGetter.apply(position.x, position.y);
    }
}
