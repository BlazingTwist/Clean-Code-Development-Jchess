package jchess.game.layout.hex3p;

import jchess.ecs.Entity;
import jchess.game.common.BaseChessGame;
import jchess.game.common.theme.IIconKey;
import jchess.game.common.events.PieceMoveEvent;
import jchess.game.common.events.RenderEvent;
import jchess.game.common.components.MarkerType;
import jchess.game.common.components.PieceComponent;
import jchess.game.common.components.PieceIdentifier;
import jchess.game.common.components.TileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;

public class Hex3PlayerGame extends BaseChessGame {
    private static final Logger logger = LoggerFactory.getLogger(Hex3PlayerGame.class);
    private static final int numTilesHorizontal = 17 + 16;
    private static final int numTilesVertical = 17;
    private static final int PLAYER_LIGHT = 0;
    private static final int PLAYER_MEDIUM = 1;
    private static final int PLAYER_DARK = 2;

    private final Entity[][] tiles = new Entity[numTilesVertical][numTilesHorizontal];

    public Hex3PlayerGame() {
        super(3);

        PieceMoveEvent pieceMoveEvent = eventManager.getEvent(PieceMoveEvent.class);
        pieceMoveEvent.addPreEventListener(event -> {
            // TODO erja, update the move history here.
        });
    }

    @Override
    public void start() {
        generateBoard();
        eventManager.getEvent(RenderEvent.class).fire(null);
    }

    @Override
    protected IIconKey getMarkerIcon(MarkerType markerType) {
        return switch (markerType) {
            case Selection -> Theme.BoardIcons.hexMarker_selected;
            case NoAction -> Theme.BoardIcons.hexMarker_noAction;
            case YesAction -> Theme.BoardIcons.hexMarker_yesAction;
        };
    }

    @Override
    protected Entity getEntityAtPosition(int x, int y) {
        if (x < 0 || x >= numTilesHorizontal) return null;
        if (y < 0 || y >= numTilesVertical) return null;

        return tiles[y][x];
    }

