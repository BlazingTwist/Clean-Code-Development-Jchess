package jchess.game.server.util;

import jakarta.servlet.http.HttpServletResponse;

public class CorsUtils {
    // TODO erja, chromium still rejects this stating:
    //   'Response to preflight request doesn't pass access control check:
    //    No 'Access-Control-Allow-Origin' header is present on the requested resource.'
    public static void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
    }
}
