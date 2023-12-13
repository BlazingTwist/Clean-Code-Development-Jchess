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
import jchess.game.layout.hex3p.Hex3PlayerGame;
import jchess.game.layout.hex3p.Theme;
import jchess.game.server.session.SessionMgrController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WipExampleServer {
    private static final Logger logger = LoggerFactory.getLogger(WipExampleServer.class);

    private static final String resourcePrefix = "resources";

    public static Theme theme;
    public static BoardUpdateWebsocket boardUpdateWebsocket;

    public static void main(String[] args) throws ServletException, URISyntaxException {
        String themePath = "/jchess/theme/v2/default";
        theme = new Theme(new File(Hex3PlayerGame.class.getResource(themePath).toURI()));
        boardUpdateWebsocket = new BoardUpdateWebsocket();

        ThemesServlet.themeMap = Map.of("default", theme.getIconMap());
        ThemesServlet.resourcePrefix = resourcePrefix;

        SessionMgrController.registerSessionManager(GameSessionData.class, 10, TimeUnit.MINUTES);
        SessionMgrController.startHeartbeat(1, TimeUnit.MINUTES);

        ClassPathResourceManager resourceManager = new ClassPathResourceManager(WipExampleServer.class.getClassLoader());

        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(WipExampleServer.class.getClassLoader())
                .setContextPath("")
                .setDeploymentName("WipChessServer")
                .addServlet(Servlets.servlet("GameCreate", GameCreateServlet.class).addMapping("/api/game/create"))
                .addServlet(Servlets.servlet("GameClicked", BoardClickedServlet.class).addMapping("/api/game/clicked"))
                .addServlet(Servlets.servlet("Themes", ThemesServlet.class).addMapping("/api/themes"));


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

}
