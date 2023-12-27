package jchess.server.api.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.*;

public class BoardUpdateWebsocket extends AbstractReceiveListener implements WebSocketConnectionCallback {
    private static final Logger logger = LoggerFactory.getLogger(BoardUpdateWebsocket.class);
    private final Map<String, List<WebSocketChannel>> channelsBySessionId = new HashMap<>();

    public void onGameRenderEvent(String sessionId, IChessGame game) {
        String message = getUpdateMessage(game);
        if (message == null) {
            return;
        }

        List<WebSocketChannel> channels = channelsBySessionId.get(sessionId);
        if (channels == null || channels.isEmpty()) {
            return;
        }

        int socketsNotified = 0;
        Iterator<WebSocketChannel> iterator = channels.iterator();
        while (iterator.hasNext()) {
            WebSocketChannel channel = iterator.next();
            if (channel.getCloseCode() >= 0) {
                iterator.remove();
                continue;
            }

            WebSockets.sendText(message, channel, null);
            socketsNotified++;
        }
        logger.debug("Notified {} WebSockets of boardUpdate", socketsNotified);
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
        ObjectMapper mapper = JsonUtils.getMapper();
        JsonNode messageTree = mapper.readTree(data);

        String sessionId = JsonUtils.traverse(messageTree).get("sessionId").textValue();
        if (sessionId == null || sessionId.isBlank()) {
            SocketUtils.close(channel, "property 'sessionId' is missing.");
            return;
        }

        List<WebSocketChannel> channels = channelsBySessionId.computeIfAbsent(sessionId, key -> new ArrayList<>());
        channels.add(channel);

        // send the current board state to the newly registered WebSocket
        GameSessionData game = SessionUtils.findGame(sessionId);
        if (game == null) {
            SocketUtils.close(channel, "Requested Session (id='" + sessionId + "') does not exist");
            return;
        }

        String updateMessage = getUpdateMessage(game.game());
        if (updateMessage != null) {
            WebSockets.sendText(updateMessage, channel, null);
        }
    }

    private String getUpdateMessage(IChessGame game) {
        GameUpdate gameUpdateObject = new GameUpdate();
        gameUpdateObject.setActivePlayerId(game.getActivePlayerId());
        gameUpdateObject.setBoardState(game.getEntityManager().getEntities().stream()
                .map(EntityAdapter.Instance::convert)
                .toList());

        return JsonUtils.serialize(gameUpdateObject);
    }
}
