package jchess.game.layout.hex3p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Theme {
    private static final Logger logger = LoggerFactory.getLogger(Theme.class);

    private static File resourceRootDir;

    static {
        try {
            resourceRootDir = new File(Theme.class.getResource("/").toURI());
        } catch (URISyntaxException e) {
            logger.error("", e);
        }
    }

    public final String preview;
    public final BoardTheme board;
    public final PieceTheme piece;
    public final UiTheme ui;

    public Theme(File themeDirectory) {
        preview = loadImage(themeDirectory, "Preview.png");
        board = new BoardTheme(new File(themeDirectory, "board_hex3"));
        piece = new PieceTheme(new File(themeDirectory, "piece"));
        ui = new UiTheme(new File(themeDirectory, "ui"));
    }

    private static String loadImage(File directory, String imageName) {
        File imageFile = new File(directory, imageName);
        return imageFile.getAbsolutePath()
                .replace(resourceRootDir.getAbsolutePath(), "")
                .replace("\\", "/")
                .replaceFirst("^/", "");

        /*try {
            return ImageIO.read(imageFile);
        } catch (Exception e) {
            logger.error("failed to read image '{}'", imageFile.getAbsolutePath());
            e.printStackTrace();
            return null;
        }*/
    }

    public Map<String, String> getIconMap() {
        try {
            HashMap<String, String> iconMap = new HashMap<>();

            iconMap.put("preview", preview);

            for (Field boardThemeField : BoardTheme.class.getFields()) {
                String icon = (String) boardThemeField.get(board);
                iconMap.put("board." + boardThemeField.getName(), icon);
            }

            for (Field pieceThemeField : PieceTheme.class.getFields()) {
                PieceTheme.PieceImages images = (PieceTheme.PieceImages) pieceThemeField.get(piece);
                iconMap.put("piece." + pieceThemeField.getName() + ".light", images.light);
                iconMap.put("piece." + pieceThemeField.getName() + ".dark", images.dark);
                iconMap.put("piece." + pieceThemeField.getName() + ".medium", images.medium);
            }

            for (Field uiThemeField : UiTheme.class.getFields()) {
                String icon = (String) uiThemeField.get(ui);
                iconMap.put("ui." + uiThemeField.getName(), icon);
            }

            return iconMap;
        } catch (IllegalAccessException e) {
            logger.error("Failed to create IconMap for theme.", e);
            throw new RuntimeException(e);
        }
    }

    public static class BoardTheme {
        public final String hexLight;
        public final String hexMedium;
        public final String hexDark;

        public final String hexMarker_yesAction;
        public final String hexMarker_noAction;
        public final String hexMarker_selected;

        public BoardTheme(File themeDirectory) {
            hexLight = loadImage(themeDirectory, "hex-Light.png");
            hexMedium = loadImage(themeDirectory, "hex-Medium.png");
            hexDark = loadImage(themeDirectory, "hex-Dark.png");

            hexMarker_yesAction = loadImage(themeDirectory, "hexMarker_yesAction.png");
            hexMarker_noAction = loadImage(themeDirectory, "hexMarker_noAction.png");
            hexMarker_selected = loadImage(themeDirectory, "hexMarker_selected.png");
        }
    }

    public static class PieceTheme {
        public final PieceImages rook;
        public final PieceImages knight;
        public final PieceImages bishop;
        public final PieceImages queen;
        public final PieceImages king;
        public final PieceImages pawn;

        public PieceTheme(File themeDirectory) {
            rook = new PieceImages(themeDirectory, "Rook");
            knight = new PieceImages(themeDirectory, "Knight");
            bishop = new PieceImages(themeDirectory, "Bishop");
            queen = new PieceImages(themeDirectory, "Queen");
            king = new PieceImages(themeDirectory, "King");
            pawn = new PieceImages(themeDirectory, "Pawn");
        }

        public static class PieceImages {
            public final String light;
            public final String medium;
            public final String dark;

            public PieceImages(File themeDirectory, String baseName) {
                light = loadImage(themeDirectory, baseName + "-W.png");
                medium = loadImage(themeDirectory, baseName + "-M.png");
                dark = loadImage(themeDirectory, baseName + "-B.png");
            }

            public String get(int playerId) {
                return switch (playerId) {
                    case 0 -> light;
                    case 1 -> medium;
                    case 2 -> dark;
                    default -> throw new IllegalArgumentException("'playerId' must be 0, 1 or 2, but was '" + playerId + "'");
                };
            }
        }
    }

    public static class UiTheme {
        public final String addTabIcon;
        public final String clickedAddTabIcon;

        public UiTheme(File themeDirectory) {
            addTabIcon = loadImage(themeDirectory, "add-tab-icon.png");
            clickedAddTabIcon = loadImage(themeDirectory, "clicked-add-tab-icon.png");
        }
    }
}
