package jchess.server.util;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import java.io.IOException;

public class SocketUtils {
    public static void close(WebSocketChannel channel, String detailMessage) throws IOException {
        final int code = 1008; // Generic "policy violation"
        channel.setCloseCode(code);
        channel.setCloseReason(detailMessage);
        WebSockets.sendClose(code, detailMessage, channel, null);
    }
}
