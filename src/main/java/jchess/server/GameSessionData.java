package jchess.server;

import jchess.common.IChessGame;
import jchess.server.api.socket.ChatWebsocket;
import jchess.server.session.ISessionData;

import java.io.IOException;

public class GameSessionData implements ISessionData {
    public final IChessGame game;
    public final ChatWebsocket.ChatHandler chatHandler = new ChatWebsocket.ChatHandler();

    public GameSessionData(IChessGame game) {
        this.game = game;
    }

    @Override
    public boolean isStillUsed() {
        return false;
    }

    @Override
    public void close() throws IOException {
        chatHandler.close();
    }
}
