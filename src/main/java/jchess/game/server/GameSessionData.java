package jchess.game.server;

import jchess.ecs.EcsEvent;
import jchess.ecs.EntityManager;
import jchess.game.common.BoardClickedListener;
import jchess.game.common.GameState;
import jchess.game.server.session.ISessionData;

import java.io.IOException;

public record GameSessionData(
        GameState gameState,
        EntityManager entityManager,
        EcsEvent<?> onBoardChangedEvent,
        BoardClickedListener clickListener
) implements ISessionData {
    @Override
    public boolean isStillUsed() {
        return false;
    }

    @Override
    public void close() throws IOException {
    }
}
