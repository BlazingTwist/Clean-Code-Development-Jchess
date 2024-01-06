package jchess.server;

import jchess.common.IChessGame;
import jchess.server.api.socket.BoardUpdateWebsocket;
import jchess.server.api.socket.ChatWebsocket;
import jchess.server.api.socket.PieceSelectionWebsocket;
import jchess.server.session.ISessionData;

import java.io.IOException;

public class GameSessionData implements ISessionData {
    public final IChessGame game;
    public final ChatWebsocket.ChatHandler chatHandler = new ChatWebsocket.ChatHandler();
    public final PieceSelectionWebsocket.PieceSelectionHandler pieceSelectionHandler;
    public final BoardUpdateWebsocket.Handler boardUpdateHandler = new BoardUpdateWebsocket.Handler();

    public GameSessionData(IChessGame game) {
        this.game = game;
        pieceSelectionHandler = new PieceSelectionWebsocket.PieceSelectionHandler(game);
    }

    @Override
    public boolean isStillUsed() {
        return false;
    }

    @Override
    public void close() throws IOException {
        chatHandler.close();
        boardUpdateHandler.close();
    }
}
