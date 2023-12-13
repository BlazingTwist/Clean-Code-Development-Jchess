package jchess.game.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import dx.schema.message.Themes;
import dx.schema.types.Icon;
import dx.schema.types.Theme;
import dx.schema.types.Vector2I;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.game.server.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ThemesServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ThemesServlet.class);
    public static final Map<String, Map<String, String>> themeMap = new HashMap<>();
    public static final String resourcePrefix = WipExampleServer.resourcePrefix;

    public ThemesServlet() {
        try {
            File themeRootDir = new File(Objects.requireNonNull(ThemesServlet.class.getResource("/jchess/theme/v2")).toURI());
            loadThemes(themeRootDir);
        } catch (Exception e) {
            logger.error("Unable to find theme directory", e);
        }
    }

    private void loadThemes(File themesRootDir) {
        if (!themesRootDir.exists()) {
            logger.error("Unable to find theme directory, searching at '{}'", themesRootDir.getAbsolutePath());
            return;
        }

        File[] themeDirectories = themesRootDir.listFiles(File::isDirectory);
        if (themeDirectories == null || themeDirectories.length == 0) {
            logger.error("Unable to find any themes in the root directory at '{}'", themesRootDir.getAbsolutePath());
            return;
        }

        for (File themeDir : themeDirectories) {
            Map<String, String> hexIcons = jchess.game.layout.hex3p.Theme.getIconMap(themeDir);
            themeMap.put(themeDir.getName(), hexIcons);

            Map<String, String> squareIcons = jchess.game.layout.square2p.Theme.getIconMap(themeDir);
            themeMap.put(themeDir.getName() + "_square", squareIcons);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");
        PrintWriter writer = resp.getWriter();

        Vector2I tileAspectRatio = new Vector2I();
        tileAspectRatio.setX(30);
        tileAspectRatio.setY(32);

        Vector2I tileStride = new Vector2I();
        tileStride.setX(15); // width / 2
        tileStride.setY(24); // (height + edgeLength) / 2

        Themes message = new Themes();
        List<Theme> themeList = message.getThemes();
        for (Map.Entry<String, Map<String, String>> themeEntry : themeMap.entrySet()) {
            Theme themeMessage = new Theme();
            themeList.add(themeMessage);

            themeMessage.setName(themeEntry.getKey());
            themeMessage.setTileAspectRatio(tileAspectRatio);
            themeMessage.setTileStride(tileStride);
            List<Icon> themeIcons = themeMessage.getIcons();
            for (Map.Entry<String, String> iconEntry : themeEntry.getValue().entrySet()) {
                Icon icon = new Icon();
                themeIcons.add(icon);

                icon.setIconId(iconEntry.getKey());
                icon.setIconPath(resourcePrefix + "/" + iconEntry.getValue());
            }
        }

        ObjectMapper mapper = JsonUtils.getMapper();
        mapper.writeValue(writer, message);
        writer.close();
    }
}
