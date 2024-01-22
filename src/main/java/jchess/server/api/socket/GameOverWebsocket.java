package jchess.server.api.socket;

import dx.schema.message.GameOver;
import dx.schema.message.GameOverSubscribe;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import jchess.server.GameSessionData;
import jchess.server.util.JsonUtils;
import jchess.server.util.SessionUtils;
import jchess.server.util.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameOverWebsocket extends AbstractReceiveListener implements WebSocketConnectionCallback {
    private static final Logger logger = LoggerFactory.getLogger(GameOverWebsocket.class);

    public void onGameOverEvent(String sessionId, Integer[] scores) {
        logger.info("onGameOverEvent for session {}", sessionId);
        GameSessionData game = SessionUtils.findGame(sessionId);
        if (game == null) {
            logger.error("Unable to send gameOver. No session found");
            return;
        }

        game.gameOverHandler.sendGameOver(scores);
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        logger.debug("Connected to Socket");
        channel.getReceiveSetter().set(this);
        channel.resumeReceives();
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        String data = message.getData();
        logger.debug("Received message '{}'", data);

        GameOverSubscribe messageObj = JsonUtils.getMapper().readValue(data, GameOverSubscribe.class);
        String sessionId = messageObj.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            SocketUtils.close(channel, "property 'sessionId' is missing.");
            return;
        }

        GameSessionData game = SessionUtils.findGame(sessionId);
        if (game == null) {
            SocketUtils.close(channel, "Requested Session (id='" + sessionId + "') does not exist");
            return;
        }

        game.gameOverHandler.subscribe(channel);
    }

    public static final class Handler implements Closeable {
        private final List<WebSocketChannel> channels = new ArrayList<>();

        public void subscribe(WebSocketChannel channel) {
            channels.add(channel);
        }

        public void sendGameOver(Integer[] scores) {
            GameOver messageObj = new GameOver();
            messageObj.setPlayerScores(Arrays.asList(scores));
            final String message = JsonUtils.serialize(messageObj);

            channels.removeIf(channel -> channel.getCloseCode() >= 0);
            for (WebSocketChannel channel : channels) {
                WebSockets.sendText(message, channel, null);
            }
            logger.debug("Notified {} WebSockets of gameOver", channels.size());
        }

        @Override
        public void close() throws IOException {
            for (WebSocketChannel channel : channels) {
                SocketUtils.close(channel, "Session has expired");
            }
            channels.clear();
        }
    }
}
