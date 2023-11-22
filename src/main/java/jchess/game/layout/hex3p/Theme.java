package jchess.game.layout.hex3p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

public class Theme {
    private static final Logger logger = LoggerFactory.getLogger(Theme.class);
    public final Image preview;
    public final BoardTheme board;
    public final PieceTheme piece;
    public final UiTheme ui;

    public Theme(File themeDirectory) {
        preview = loadImage(themeDirectory, "Preview.png");
        board = new BoardTheme(new File(themeDirectory, "board_hex3"));
        piece = new PieceTheme(new File(themeDirectory, "piece"));
        ui = new UiTheme(new File(themeDirectory, "ui"));
    }

    private static Image loadImage(File directory, String imageName) {
        File imageFile = new File(directory, imageName);
        try {
            return ImageIO.read(imageFile);
        } catch (IOException e) {
            logger.error("failed to read image '{}'", imageFile.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }

    public static class BoardTheme {
        public final Image hexLight;
        public final Image hexMedium;
        public final Image hexDark;

        public final Image hexMarker_yesAction;
        public final Image hexMarker_noAction;
        public final Image hexMarker_selected;

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
            public final Image light;
            public final Image medium;
            public final Image dark;

            public PieceImages(File themeDirectory, String baseName) {
                light = loadImage(themeDirectory, baseName + "-W.png");
                medium = loadImage(themeDirectory, baseName + "-M.png");
                dark = loadImage(themeDirectory, baseName + "-B.png");
            }

            public Image get(int playerId) {
                return switch (playerId) {
                    case 0 -> light;
                    case 1 -> medium;
                    case 2 -> dark;
                    default ->
                            throw new IllegalArgumentException("'playerId' must be 0, 1 or 2, but was '" + playerId + "'");
                };
            }
        }
    }

    public static class UiTheme {
        public final Image addTabIcon;
        public final Image clickedAddTabIcon;

        public UiTheme(File themeDirectory) {
            addTabIcon = loadImage(themeDirectory, "add-tab-icon.png");
            clickedAddTabIcon = loadImage(themeDirectory, "clicked-add-tab-icon.png");
        }
    }
}
