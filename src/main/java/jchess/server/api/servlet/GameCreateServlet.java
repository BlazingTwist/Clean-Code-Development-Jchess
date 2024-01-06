package jchess.server.api.servlet;

import dx.schema.message.GameCreate;
import io.undertow.util.StatusCodes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.gamemode.GameMode;
import jchess.server.WipExampleServer;
import jchess.server.util.HttpUtils;
import jchess.server.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

public class GameCreateServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(GameCreateServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GameCreate createInfo = JsonUtils.getMapper().readValue(req.getReader(), GameCreate.class);

        final GameMode gameMode;
        try {
            gameMode = GameMode.valueOf(createInfo.getModeId());
        } catch (Exception e) {
            logger.warn("Failed to find GameMode with id: '{}'", createInfo.getModeId());
            HttpUtils.error(resp, StatusCodes.BAD_REQUEST, "Invalid Game-Mode Id");
            return;
        }

        final String sessionId;
        try {
            sessionId = WipExampleServer.startNewGame(gameMode);
        } catch (Exception e) {
            logger.warn("Failed to start new game.", e);
            HttpUtils.error(resp, StatusCodes.INTERNAL_SERVER_ERROR, "Failed to start new game. Exception: " + e.getMessage());
            return;
        }

        // TODO erja, rename 'error' method
        HttpUtils.error(resp, StatusCodes.CREATED, sessionId);
    }
}
