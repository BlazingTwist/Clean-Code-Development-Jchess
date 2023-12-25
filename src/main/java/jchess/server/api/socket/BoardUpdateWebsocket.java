package jchess.server.api.socket;

import dx.schema.message.BoardUpdateSubscribe;
import dx.schema.message.GameUpdate;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import jchess.common.IChessGame;
import jchess.server.GameSessionData;
import jchess.server.adapter.EntityAdapter;
import jchess.server.util.JsonUtils;
import jchess.server.util.SessionUtils;
import jchess.server.util.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardUpdateWebsocket extends AbstractReceiveListener implements WebSocketConnectionCallback {
    private static final Logger logger = LoggerFactory.getLogger(BoardUpdateWebsocket.class);

    public void onGameRenderEvent(String sessionId, IChessGame game) {
        GameSessionData session = SessionUtils.findGame(sessionId);
        if (session == null) {
            return;
        }

        session.boardUpdateHandler.sendGameUpdate(game);
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
        logger.info("Received message '{}'", data);
        BoardUpdateSubscribe messageObj = JsonUtils.getMapper().readValue(data, BoardUpdateSubscribe.class);

        String sessionId = messageObj.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            SocketUtils.close(channel, "property 'sessionId' is missing.");
            return;
        }

        // send the current board state to the newly registered WebSocket
        GameSessionData game = SessionUtils.findGame(sessionId);
        if (game == null) {
            SocketUtils.close(channel, "Requested Session (id='" + sessionId + "') does not exist");
            return;
        }

        game.boardUpdateHandler.subscribe(channel, messageObj.getPerspective());

        String updateMessage = getUpdateMessage(game.game, messageObj.getPerspective());
        if (updateMessage != null) {
            WebSockets.sendText(updateMessage, channel, null);
        }
    }

    private static String getUpdateMessage(IChessGame game, int perspective) {
        GameUpdate gameUpdateObject = new GameUpdate();
        gameUpdateObject.setActivePlayerId(game.getActivePlayerId());
        gameUpdateObject.setBoardState(game.getEntityManager().getEntities().stream()
                .map(EntityAdapter.Instance::convert)
                .map(entity -> game.applyPerspective(entity, perspective))
                .toList());

        return JsonUtils.serialize(gameUpdateObject);
    }

    public static class Handler implements Closeable {
        private final Map<Integer, List<WebSocketChannel>> channelsByPerspective = new HashMap<>();

        public void subscribe(WebSocketChannel channel, int perspective) {
            List<WebSocketChannel> channels = channelsByPerspective.computeIfAbsent(perspective, x -> new ArrayList<>());
            channels.add(channel);
        }

        public void sendGameUpdate(IChessGame game) {
            int channelsNotified = 0;
            for (Map.Entry<Integer, List<WebSocketChannel>> entry : channelsByPerspective.entrySet()) {
                int perspective = entry.getKey();
                List<WebSocketChannel> channels = entry.getValue();

                channels.removeIf(channel -> channel.getCloseCode() >= 0);
                if (channels.isEmpty()) {
                    continue;
                }

                String message = getUpdateMessage(game, perspective);
                if (message == null) {
                    continue;
                }

                for (WebSocketChannel channel : channels) {
                    WebSockets.sendText(message, channel, null);
                    channelsNotified++;
                }
            }
            logger.debug("Notified {} WebSockets of boardUpdate", channelsNotified);

            channelsByPerspective.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        @Override
        public void close() throws IOException {
            for (List<WebSocketChannel> channels : channelsByPerspective.values()) {
                for (WebSocketChannel channel : channels) {
                    SocketUtils.close(channel, "session has expired");
                }
            }
            channelsByPerspective.clear();
        }
    }
}
