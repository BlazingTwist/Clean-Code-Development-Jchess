package jchess.game.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dx.schema.message.GameUpdate;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import jchess.game.server.adapter.EntityAdapter;
import jchess.game.server.session.SessionManager;
import jchess.game.server.session.SessionMgrController;
import jchess.game.server.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BoardUpdateWebsocket extends AbstractReceiveListener implements WebSocketConnectionCallback {
    private static final Logger logger = LoggerFactory.getLogger(BoardUpdateWebsocket.class);
    private final Map<String, List<WebSocketChannel>> channelsBySessionId = new HashMap<>();

    public void registerGame(String sessionId, GameSessionData game) {
        game.onBoardChangedEvent().addPostEventListener((x) -> {
            String message = getUpdateMessage(game);
            if (message == null) {
                return;
            }

            int socketsNotified = 0;
            List<WebSocketChannel> channels = channelsBySessionId.get(sessionId);
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
            logger.info("Notified {} WebSockets of boardUpdate", socketsNotified);
        });
    }

    private String getUpdateMessage(GameSessionData game) {
        GameUpdate gameUpdateObject = new GameUpdate();
        gameUpdateObject.setActivePlayerId(game.gameState().activePlayerId());
        gameUpdateObject.setBoardState(game.entityManager().getEntities().stream()
                .map(EntityAdapter.Instance::convert)
                .toList());

        ObjectMapper mapper = JsonUtils.getMapper();
        String message;
        try {
            message = mapper.writeValueAsString(gameUpdateObject);
        } catch (JsonProcessingException e) {
            logger.error("Failed to generate Json message", e);
            return null;
        }
        return message;
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
        ObjectMapper mapper = new ObjectMapper();
        JsonNode messageTree = mapper.readTree(data);

        String sessionId = messageTree.get("sessionId").textValue();
        List<WebSocketChannel> channels = channelsBySessionId.computeIfAbsent(sessionId, key -> new ArrayList<>());
        channels.add(channel);

        // send the current board state to the newly registered WebSocket
        SessionManager<GameSessionData> gameManager = SessionMgrController.lookupSessionManager(GameSessionData.class);
        SessionManager.Session<GameSessionData> gameData = gameManager.getSession(sessionId);
        String updateMessage = getUpdateMessage(gameData.sessionData);
        if (updateMessage != null) {
            WebSockets.sendText(updateMessage, channel, null);
        }
    }
}