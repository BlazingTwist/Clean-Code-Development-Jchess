package jchess.common;

import jchess.ecs.EcsEventManager;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;

public interface IChessGame {
    EntityManager getEntityManager();

    EcsEventManager getEventManager();

    int getActivePlayerId();

    int getKingTypeId();

    void start();

    void movePiece(Entity fromTile, Entity toTile, Class<?> moveType);
}
