package jchess.game.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import dx.schema.message.GameUpdate;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.ecs.EntityManager;
import jchess.game.common.IGameState;
import jchess.game.server.adapter.EntityAdapter;
import jchess.game.server.util.JsonUtils;

import java.io.IOException;
import java.io.PrintWriter;

public class GameUpdateServlet extends HttpServlet {
    // TODO erja, tie these fields into a session ID or something
    public static IGameState gameState;
    public static EntityManager entityManager;

    // TODO erja, currently implemented as a GET request, but this should be POST-ing to the frontend.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");
        PrintWriter writer = resp.getWriter();

        GameUpdate message = new GameUpdate();
        message.setActivePlayerId(gameState.activePlayerId());
        message.setBoardState(entityManager.getEntities().stream()
                .map(EntityAdapter.Instance::convert)
                .toList());

        ObjectMapper mapper = JsonUtils.getMapper();
        mapper.writeValue(writer, message);
        writer.close();
    }
}
