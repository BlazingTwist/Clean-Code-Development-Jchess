package jchess.game.server;

import example.undertow.UndertowMicroserviceGET;
import example.undertow.UndertowResourceHosting;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

public class WipExampleServer {
    private static final Logger logger = LoggerFactory.getLogger(WipExampleServer.class);

    private static final String resourcePrefix = "resources";

    public static void main(String[] args) throws ServletException, URISyntaxException {
        String themePath = "/jchess/theme/v2/default";
        Theme theme = new Theme(new File(Hex3PlayerGame.class.getResource(themePath).toURI()));
        Hex3PlayerGame game = new Hex3PlayerGame(theme);
        game.start();
        ThemesServlet.themeMap = Map.of("default", theme.getIconMap());
        ThemesServlet.resourcePrefix = resourcePrefix;
        GameUpdateServlet.gameState = game.gameState;
        GameUpdateServlet.entityManager = game.entityManager;

        ClassPathResourceManager resourceManager = new ClassPathResourceManager(WipExampleServer.class.getClassLoader());

        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(WipExampleServer.class.getClassLoader())
                .setContextPath("")
                .setDeploymentName("WipChessServer")
                .addServlet(Servlets.servlet("GameUpdate", GameUpdateServlet.class).addMapping("/api/gameUpdate"))
                .addServlet(Servlets.servlet("Themes", ThemesServlet.class).addMapping("/api/themes"));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();
        HttpHandler handler = manager.start();
        PathHandler pathHandler = Handlers.path(handler)
                .addPrefixPath(resourcePrefix, new ResourceHandler(resourceManager));

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
        logger.info("Server started");
    }
}
