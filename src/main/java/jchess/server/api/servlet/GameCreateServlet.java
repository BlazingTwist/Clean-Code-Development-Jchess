package jchess.server.api.servlet;

import dx.schema.message.GameCreate;
import dx.schema.types.LayoutId;
import io.undertow.util.StatusCodes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.server.WipExampleServer;
import jchess.server.util.HttpUtils;
import jchess.server.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GameCreateServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(GameCreateServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GameCreate createInfo = JsonUtils.getMapper().readValue(req.getReader(), GameCreate.class);

        final LayoutId layoutId;
        try {
            layoutId = LayoutId.fromValue(createInfo.getModeId());
        } catch (Exception e) {
            logger.warn("Failed to find GameMode with id: '{}'", createInfo.getModeId());
            HttpUtils.respond(resp, StatusCodes.BAD_REQUEST, "Invalid Game-Mode Id");
            return;
        }

        final String sessionId;
        try {
            sessionId = WipExampleServer.startNewGame(layoutId);
        } catch (Exception e) {
            logger.warn("Failed to start new game.", e);
            HttpUtils.respond(resp, StatusCodes.INTERNAL_SERVER_ERROR, "Failed to start new game. Exception: " + e.getMessage());
            return;
        }

        HttpUtils.respond(resp, StatusCodes.CREATED, sessionId);
    }
}
