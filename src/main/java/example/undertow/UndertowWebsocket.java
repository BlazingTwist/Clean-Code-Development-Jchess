package example.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UndertowWebsocket {
    private static final Logger logger = LoggerFactory.getLogger(UndertowWebsocket.class);

    public static void main(String[] args) throws ServletException, IOException {
        SocketHandler socketHandler = new SocketHandler();
        new Thread(() -> {
            while (true) {
                try {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        break;
                    }
                    logger.info("Notifying channel.");
                    socketHandler.notifyChannel();
                } catch (Exception ignore) {

                }
            }
        }).start();

        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(UndertowWebsocket.class.getClassLoader())
                .setContextPath("")
                .setDeploymentName("Example_UndertowWebsocket")
                .setResourceManager(new ClassPathResourceManager(UndertowWebsocket.class.getClassLoader()));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();
        HttpHandler handler = manager.start();
        PathHandler pathHandler = Handlers.path(handler)
                .addPrefixPath("/websocket", Handlers.websocket(socketHandler));

        Undertow server = Undertow.builder()
                .addHttpListener(8880, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
        logger.info("Server started");
    }

    public static class SocketHandler implements WebSocketConnectionCallback {
        private List<WebSocketChannel> channel = new ArrayList<>();

        @Override
        public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
            logger.info("onConnect");
            this.channel.add(channel);
        }

        public void notifyChannel() {
            for (WebSocketChannel c : channel) {
                WebSockets.sendText("Hallo Welt!", c, null);
            }
        }
    }
}
