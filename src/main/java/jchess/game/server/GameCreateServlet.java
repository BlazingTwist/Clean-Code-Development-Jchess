package jchess.game.server;

import io.undertow.util.StatusCodes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class GameCreateServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = WipExampleServer.startNewGame();

        resp.setStatus(StatusCodes.CREATED);
        PrintWriter writer = resp.getWriter();
        writer.write(sessionId);
        writer.flush();
        writer.close();
    }
}
