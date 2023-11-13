package jchess.game.layout.square2p;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

public class Theme {

    public final Image preview;
    public final BoardTheme board;
    public final PieceTheme piece;
    public final UiTheme ui;

    public Theme(File themeDirectory) {
        preview = loadImage(themeDirectory, "Preview.png");
        board = new BoardTheme(new File(themeDirectory, "board_square"));
        piece = new PieceTheme(new File(themeDirectory, "piece"));
        ui = new UiTheme(new File(themeDirectory, "ui"));
    }

    private static Image loadImage(File directory, String imageName) {
        File imageFile = new File(directory, imageName);
        try {
            return ImageIO.read(imageFile);
        } catch (IOException e) {
            System.err.println("failed to read image '" + imageFile.getAbsolutePath() + "'");
            e.printStackTrace();
            return null;
        }
    }

    public static class BoardTheme {
        public final Image tileLight;
        public final Image tileDark;

        public final Image tileMarker_yesAction;
        public final Image tileMarker_noAction;
        public final Image tileMarker_selected;

        public BoardTheme(File themeDirectory) {
            tileLight = loadImage(themeDirectory, "tile-Light.png");
            tileDark = loadImage(themeDirectory, "tile-Dark.png");

            tileMarker_yesAction = loadImage(themeDirectory, "tileMarker_yesAction.png");
            tileMarker_noAction = loadImage(themeDirectory, "tileMarker_noAction.png");
            tileMarker_selected = loadImage(themeDirectory, "tileMarker_selected.png");
        }
    }

    public static class PieceTheme {
        public final Image rookBlack;
        public final Image rookWhite;
        public final Image knightBlack;
        public final Image knightWhite;
        public final Image bishopBlack;
        public final Image bishopWhite;
        public final Image queenBlack;
        public final Image queenWhite;
        public final Image kingBlack;
        public final Image kingWhite;
        public final Image pawnBlack;
        public final Image pawnWhite;

        public PieceTheme(File themeDirectory) {
            rookBlack = loadImage(themeDirectory, "Rook-B.png");
            rookWhite = loadImage(themeDirectory, "Rook-W.png");
            knightBlack = loadImage(themeDirectory, "Knight-B.png");
            knightWhite = loadImage(themeDirectory, "Knight-W.png");
            bishopBlack = loadImage(themeDirectory, "Bishop-B.png");
            bishopWhite = loadImage(themeDirectory, "Bishop-W.png");
            queenBlack = loadImage(themeDirectory, "Queen-B.png");
            queenWhite = loadImage(themeDirectory, "Queen-W.png");
            kingBlack = loadImage(themeDirectory, "King-B.png");
            kingWhite = loadImage(themeDirectory, "King-W.png");
            pawnBlack = loadImage(themeDirectory, "Pawn-B.png");
            pawnWhite = loadImage(themeDirectory, "Pawn-W.png");
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
