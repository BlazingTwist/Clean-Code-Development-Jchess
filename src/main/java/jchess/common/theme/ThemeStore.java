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
import util.ResourceHelper;
import util.ResourceHelper.ResourceFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum ThemeStore {
    INSTANCE;

    private final List<Theme> themes = new ArrayList<>();

    ThemeStore() {
        try {
            ObjectMapper mapper = JsonUtils.getMapper();
            ResourceFile gameConfigFile = ResourceHelper.getResource("/jchess/GameConfig.json");
            GameConfig gameConfig = mapper.readValue(gameConfigFile.toInputStream(), GameConfig.class);
            for (String themePath : gameConfig.getThemes()) {
                ResourceFile themeFile = resolvePath(gameConfigFile, themePath);
                Theme theme = mapper.readValue(themeFile.toInputStream(), Theme.class);
                resolveThemePaths(themeFile, theme);
                themes.add(theme);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read GameConfig", e);
        }
    }

    private static ResourceFile resolvePath(ResourceFile declaringFile, String path) {
        if (path.startsWith("./")) {
            return declaringFile.parent().resolve(path).normalize();
        } else {
            return ResourceHelper.getResource(path);
        }
    }

    private static String resolveIcon(ResourceFile declaringFile, String path) {
        if (path.startsWith("./")) {
            return ThemeUtils.getIconPath(declaringFile.parent(), path);
        } else {
            return ThemeUtils.sanitizeIconPath(path);
        }
    }

    private static void resolveThemePaths(ResourceFile themeFile, Theme theme) {
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

    private static void resolveLayoutPaths(ResourceFile themeFile, LayoutTheme layout) {
        layout.setTiles(
                layout.getTiles().stream()
                        .map(path -> resolveIcon(themeFile, path))
                        .toList()
        );

        for (Marker marker : layout.getMarkers()) {
            marker.setIcon(resolveIcon(themeFile, marker.getIcon()));
        }
    }

    private static void resolvePiecePaths(ResourceFile themeFile, Piece piece) {
        piece.setPathPrefix(resolveIcon(themeFile, piece.getPathPrefix()));
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public List<Theme> getThemes(GameModeStore.GameModeProvider gameModeProvider) {
        return themes.stream()
                .filter(theme -> {
                    if (theme.getBoardTheme() == null) return false;
                    return theme.getBoardTheme().getLayouts().stream().anyMatch(layout -> layout.getLayoutId() == gameModeProvider.layoutId());
                })
                .filter(theme -> theme.getPiecesTheme() != null && theme.getPiecesTheme().getPlayerColors().size() >= gameModeProvider.numPlayers())
                .filter(theme -> {
                    if (theme.getPiecesTheme() == null) return false;

                    Set<PieceType> themePieceTypes = theme.getPiecesTheme().getPieces().stream().map(Piece::getPieceType).collect(Collectors.toSet());
                    Set<PieceType> modePieceTypes = gameModeProvider.pieceStore().getPieces();
                    return themePieceTypes.containsAll(modePieceTypes); // require that theme supports all pieces of the gameMode
                })
                .toList();
    }
}
