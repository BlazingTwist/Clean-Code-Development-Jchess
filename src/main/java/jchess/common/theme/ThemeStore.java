package jchess.common.theme;

import com.fasterxml.jackson.databind.ObjectMapper;
import dx.schema.conf.BoardTheme;
import dx.schema.conf.GameConfig;
import dx.schema.conf.LayoutTheme;
import dx.schema.conf.Marker;
import dx.schema.conf.Piece;
import dx.schema.conf.PiecesTheme;
import dx.schema.conf.Theme;
import dx.schema.types.PieceType;
import jchess.gamemode.GameModeStore;
import jchess.server.util.JsonUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum ThemeStore {
    INSTANCE;

    private final List<Theme> themes = new ArrayList<>();

    ThemeStore() {
        try {
            ObjectMapper mapper = JsonUtils.getMapper();
            File gameConfigFile = new File(Objects.requireNonNull(ThemeStore.class.getResource("/jchess/GameConfig.json")).toURI());
            GameConfig gameConfig = mapper.readValue(gameConfigFile, GameConfig.class);
            for (String themePath : gameConfig.getThemes()) {
                File themeFile = resolvePath(gameConfigFile, themePath);
                Theme theme = mapper.readValue(themeFile, Theme.class);
                resolveThemePaths(themeFile, theme);
                themes.add(theme);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read GameConfig", e);
        }
    }

    private static File resolvePath(File declaringFile, String path) throws URISyntaxException {
        if (path.startsWith("./")) {
            return new File(declaringFile.getParentFile(), path);
        } else {
            return new File(Objects.requireNonNull(ThemeStore.class.getResource(path)).toURI());
        }
    }

    private static String resolveIcon(File declaringFile, String path) {
        if (path.startsWith("./")) {
            return ThemeUtils.getIconPath(declaringFile.getParentFile(), path);
        } else {
            return ThemeUtils.sanitizeIconPath(path);
        }
    }

    private static void resolveThemePaths(File themeFile, Theme theme) {
        Optional.ofNullable(theme.getBoardTheme()).map(BoardTheme::getLayouts).ifPresent(layouts -> {
            for (LayoutTheme layout : layouts) {
                resolveLayoutPaths(themeFile, layout);
            }
        });

        Optional.ofNullable(theme.getPiecesTheme()).map(PiecesTheme::getPieces).ifPresent(pieces -> {
            for (Piece piece : pieces) {
                resolvePiecePaths(themeFile, piece);
            }
        });
    }

    private static void resolveLayoutPaths(File themeFile, LayoutTheme layout) {
        layout.setTiles(
                layout.getTiles().stream()
                        .map(path -> resolveIcon(themeFile, path))
                        .toList()
        );

        for (Marker marker : layout.getMarkers()) {
            marker.setIcon(resolveIcon(themeFile, marker.getIcon()));
        }
    }

    private static void resolvePiecePaths(File themeFile, Piece piece) {
        piece.setPathPrefix(resolveIcon(themeFile, piece.getPathPrefix()));
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public List<Theme> getThemes(LayoutTheme.LayoutId layoutId) {
        GameModeStore.GameModeProvider gameMode = GameModeStore.getGameMode(layoutId);

        return themes.stream()
                .filter(theme -> {
                    if (theme.getBoardTheme() == null) return false;
                    return theme.getBoardTheme().getLayouts().stream().anyMatch(layout -> layout.getLayoutId() == layoutId);
                })
                .filter(theme -> theme.getPiecesTheme() != null && theme.getPiecesTheme().getPlayerColors().size() >= gameMode.getNumPlayers())
                .filter(theme -> {
                    if (theme.getPiecesTheme() == null) return false;

                    Set<PieceType> themePieceTypes = theme.getPiecesTheme().getPieces().stream().map(Piece::getPieceType).collect(Collectors.toSet());
                    Set<PieceType> modePieceTypes = gameMode.getPieceStore().getPieces();
                    return themePieceTypes.containsAll(modePieceTypes); // require that theme supports all pieces of the gameMode
                })
                .toList();
    }
}
