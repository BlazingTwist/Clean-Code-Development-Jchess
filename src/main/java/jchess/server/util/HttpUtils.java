package jchess.server.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class HttpUtils {
    public static void error(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        PrintWriter writer = response.getWriter();
        writer.write(message);
        writer.flush();
        writer.close();
    }
}