    private void generateBoard() {
        // first pass: create entities
        for (int y = 0; y < numTilesVertical; y++) {
            Entity[] tileRow = tiles[y];
            int x0 = Math.abs(8 - y);
            int x1 = 32 - x0;
            for (int x = x0; x <= x1; x += 2) {
                tileRow[x] = entityManager.createEntity();
            }
        }

        // second pass: fill entities with components
        for (int y = 0; y < numTilesVertical; y++) {
            Entity[] tileRow = tiles[y];
            int x0 = Math.abs(8 - y);
            int x1 = 32 - x0;
            for (int x = x0; x <= x1; x += 2) {
                TileComponent tile = new TileComponent();
                tile.iconKey = (x % 3 == 0) ? Theme.BoardIcons.hexLight
                        : ((x % 3 == 1) ? Theme.BoardIcons.hexMedium
                        : Theme.BoardIcons.hexDark);
                tile.position = new Point(x, y);

                tile.neighborsByDirection.put(0, getEntityAtPosition(x, y - 2));
                tile.neighborsByDirection.put(30, getEntityAtPosition(x + 1, y - 1));
                tile.neighborsByDirection.put(60, getEntityAtPosition(x + 3, y - 1));
                tile.neighborsByDirection.put(90, getEntityAtPosition(x + 2, y));
                tile.neighborsByDirection.put(120, getEntityAtPosition(x + 3, y + 1));
                tile.neighborsByDirection.put(150, getEntityAtPosition(x + 1, y + 1));
                tile.neighborsByDirection.put(180, getEntityAtPosition(x, y + 2));
                tile.neighborsByDirection.put(210, getEntityAtPosition(x - 1, y + 1));
                tile.neighborsByDirection.put(240, getEntityAtPosition(x - 3, y + 1));
                tile.neighborsByDirection.put(270, getEntityAtPosition(x - 2, y));
                tile.neighborsByDirection.put(300, getEntityAtPosition(x - 3, y - 1));
                tile.neighborsByDirection.put(330, getEntityAtPosition(x - 1, y - 1));

                tileRow[x].tile = tile;
            }
        }

        placeRook(8, 16, PLAYER_LIGHT);
        placeRook(24, 16, PLAYER_LIGHT);
        placeRook(32, 8, PLAYER_MEDIUM);
        placeRook(24, 0, PLAYER_MEDIUM);
        placeRook(8, 0, PLAYER_DARK);
        placeRook(0, 8, PLAYER_DARK);

        placeKnight(10, 16, PLAYER_LIGHT);
        placeKnight(22, 16, PLAYER_LIGHT);
        placeKnight(31, 7, PLAYER_MEDIUM);
        placeKnight(25, 1, PLAYER_MEDIUM);
        placeKnight(7, 1, PLAYER_DARK);
        placeKnight(1, 7, PLAYER_DARK);

        placeBishop(12, 16, PLAYER_LIGHT);
        placeBishop(16, 16, PLAYER_LIGHT);
        placeBishop(20, 16, PLAYER_LIGHT);
        placeBishop(30, 6, PLAYER_MEDIUM);
        placeBishop(28, 4, PLAYER_MEDIUM);
        placeBishop(26, 2, PLAYER_MEDIUM);
        placeBishop(6, 2, PLAYER_DARK);
        placeBishop(4, 4, PLAYER_DARK);
        placeBishop(2, 6, PLAYER_DARK);

        placeQueen(14, 16, PLAYER_LIGHT);
        placeQueen(29, 5, PLAYER_MEDIUM);
        placeQueen(5, 3, PLAYER_DARK);

        placeKing(18, 16, PLAYER_LIGHT);
        placeKing(27, 3, PLAYER_MEDIUM);
        placeKing(3, 5, PLAYER_DARK);

        for (int i = 0; i < 10; i++) {
            placePawn(7 + (i * 2), 15, PLAYER_LIGHT);
            placePawn(31 - i, 9 - i, PLAYER_MEDIUM);
            placePawn(10 - i, i, PLAYER_DARK);
        }
        for (int i = 0; i < 9; i++) {
            placePawn(8 + (i * 2), 14, PLAYER_LIGHT);
            placePawn(29 - i, 9 - i, PLAYER_MEDIUM);
            placePawn(11 - i, 1 + i, PLAYER_DARK);
        }
    }

    private static Theme.PieceColor getPlayerColor(int playerId) {
        return switch (playerId) {
            case 0 -> Theme.PieceColor.light;
            case 1 -> Theme.PieceColor.medium;
            case 2 -> Theme.PieceColor.dark;
            default -> throw new IllegalArgumentException("'playerId' must be 0, 1 or 2, but was '" + playerId + "'");
        };
    }

    private void placeRook(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Rook, Theme.PieceIcons.rook, getPlayerColor(playerColor));
    }

    private void placeKnight(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Knight, Theme.PieceIcons.knight, getPlayerColor(playerColor));
    }

    private void placeBishop(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Bishop, Theme.PieceIcons.bishop, getPlayerColor(playerColor));
    }

    private void placeQueen(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Queen, Theme.PieceIcons.queen, getPlayerColor(playerColor));
    }

    private void placeKing(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.King, Theme.PieceIcons.king, getPlayerColor(playerColor));
    }

    private void placePawn(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Pawn, Theme.PieceIcons.pawn, getPlayerColor(playerColor));
    }

    private void placePiece(int x, int y, int playerColor, PieceMoveRules.PieceType pieceType, Theme.PieceIcons pieceIcon, Theme.PieceColor color) {
        Entity tile = getEntityAtPosition(x, y);
        if (tile == null) {
            logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
            return;
        }

        PieceIdentifier pieceId = new PieceIdentifier(
                pieceType.getId(),
                pieceType.getShortName(),
                pieceIcon.asIconKey(color),
                playerColor,
                ((playerColor - 3) * (-120)) % 360 // [0, 240, 120]
        );
        PieceComponent piece = new PieceComponent();
        piece.identifier = pieceId;
        piece.moveSet = new PieceMoveRules(pieceType, pieceId);
        tile.piece = piece;
    }
}
