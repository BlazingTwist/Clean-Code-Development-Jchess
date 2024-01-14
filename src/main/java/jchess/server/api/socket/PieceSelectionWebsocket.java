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

public class PieceSelectionWebsocket extends AbstractReceiveListener implements WebSocketConnectionCallback {
    private static final Logger logger = LoggerFactory.getLogger(PieceSelectionWebsocket.class);

    public void onOfferPieceSelectionEvent(String sessionId, OfferPieceSelectionEvent.PieceSelection pieceSelection) {
        logger.info("onOfferPieceSelectionEvent for session {}", sessionId);
        GameSessionData game = SessionUtils.findGame(sessionId);
        if (game == null) {
            logger.error("Unable to send offerPieceSelection. No session found.");
            return;
        }

        game.pieceSelectionHandler.offerPieceSelection(pieceSelection);
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

        String msgType = JsonUtils.traverse(messageTree).get("msgType").textValue();
        if (!"subscribe".equalsIgnoreCase(msgType)) {
            SocketUtils.close(channel, "expected property 'msgType' to be 'subscribe', but instead received: '" + msgType + "'");
            return;
        }

        String sessionId = JsonUtils.traverse(messageTree).get("sessionId").textValue();
        if (sessionId == null || sessionId.isBlank()) {
            SocketUtils.close(channel, "property 'sessionId' is missing.");
            return;
        }

        GameSessionData game = SessionUtils.findGame(sessionId);
        if (game == null) {
            SocketUtils.close(channel, "Session does not exist");
            return;
        }

        PieceSelectionHandler handler = game.pieceSelectionHandler;
        handler.add(channel);
        channel.getReceiveSetter().set(handler);
    }

    public static class PieceSelectionHandler extends AbstractReceiveListener implements Closeable {
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
            String data = message.getData();
            logger.info("Received pieceSelection message '{}'", data);
            ObjectMapper mapper = JsonUtils.getMapper();
            JsonNode messageTree = mapper.readTree(data);

            String msgType = JsonUtils.traverse(messageTree).get("msgType").textValue();
            if (!"pieceSelected".equalsIgnoreCase(msgType)) {
                SocketUtils.close(channel, "expected property 'msgType' to be 'pieceSelected', but instead received: '" + msgType + "'");
                return;
            }

            JsonNode dataNode = messageTree.get("data");
            if (dataNode == null) {
                SocketUtils.close(channel, "property 'data' is missing.");
                return;
            }
            PieceSelected pieceSel = mapper.treeToValue(dataNode, PieceSelected.class);
            game.getEventManager().getEvent(PieceOfferSelectedEvent.class).fire(pieceSel);
        }

        public void offerPieceSelection(OfferPieceSelectionEvent.PieceSelection pieceSelection) {
            OfferPieceSelection messageObj = new OfferPieceSelection();
            messageObj.setTitle(pieceSelection.windowTitle());
            messageObj.setPieces(Arrays.asList(pieceSelection.piecesToOffer()));
            final String message = JsonUtils.serialize(messageObj);

            channels.removeIf(channel -> channel.getCloseCode() >= 0);
            for (WebSocketChannel channel : channels) {
                WebSockets.sendText(message, channel, null);
            }
            logger.debug("Notified {} WebSockets of offerPieceSelection", channels.size());
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
