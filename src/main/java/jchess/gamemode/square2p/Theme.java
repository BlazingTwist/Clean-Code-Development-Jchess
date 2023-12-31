package jchess.gamemode.square2p;

import jchess.common.theme.IIconKey;
import jchess.common.theme.ThemeUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Theme {
    public static Map<String, String> getIconMap(File themeDirectory) {
        HashMap<String, String> iconMap = new HashMap<>();

        iconMap.put("preview", ThemeUtils.getIconPath(themeDirectory, "Preview.png"));

        File boardThemeDirectory = new File(themeDirectory, "board_square");
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
        tileLight("tile-Light.png"),
        tileDark("tile-Dark.png"),
        tileMarker_yesAction("tileMarker_yesAction.png"),
        tileMarker_noAction("tileMarker_noAction.png"),
        tileMarker_selected("tileMarker_selected.png");

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
