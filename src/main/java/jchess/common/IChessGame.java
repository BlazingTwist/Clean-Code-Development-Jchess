package jchess.common;

import dx.schema.types.PieceType;
import jchess.common.state.StateManager;
import jchess.ecs.EcsEventManager;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;

public interface IChessGame {
    EntityManager getEntityManager();

    EcsEventManager getEventManager();

    StateManager getStateManager();

    int getActivePlayerId();

    int getNumPlayers();

    void start();

    void createPiece(Entity targetTile, PieceType pieceType, int ownerId);

    void notifyPieceMove(Entity fromTile, Entity toTile, Class<?> moveType);

    void endTurn();

    dx.schema.types.Entity applyPerspective(dx.schema.types.Entity tile, int playerIndex);
}
