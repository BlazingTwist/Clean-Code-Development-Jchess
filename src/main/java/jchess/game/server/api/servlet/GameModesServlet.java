package jchess.game.server.api.servlet;

import dx.schema.message.GameModes;
import dx.schema.types.GameMode;
import io.undertow.util.StatusCodes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.game.server.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameModesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");

        List<GameMode> gameModes = new ArrayList<>();
        for (jchess.game.layout.GameMode mode : jchess.game.layout.GameMode.values()) {
            GameMode gameMode = new GameMode();
            gameMode.setModeId(mode.name());
            gameMode.setDisplayName(mode.getDisplayName());
            gameMode.setNumPlayers(mode.getNumPlayers());
            gameMode.setThemeIds(Arrays.asList(mode.getAllowedThemeIds()));
            gameModes.add(gameMode);
        }

        GameModes message = new GameModes();
        message.setModes(gameModes);

        resp.setStatus(StatusCodes.OK);
        JsonUtils.getMapper().writeValue(resp.getWriter(), message);
    }
}
