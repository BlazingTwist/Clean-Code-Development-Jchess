package jchess.game.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.game.layout.hex3p.Hex3PlayerGame;
import jchess.game.server.session.SessionManager;
import jchess.game.server.session.SessionMgrController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class GameCreateServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO erja get theme from frontend
        Hex3PlayerGame game = new Hex3PlayerGame(WipExampleServer.theme);
        game.start();

        String sessionId = UUID.randomUUID().toString();
        GameSessionData gameData = game.getSessionData();
        SessionManager<GameSessionData> gameManager = SessionMgrController.lookupSessionManager(GameSessionData.class);
        gameManager.createSession(sessionId, gameData);
        WipExampleServer.boardUpdateWebsocket.registerGame(sessionId, gameData);

        PrintWriter writer = resp.getWriter();
        writer.write(sessionId);
        writer.flush();
        writer.close();
    }
}
