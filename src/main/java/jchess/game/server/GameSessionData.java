package jchess.game.server;

import jchess.game.common.IChessGame;
import jchess.game.server.session.ISessionData;

import java.io.IOException;

public record GameSessionData(IChessGame game) implements ISessionData {
    @Override
    public boolean isStillUsed() {
        return false;
    }

    @Override
    public void close() throws IOException {
    }
}
