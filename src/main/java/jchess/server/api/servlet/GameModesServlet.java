package jchess.server.api.servlet;

import dx.schema.conf.LayoutTheme;
import dx.schema.conf.Theme;
import dx.schema.message.GameModes;
import dx.schema.types.GameMode;
import io.undertow.util.StatusCodes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jchess.common.theme.ThemeStore;
import jchess.gamemode.GameModeStore;
import jchess.server.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameModesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/json");

        List<GameMode> gameModes = new ArrayList<>();
        for (LayoutTheme.LayoutId layout : LayoutTheme.LayoutId.values()) {
            GameModeStore.GameModeProvider provider = GameModeStore.getGameMode(layout);
            if (provider == null) {
                continue;
            }

            List<String> supportedThemes = ThemeStore.INSTANCE.getThemes(layout).stream()
                    .map(Theme::getDisplayName)
                    .collect(Collectors.toList());

            GameMode gameMode = new GameMode();
            gameMode.setModeId(layout.name());
            gameMode.setDisplayName(provider.getDisplayName());
            gameMode.setNumPlayers(provider.getNumPlayers());
            gameMode.setThemeIds(supportedThemes);
            gameModes.add(gameMode);
        }

        GameModes message = new GameModes();
        message.setModes(gameModes);

        resp.setStatus(StatusCodes.OK);
        JsonUtils.getMapper().writeValue(resp.getWriter(), message);
    }
}
