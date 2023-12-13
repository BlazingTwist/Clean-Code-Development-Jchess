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
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UndertowWebsocket {
    private static final Logger logger = LoggerFactory.getLogger(UndertowWebsocket.class);

    public static void main(String[] args) throws ServletException, IOException {
        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(UndertowWebsocket.class.getClassLoader())
                .setContextPath("")
                .setDeploymentName("Example_UndertowWebsocket")
                .setResourceManager(new ClassPathResourceManager(UndertowWebsocket.class.getClassLoader()));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deployment);
        manager.deploy();
        HttpHandler handler = manager.start();
        PathHandler pathHandler = Handlers.path(handler)
                .addPrefixPath("/websocket", Handlers.websocket(new SocketHandler()));

        Undertow server = Undertow.builder()
                .addHttpListener(8880, "localhost")
                .setHandler(pathHandler)
                .build();
        server.start();
        logger.info("Server started");
    }

    public static class SocketHandler extends AbstractReceiveListener implements WebSocketConnectionCallback {
        private final List<WebSocketChannel> channels = new ArrayList<>();

        @Override
        public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
            logger.info("onConnect");
            this.channels.add(channel);

            channel.getReceiveSetter().set(this);
            channel.resumeReceives();
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
            String msgText = message.getData();
            logger.info("Received message {}", msgText);
            notifyChannels(msgText);
        }

        public void notifyChannels(String message) {
            int numChannelsNotified = 0;
            int numChannelsDropped = 0;
            Iterator<WebSocketChannel> iterator = channels.iterator();
            while (iterator.hasNext()) {
                WebSocketChannel channel = iterator.next();
                if (channel.getCloseCode() >= 0) {
                    iterator.remove();
                    numChannelsDropped++;
                    continue;
                }

                WebSockets.sendText(message, channel, null);
                numChannelsNotified++;
            }
            logger.info("Notified {} channels, dropped {} channels", numChannelsNotified, numChannelsDropped);
        }
    }
}
