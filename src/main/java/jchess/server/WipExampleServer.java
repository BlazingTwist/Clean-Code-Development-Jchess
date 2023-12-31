package jchess.server;

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
import jchess.common.IChessGame;
import jchess.common.events.OfferPieceSelectionEvent;
import jchess.common.events.RenderEvent;
import jchess.gamemode.GameMode;
import jchess.server.api.servlet.BoardClickedServlet;
import jchess.server.api.servlet.GameCreateServlet;
import jchess.server.api.servlet.GameModesServlet;
import jchess.server.api.servlet.ThemesServlet;
import jchess.server.api.socket.BoardUpdateWebsocket;
import jchess.server.api.socket.PieceSelectionWebsocket;
import jchess.server.api.socket.ChatWebsocket;
import jchess.server.session.SessionManager;
import jchess.server.session.SessionMgrController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class WipExampleServer {
    private static final Logger logger = LoggerFactory.getLogger(WipExampleServer.class);

    public static final String resourcePrefix = "resources";

    public static BoardUpdateWebsocket boardUpdateWebsocket;
    public static PieceSelectionWebsocket pieceSelectionWebsocket;

    public static void main(String[] args) throws ServletException, URISyntaxException {
        boardUpdateWebsocket = new BoardUpdateWebsocket();
        pieceSelectionWebsocket = new PieceSelectionWebsocket();
        ChatWebsocket chatWebsocket = new ChatWebsocket();

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
                .addPrefixPath("/api/board/update", Handlers.websocket(boardUpdateWebsocket))
                .addPrefixPath("/api/pieceSelection", Handlers.websocket(pieceSelectionWebsocket))
                .addPrefixPath("/api/chat", Handlers.websocket(chatWebsocket));

        Undertow server = Undertow.builder()
                .addHttpListener(8880, "127.0.0.1")
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

        game.getEventManager().getEvent(RenderEvent.class).addListener(x -> boardUpdateWebsocket.onGameRenderEvent(sessionId, game));
        game.getEventManager().<OfferPieceSelectionEvent>getEvent(OfferPieceSelectionEvent.class).addListener(x -> pieceSelectionWebsocket.onOfferPieceSelectionEvent(sessionId, x));
        logger.info("Starting new game. Mode '{}'. SessionId '{}'", mode, sessionId);
        game.start();

        return sessionId;
    }

}
