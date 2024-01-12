package jchess.common;

import dx.schema.types.PieceType;
import jchess.ecs.EcsEventManager;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;

public interface IChessGame {
    EntityManager getEntityManager();

    EcsEventManager getEventManager();

    int getActivePlayerId();

    void start();

    void createPiece(Entity targetTile, PieceType pieceType, int ownerId);

    void movePiece(Entity fromTile, Entity toTile, Class<?> moveType);

    dx.schema.types.Entity applyPerspective(dx.schema.types.Entity tile, int playerIndex);
}
