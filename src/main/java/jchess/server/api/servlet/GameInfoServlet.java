package jchess.server.api.servlet;

import io.undertow.util.StatusCodes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.server.GameSessionData;
import jchess.server.util.HttpUtils;
import jchess.server.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

public class GameInfoServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(GameInfoServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BufferedReader bodyReader = new BufferedReader(req.getReader());
        String sessionId = bodyReader.readLine();
        bodyReader.close();

        if (sessionId == null) {
            logger.warn("received sessionId is null");
            HttpUtils.respond(resp, StatusCodes.NOT_FOUND, "sessionId is null");
            return;
        }

        GameSessionData game = SessionUtils.findGame(sessionId);
        if (game == null) {
            HttpUtils.respond(resp, StatusCodes.NOT_FOUND, "no game found for sessionId");
            return;
        }

        HttpUtils.respondJson(resp, StatusCodes.OK, game.gameInfo);
    }
}
