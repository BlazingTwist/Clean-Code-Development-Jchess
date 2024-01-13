package jchess.server.api.servlet;

import io.undertow.util.StatusCodes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.server.GameSessionData;
import jchess.server.util.HttpUtils;
import jchess.server.util.SessionUtils;

import java.io.IOException;

public class GameInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getPathInfo();
        if(sessionId.startsWith("/")) {
            sessionId = sessionId.substring(1);
        }

        GameSessionData game = SessionUtils.findGame(sessionId);
        if (game == null) {
            HttpUtils.respond(resp, StatusCodes.NOT_FOUND, "no game found for sessionId");
            return;
        }

        HttpUtils.respondJson(resp, StatusCodes.OK, game.gameInfo);
    }
}
