package jchess.game.server;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import jakarta.servlet.ServletException;
import jchess.game.common.IChessGame;
import jchess.game.common.events.RenderEvent;
import jchess.game.layout.GameMode;
import jchess.game.server.api.servlet.BoardClickedServlet;
import jchess.game.server.api.servlet.GameCreateServlet;
import jchess.game.server.api.servlet.GameModesServlet;
import jchess.game.server.api.servlet.ThemesServlet;
import jchess.game.server.api.socket.BoardUpdateWebsocket;
import jchess.game.server.session.SessionManager;
import jchess.game.server.session.SessionMgrController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class WipExampleServer {
    private static final Logger logger = LoggerFactory.getLogger(WipExampleServer.class);

    public static final String resourcePrefix = "resources";

    public static BoardUpdateWebsocket boardUpdateWebsocket;

    public static void main(String[] args) throws ServletException, URISyntaxException {
        boardUpdateWebsocket = new BoardUpdateWebsocket();

        SessionMgrController.registerSessionManager(GameSessionData.class, 10, TimeUnit.MINUTES);
        SessionMgrController.startHeartbeat(1, TimeUnit.MINUTES);

        ClassPathResourceManager resourceManager = new ClassPathResourceManager(WipExampleServer.class.getClassLoader());

        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(WipExampleServer.class.getClassLoader())
                .setContextPath("")
                .setDeploymentName("WipChessServer")
                .addServlet(Servlets.servlet("GameCreate", GameCreateServlet.class).addMapping("/api/game/create"))
                .addServlet(Servlets.servlet("GameClicked", BoardClickedServlet.class).addMapping("/api/game/clicked"))
                .addServlet(Servlets.servlet("Themes", ThemesServlet.class).addMapping("/api/themes"))
                .addServlet(Servlets.servlet("GameModes", GameModesServlet.class).addMapping("/api/modes"));


        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();
        HttpHandler handler = manager.start();
        PathHandler pathHandler = Handlers.path(handler)
                .addPrefixPath(resourcePrefix, new ResourceHandler(resourceManager))
                .addPrefixPath("/api/board/update", Handlers.websocket(boardUpdateWebsocket));

        Undertow server = Undertow.builder()
                .addHttpListener(8880, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
        logger.info("Server started");
    }

    public static String startNewGame(GameMode mode) {
        IChessGame game = mode.newGame();

        GameSessionData gameData = new GameSessionData(game);
        SessionManager<GameSessionData> gameManager = SessionMgrController.lookupSessionManager(GameSessionData.class);
        String sessionId = gameManager.createSession(gameData).sessionId;

        game.getEventManager().getEvent(RenderEvent.class).addPostEventListener(x -> boardUpdateWebsocket.onGameRenderEvent(sessionId, game));
        logger.info("Starting new game. Mode '{}'. SessionId '{}'", mode, sessionId);
        game.start();

        return sessionId;
    }

}
