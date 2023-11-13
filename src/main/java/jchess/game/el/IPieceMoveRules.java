package jchess.game.el;

import jchess.ecs.Entity;

import java.util.stream.Stream;

public interface IPieceMoveRules {
    Stream<Entity> findValidMoves(Entity movingEntity);
}
