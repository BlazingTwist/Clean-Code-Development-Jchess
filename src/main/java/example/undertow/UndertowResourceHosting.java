package example.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Einfaches Undertow Beispiel mit Resource hosting.<p>
 * Ressourcen können dann im Browser geöffnet werden,
 * z.B. '<a href="http://localhost:8080/jchess/images.org/Bishop-W.png">Bishop-W.png</a>'
 */
public class UndertowResourceHosting {
    private static final Logger logger = LoggerFactory.getLogger(UndertowResourceHosting.class);

    @SuppressWarnings("DuplicatedCode") // example code. Duplication is allowed
    public static void main(String[] args) throws ServletException {
        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(UndertowResourceHosting.class.getClassLoader())
                .setContextPath("")
                .setDeploymentName("Example_UndertowResourceHosting")
                .setResourceManager(new ClassPathResourceManager(UndertowResourceHosting.class.getClassLoader()));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();
        HttpHandler handler = manager.start();
        PathHandler pathHandler = Handlers.path(handler);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
        logger.info("Server started");
    }
}
