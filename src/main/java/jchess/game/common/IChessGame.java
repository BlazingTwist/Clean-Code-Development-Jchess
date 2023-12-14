package jchess.game.common;

import jchess.ecs.EcsEventManager;
import jchess.ecs.EntityManager;

public interface IChessGame {
    EntityManager getEntityManager();

    EcsEventManager getEventManager();

    int getActivePlayerId();

    void start();
}
