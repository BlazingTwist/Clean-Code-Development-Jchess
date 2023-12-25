package jchess.server.api.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dx.schema.message.ChatMessage;
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
import java.util.*;

/**
 * Behandelt den Multiplayer-Chat pro Game-Session
 */
public class ChatWebsocket extends AbstractReceiveListener implements WebSocketConnectionCallback {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebsocket.class);
    private final Map<String, ChatHandler> chatHandlersBySessionId = new HashMap<>();

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

        String userName = JsonUtils.traverse(messageTree).get("userName").textValue();
        if (userName == null || userName.isBlank()) {
            SocketUtils.close(channel, "property 'userName' is missing.");
            return;
        }

        ChatHandler handler = chatHandlersBySessionId.computeIfAbsent(sessionId, x -> new ChatHandler());
        handler.addChatter(userName, channel);
    }

    public static class ChatHandler implements Closeable {
        public final List<ChatParticipant> chatters = new ArrayList<>();
        public final List<ChatMessage> chatMessages = new ArrayList<>();

        public void addChatter(String userName, WebSocketChannel channel) {
            repeatAllMessages(channel);

            ChatParticipant chatter = new ChatParticipant(userName, channel);
            chatters.add(chatter);
            channel.getReceiveSetter().set(new ChatReceiveHandler(this, chatter));
        }

        private void repeatAllMessages(WebSocketChannel channel) {
            logger.debug("repeating {} messages to new subscriber", chatMessages.size());
            String messageList = JsonUtils.serialize(chatMessages);
            WebSockets.sendText(messageList, channel, null);
        }

        public void onMessageReceived(ChatMessage message) {
            chatMessages.add(message);
            notifyAllChatters(message);
        }

        private void notifyAllChatters(ChatMessage message) {
            String payload = JsonUtils.serialize(List.of(message));

            int socketsNotified = 0;
            Iterator<ChatParticipant> iterator = chatters.iterator();
            while (iterator.hasNext()) {
                WebSocketChannel channel = iterator.next().channel;
                if (channel.getCloseCode() >= 0) {
                    iterator.remove();
                    continue;
                }

                WebSockets.sendText(JsonUtils.serialize(payload), channel, null);
                socketsNotified++;
            }
            logger.debug("Notified {} WebSockets of chat message", socketsNotified);
        }

        @Override
        public void close() throws IOException {
            for (ChatParticipant chatter : chatters) {
                SocketUtils.close(chatter.channel, "session has expired");
            }
            chatters.clear();
        }
    }

    private record ChatParticipant(String userName, WebSocketChannel channel) {
    }

    private static class ChatReceiveHandler extends AbstractReceiveListener {
        private final ChatHandler chatHandler;
        private final ChatParticipant sender;

        public ChatReceiveHandler(ChatHandler chatHandler, ChatParticipant sender) {
            this.chatHandler = chatHandler;
            this.sender = sender;
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
            String data = message.getData();
            logger.debug("Received chat message '{}'", data);
            ObjectMapper mapper = JsonUtils.getMapper();
            JsonNode messageTree = mapper.readTree(data);

            String msgType = JsonUtils.traverse(messageTree).get("msgType").textValue();
            if (!"submit".equalsIgnoreCase(msgType)) {
                SocketUtils.close(channel, "expected property 'msgType' to be 'submit', but instead received: '" + msgType + "'");
                return;
            }

            String messageText = JsonUtils.traverse(messageTree).get("data").textValue();
            if (messageText == null || messageText.isBlank()) {
                SocketUtils.close(channel, "property 'data' is missing.");
                return;
            }

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setUserName(sender.userName);
            chatMessage.setMessage(messageText);
            chatMessage.setTimestamp(System.currentTimeMillis());
            chatHandler.onMessageReceived(chatMessage);
        }
    }
}
