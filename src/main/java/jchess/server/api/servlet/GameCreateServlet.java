package jchess.server.api.servlet;

import dx.schema.types.GameInfo;
import io.undertow.util.StatusCodes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.server.JChessServer;
import jchess.server.util.HttpUtils;
import jchess.server.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GameCreateServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(GameCreateServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GameInfo createInfo = JsonUtils.getMapper().readValue(req.getReader(), GameInfo.class);

        final String sessionId;
        try {
            sessionId = JChessServer.startNewGame(createInfo);
        } catch (Exception e) {
            logger.warn("Failed to start new game.", e);
            HttpUtils.respond(resp, StatusCodes.INTERNAL_SERVER_ERROR, "Failed to start new game. Exception: " + e.getMessage());
            return;
        }

        HttpUtils.respond(resp, StatusCodes.CREATED, sessionId);
    }
}
