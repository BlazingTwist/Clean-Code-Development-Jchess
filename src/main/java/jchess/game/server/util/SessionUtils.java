package jchess.game.server.util;

import jchess.game.common.IChessGame;
import jchess.game.server.GameSessionData;
import jchess.game.server.session.SessionManager;
import jchess.game.server.session.SessionMgrController;

public class SessionUtils {
    public static IChessGame findGame(String sessionId) {
        SessionManager<GameSessionData> gameManager = SessionMgrController.lookupSessionManager(GameSessionData.class);
        SessionManager.Session<GameSessionData> session = gameManager.getSession(sessionId);
        return session.sessionData.game();
    }
}
