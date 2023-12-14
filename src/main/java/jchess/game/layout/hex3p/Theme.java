package jchess.game.layout.hex3p;

import jchess.game.common.theme.IIconKey;
import jchess.game.common.theme.ThemeUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Theme {
    public static Map<String, String> getIconMap(File themeDirectory) {
        HashMap<String, String> iconMap = new HashMap<>();

        iconMap.put("preview", ThemeUtils.getIconPath(themeDirectory, "Preview.png"));

        File boardThemeDirectory = new File(themeDirectory, "board_hex3");
        for (BoardIcons boardIcon : BoardIcons.values()) {
            String iconPath = ThemeUtils.getIconPath(boardThemeDirectory, boardIcon.getFileName());
            iconMap.put(boardIcon.getIconId(), iconPath);
        }

        File pieceThemeDirectory = new File(themeDirectory, "piece");
        for (PieceIcons pieceIcon : PieceIcons.values()) {
            for (PieceColor pieceColor : PieceColor.values()) {
                String iconPath = ThemeUtils.getIconPath(pieceThemeDirectory, pieceIcon.getFileName(pieceColor));
                iconMap.put(pieceIcon.getIconKey(pieceColor), iconPath);
            }
        }

        return iconMap;
    }

    public enum BoardIcons implements IIconKey {
        hexLight("hex-Light.png"),
        hexMedium("hex-Medium.png"),
        hexDark("hex-Dark.png"),
        hexMarker_yesAction("hexMarker_yesAction.png"),
        hexMarker_noAction("hexMarker_noAction.png"),
        hexMarker_selected("hexMarker_selected.png");

        private final String fileName;

        BoardIcons(String fileName) {
            this.fileName = fileName;
        }

        public String getIconId() {
            return "board." + this.name();
        }

        public String getFileName() {
            return fileName;
        }
    }

    public enum PieceColor {
        light("-W.png"),
        medium("-M.png"),
        dark("-B.png");

        private final String fileNameSuffix;

        PieceColor(String fileNameSuffix) {
            this.fileNameSuffix = fileNameSuffix;
        }

        public String getFileNameSuffix() {
            return fileNameSuffix;
        }
    }

    public enum PieceIcons {
        rook("Rook"),
        knight("Knight"),
        bishop("Bishop"),
        queen("Queen"),
        king("King"),
        pawn("Pawn");

        private final String fileName;

        PieceIcons(String fileName) {
            this.fileName = fileName;
        }

        public String getIconKey(PieceColor color) {
            return "piece." + this.name() + "." + color.name();
        }

        public String getFileName(PieceColor color) {
            return fileName + color.getFileNameSuffix();
        }

        public IIconKey asIconKey(PieceColor color) {
            return () -> getIconKey(color);
        }
    }

}
