package jchess.game.server;

import dx.schema.message.GameClicked;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.ecs.Entity;
import jchess.game.server.adapter.Vector2IAdapter;
import jchess.game.server.session.SessionManager;
import jchess.game.server.session.SessionMgrController;
import jchess.game.server.util.JsonUtils;

import java.io.IOException;

public class BoardClickedServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GameClicked clickInfo = JsonUtils.getMapper().readValue(req.getReader(), GameClicked.class);
        SessionManager<GameSessionData> gameManager = SessionMgrController.lookupSessionManager(GameSessionData.class);
        SessionManager.Session<GameSessionData> session = gameManager.getSession(clickInfo.getSessionId());

        GameSessionData gameData = session.sessionData;
        Entity clickedEntity = gameData.gameState().getByPosition(Vector2IAdapter.toPoint(clickInfo.getClickPos()));
        gameData.clickListener().onClick(clickedEntity);
    }
}
