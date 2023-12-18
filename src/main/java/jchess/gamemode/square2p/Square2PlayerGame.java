package jchess.gamemode.square2p;

import jchess.common.BaseChessGame;
import jchess.common.components.MarkerType;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.components.TileComponent;
import jchess.common.events.PieceMoveEvent;
import jchess.common.theme.IIconKey;
import jchess.ecs.Entity;
import jchess.gamemode.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;

public class Square2PlayerGame extends BaseChessGame {
    private static final Logger logger = LoggerFactory.getLogger(Square2PlayerGame.class);
    private static final int numTiles = 8;

    private final Entity[][] tiles = new Entity[numTiles][numTiles];


    public Square2PlayerGame() {
        super(GameMode.Square2P.getNumPlayers());

        PieceMoveEvent pieceMoveEvent = eventManager.getEvent(PieceMoveEvent.class);
        pieceMoveEvent.addListener(event -> {
            // TODO erja, update the move history here.
        });
    }

    @Override
    public int getKingTypeId() {
        return PieceType.King.getId();
    }

    @Override
    protected IIconKey getMarkerIcon(MarkerType markerType) {
        return switch (markerType) {
            case Selection -> Theme.BoardIcons.tileMarker_selected;
            case NoAction -> Theme.BoardIcons.tileMarker_noAction;
            case YesAction -> Theme.BoardIcons.tileMarker_yesAction;
        };
    }

    @Override
    protected Entity getEntityAtPosition(int x, int y) {
        if (x < 0 || x >= numTiles) return null;
        if (y < 0 || y >= numTiles) return null;

        return tiles[y][x];
    }

    @Override
    protected void generateBoard() {
        // first pass: create entities
        for (int y = 0; y < numTiles; y++) {
            Entity[] tileRow = tiles[y];
            for (int x = 0; x < numTiles; x++) {
                tileRow[x] = entityManager.createEntity();
            }
        }

        // second pass: fill entities with components
        for (int y = 0; y < numTiles; y++) {
            Entity[] tileRow = tiles[y];
            for (int x = 0; x < numTiles; x++) {
                TileComponent tile = new TileComponent();
                tile.iconKey = ((x + y) % 2 == 0) ? Theme.BoardIcons.tileLight : Theme.BoardIcons.tileDark;
                tile.position = new Point(x, y);

                tile.neighborsByDirection.put(0, getEntityAtPosition(x, y - 1));
                tile.neighborsByDirection.put(45, getEntityAtPosition(x + 1, y - 1));
                tile.neighborsByDirection.put(90, getEntityAtPosition(x + 1, y));
                tile.neighborsByDirection.put(135, getEntityAtPosition(x + 1, y + 1));
                tile.neighborsByDirection.put(180, getEntityAtPosition(x, y + 1));
                tile.neighborsByDirection.put(225, getEntityAtPosition(x - 1, y + 1));
                tile.neighborsByDirection.put(270, getEntityAtPosition(x - 1, y));
                tile.neighborsByDirection.put(315, getEntityAtPosition(x - 1, y - 1));
                tileRow[x].tile = tile;
            }
        }

        placeRook(0, 0, false);
        placeRook(7, 0, false);
        placeRook(0, 7, true);
        placeRook(7, 7, true);

        placeKnight(1, 0, false);
        placeKnight(6, 0, false);
        placeKnight(1, 7, true);
        placeKnight(6, 7, true);

        placeBishop(2, 0, false);
        placeBishop(5, 0, false);
        placeBishop(2, 7, true);
        placeBishop(5, 7, true);

        placeQueen(3, 0, false);
        placeQueen(3, 7, true);

        placeKing(4, 0, false);
        placeKing(4, 7, true);

        for (int x = 0; x < numTiles; x++) {
            placePawn(x, 1, false);
            placePawn(x, 6, true);
        }
    }

    private Theme.PieceColor getColor(boolean isWhite) {
        return isWhite ? Theme.PieceColor.light : Theme.PieceColor.dark;
    }

    private void placeRook(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, PieceType.Rook, Theme.PieceIcons.rook, getColor(isWhite));
    }

    private void placeKnight(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, PieceType.Knight, Theme.PieceIcons.knight, getColor(isWhite));
    }

    private void placeBishop(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, PieceType.Bishop, Theme.PieceIcons.bishop, getColor(isWhite));
    }

    private void placeQueen(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, PieceType.Queen, Theme.PieceIcons.queen, getColor(isWhite));
    }

    private void placeKing(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, PieceType.King, Theme.PieceIcons.king, getColor(isWhite));
    }

    private void placePawn(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, PieceType.Pawn, Theme.PieceIcons.pawn, getColor(isWhite));
    }

    private void placePiece(int x, int y, boolean isWhite, PieceType pieceType, Theme.PieceIcons pieceIcon, Theme.PieceColor pieceColor) {
        Entity tile = getEntityAtPosition(x, y);
        if (tile == null) {
            logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
            return;
        }

        PieceIdentifier pieceIdentifier = new PieceIdentifier(
                pieceType.getId(),
                pieceType.getShortName(),
                pieceIcon.asIconKey(pieceColor),
                isWhite ? 0 : 1,
                isWhite ? 0 : 180
        );

        PieceComponent piece = new PieceComponent(this, pieceIdentifier, pieceType.getBaseMoves());
        piece.addSpecialMoves(pieceType.getSpecialRules());
        tile.piece = piece;
    }

}
