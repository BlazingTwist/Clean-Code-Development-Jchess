package jchess.server.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class HttpUtils {
    public static void respond(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        PrintWriter writer = response.getWriter();
        writer.write(message);
        writer.flush();
        writer.close();
    }

    public static <T> void respondJson(HttpServletResponse response, int code, T object) throws IOException {
        response.setContentType("text/json");
        response.setStatus(code);

        JsonUtils.getMapper().writeValue(response.getWriter(), object);
        PrintWriter writer = response.getWriter();
        writer.flush();
        writer.close();
    }
}
