package jchess.server.api.servlet;

import dx.schema.message.Themes;
import dx.schema.types.Icon;
import dx.schema.types.Theme;
import dx.schema.types.Vector2I;
import io.undertow.util.StatusCodes;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.server.WipExampleServer;
import jchess.server.adapter.Vector2IAdapter;
import jchess.server.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ResourceHelper;
import util.ResourceHelper.ResourceFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemesServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ThemesServlet.class);
    private static final Map<String, Map<String, String>> themeMap = new HashMap<>();
    private static final Map<String, TileInfo> themeTileInfoMap = new HashMap<>();
    private static final String resourcePrefix = WipExampleServer.resourcePrefix;

    public ThemesServlet() {
        try {
            ResourceFile themeRootDir = ResourceHelper.getResource("/jchess/theme/v2");
            loadThemes(themeRootDir);
        } catch (Exception e) {
            logger.error("Unable to find theme directory", e);
        }
    }

    private void loadThemes(ResourceFile themesRootDir) {
        TileInfo hexTileInfo = new TileInfo(Vector2IAdapter.fromPosition(30, 32), Vector2IAdapter.fromPosition(15, 24));
        TileInfo squareTileInfo = new TileInfo(Vector2IAdapter.fromPosition(50, 50), Vector2IAdapter.fromPosition(50, 50));

        String[] themeDirNames = {"default"};
        for (String themeDirName : themeDirNames) {
            ResourceFile themeDir = themesRootDir.resolve(themeDirName);
            String hexThemeId = themeDirName;
            Map<String, String> hexIcons = jchess.gamemode.hex3p.Theme.getIconMap(themeDir);
            themeMap.put(hexThemeId, hexIcons);
            themeTileInfoMap.put(hexThemeId, hexTileInfo);

            String squareThemeId = themeDirName + "_square";
            Map<String, String> squareIcons = jchess.gamemode.square2p.Theme.getIconMap(themeDir);
            themeMap.put(squareThemeId, squareIcons);
            themeTileInfoMap.put(squareThemeId, squareTileInfo);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");

        Themes message = new Themes();
        List<Theme> themeList = message.getThemes();
        for (Map.Entry<String, Map<String, String>> themeEntry : themeMap.entrySet()) {
            Theme themeMessage = new Theme();
            themeList.add(themeMessage);

            TileInfo tileInfo = themeTileInfoMap.get(themeEntry.getKey());

            themeMessage.setName(themeEntry.getKey());
            themeMessage.setTileAspectRatio(tileInfo.tileSize);
            themeMessage.setTileStride(tileInfo.tileStride);
            List<Icon> themeIcons = themeMessage.getIcons();
            for (Map.Entry<String, String> iconEntry : themeEntry.getValue().entrySet()) {
                Icon icon = new Icon();
                themeIcons.add(icon);

                icon.setIconId(iconEntry.getKey());
                icon.setIconPath(resourcePrefix + "/" + iconEntry.getValue());
            }
        }

        resp.setStatus(StatusCodes.OK);
        JsonUtils.getMapper().writeValue(resp.getWriter(), message);
    }

    private record TileInfo(Vector2I tileSize, Vector2I tileStride) {
    }
}
