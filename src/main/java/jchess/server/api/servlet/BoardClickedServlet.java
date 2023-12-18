package jchess.server.api.servlet;

import dx.schema.message.GameClicked;
import dx.schema.types.Vector2I;
import io.undertow.util.StatusCodes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.common.IChessGame;
import jchess.common.events.BoardClickedEvent;
import jchess.ecs.EcsEvent;
import jchess.server.util.JsonUtils;
import jchess.server.util.SessionUtils;

import java.io.IOException;

public class BoardClickedServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GameClicked clickInfo = JsonUtils.getMapper().readValue(req.getReader(), GameClicked.class);
        IChessGame game = SessionUtils.findGame(clickInfo.getSessionId());
        game.getEventManager().<EcsEvent<Vector2I>>getEvent(BoardClickedEvent.class).fire(clickInfo.getClickPos());

        resp.setStatus(StatusCodes.OK);
    }
}
