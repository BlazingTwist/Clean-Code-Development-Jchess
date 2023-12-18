package jchess.server.util;

import jchess.common.IChessGame;
import jchess.server.GameSessionData;
import jchess.server.session.SessionManager;
import jchess.server.session.SessionMgrController;

public class SessionUtils {
    public static IChessGame findGame(String sessionId) {
        SessionManager<GameSessionData> gameManager = SessionMgrController.lookupSessionManager(GameSessionData.class);
        SessionManager.Session<GameSessionData> session = gameManager.getSession(sessionId);
        return session.sessionData.game();
    }
}
