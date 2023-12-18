package jchess.server.api.servlet;

import dx.schema.message.GameCreate;
import io.undertow.util.StatusCodes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.gamemode.GameMode;
import jchess.server.WipExampleServer;
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

        GameMode gameMode;
        try {
            gameMode = GameMode.valueOf(createInfo.getModeId());
        } catch (Exception e) {
            logger.warn("Failed to find GameMode with id: '{}'", createInfo.getModeId());
            resp.setStatus(StatusCodes.BAD_REQUEST);
            PrintWriter writer = resp.getWriter();
            writer.write("Invalid Game-Mode Id");
            writer.flush();
            writer.close();
            return;
        }

        String sessionId = WipExampleServer.startNewGame(gameMode);

        resp.setStatus(StatusCodes.CREATED);
        PrintWriter writer = resp.getWriter();
        writer.write(sessionId);
        writer.flush();
        writer.close();
    }
}
