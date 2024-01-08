package jchess.server.api.servlet;

import dx.schema.message.Themes;
import io.undertow.util.StatusCodes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.common.theme.ThemeStore;
import jchess.server.WipExampleServer;
import jchess.server.util.HttpUtils;

import java.io.IOException;

public class ThemesServlet extends HttpServlet {
    private static final String resourcePrefix = WipExampleServer.resourcePrefix;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Themes message = new Themes();
        message.setResourcePrefix(resourcePrefix);
        message.setThemes(ThemeStore.INSTANCE.getThemes());
        HttpUtils.respondJson(resp, StatusCodes.OK, message);
    }
}
