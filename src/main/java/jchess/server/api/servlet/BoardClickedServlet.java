package jchess.server.api.servlet;

import dx.schema.message.GameClicked;
import io.undertow.util.StatusCodes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.common.components.TileComponent;
import jchess.common.events.BoardClickedEvent;
import jchess.server.GameSessionData;
import jchess.server.util.HttpUtils;
import jchess.server.util.JsonUtils;
import jchess.server.util.SessionUtils;

import java.awt.Point;
import java.io.IOException;

public class BoardClickedServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GameClicked clickInfo = JsonUtils.getMapper().readValue(req.getReader(), GameClicked.class);
        GameSessionData game = SessionUtils.findGame(clickInfo.getSessionId());
        if (game == null) {
            HttpUtils.respond(resp, StatusCodes.NOT_FOUND, "Session does not exist");
            return;
        }

        Point tilePosition = TileComponent.getTilePosition(clickInfo.getClickedTile());
        game.game.getEventManager().getEvent(BoardClickedEvent.class).fire(tilePosition);
        resp.setStatus(StatusCodes.OK);
    }
}
