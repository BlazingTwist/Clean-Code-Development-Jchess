package jchess.common.events;

import jchess.ecs.EcsEvent;
import jchess.ecs.Entity;

public class PieceMoveEvent extends EcsEvent<PieceMoveEvent.PieceMove> {
    public record PieceMove(Entity fromTile, Entity toTile, Class<?> moveType) {
    }
}
