package jchess.server.api.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dx.schema.message.OfferPieceSelection;
import dx.schema.message.PieceSelected;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import jchess.common.IChessGame;
import jchess.common.events.OfferPieceSelectionEvent;
import jchess.common.events.PieceOfferSelectedEvent;
import jchess.server.GameSessionData;
import jchess.server.session.SessionManager;
import jchess.server.session.SessionMgrController;
import jchess.server.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PieceSelectionWebsocket extends AbstractReceiveListener implements WebSocketConnectionCallback {
    private static final Logger logger = LoggerFactory.getLogger(PieceSelectionWebsocket.class);

    public void onOfferPieceSelectionEvent(String sessionId, OfferPieceSelectionEvent.PieceSelection pieceSelection) {
        logger.info("onOfferPieceSelectionEvent for session {}", sessionId);
        SessionManager<GameSessionData> gameManager = SessionMgrController.lookupSessionManager(GameSessionData.class);
        SessionManager.Session<GameSessionData> session = gameManager.getSession(sessionId);
        session.sessionData.pieceSelectionHandler().offerPieceSelection(pieceSelection);
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

        String msgType = getMessageType(messageTree);
        if (!"subscribe".equalsIgnoreCase(msgType)) {
            throw new InvalidObjectException("expected property 'msgType' to be 'subscribe', but instead received: '" + msgType + "'");
        }

        String sessionId = messageTree.get("sessionId").textValue();
        SessionManager<GameSessionData> gameManager = SessionMgrController.lookupSessionManager(GameSessionData.class);
        SessionManager.Session<GameSessionData> session = gameManager.getSession(sessionId);

        PieceSelectionHandler handler = session.sessionData.pieceSelectionHandler();
        handler.add(channel);
        channel.getReceiveSetter().set(handler);
    }

    private static String getMessageType(JsonNode messageTree) throws IOException {
        String msgType = messageTree.get("msgType").textValue();
        if (msgType == null || msgType.isBlank()) {
            throw new InvalidObjectException("property 'msgType' is missing");
        }
        return msgType;
    }

    public static class PieceSelectionHandler extends AbstractReceiveListener {
        private final IChessGame game;
        private final List<WebSocketChannel> channels = new ArrayList<>();

        public PieceSelectionHandler(IChessGame game) {
            this.game = game;
        }

        public void add(WebSocketChannel channel) {
            channels.add(channel);
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
            ObjectMapper mapper = JsonUtils.getMapper();
            JsonNode messageTree = mapper.readTree(message.getData());

            String msgType = getMessageType(messageTree);
            if (!"pieceSelected".equalsIgnoreCase(msgType)) {
                throw new InvalidObjectException("expected property 'msgType' to be 'pieceSelected', but instead received: '" + msgType + "'");
            }

            PieceSelected pieceSel = mapper.readValue(messageTree.get("data").textValue(), PieceSelected.class);
            game.getEventManager().<PieceOfferSelectedEvent>getEvent(PieceOfferSelectedEvent.class).fire(pieceSel);
        }

        public void offerPieceSelection(OfferPieceSelectionEvent.PieceSelection pieceSelection) {
            OfferPieceSelection payload = new OfferPieceSelection();
            payload.setTitle(pieceSelection.windowTitle());
            payload.setPieces(Arrays.asList(pieceSelection.piecesToOffer()));

            int socketsNotified = 0;
            Iterator<WebSocketChannel> iterator = channels.iterator();
            while (iterator.hasNext()) {
                WebSocketChannel channel = iterator.next();
                if (channel.getCloseCode() >= 0) {
                    iterator.remove();
                    continue;
                }

                WebSockets.sendText(JsonUtils.serialize(payload), channel, null);
                socketsNotified++;
            }
            logger.debug("Notified {} WebSockets of offerPieceSelection", socketsNotified);
        }

    }
}
