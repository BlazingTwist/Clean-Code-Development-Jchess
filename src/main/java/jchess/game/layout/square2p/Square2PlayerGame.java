package jchess.game.layout.square2p;

import jchess.ecs.Entity;
import jchess.game.common.BaseChessGame;
import jchess.game.common.events.PieceMoveEvent;
import jchess.game.common.events.RenderEvent;
import jchess.game.common.marker.MarkerType;
import jchess.game.common.piece.PieceComponent;
import jchess.game.common.piece.PieceIdentifier;
import jchess.game.common.tile.TileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;

public class Square2PlayerGame extends BaseChessGame {
    private static final Logger logger = LoggerFactory.getLogger(Square2PlayerGame.class);
    private static final int numTiles = 8;

    private final Entity[][] tiles = new Entity[numTiles][numTiles];


    public Square2PlayerGame() {
        super(2);

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
    protected String getMarkerIcon(MarkerType markerType) {
        return switch (markerType) {
            case Selection -> "board.tileMarker_selected";
            case NoAction -> "board.tileMarker_noAction";
            case YesAction -> "board.tileMarker_yesAction";
        };
    }

    @Override
    protected Entity getEntityAtPosition(int x, int y) {
        if (x < 0 || x >= numTiles) return null;
        if (y < 0 || y >= numTiles) return null;

        return tiles[y][x];
    }

    private void generateBoard() {
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
                tile.iconId = ((x + y) % 2 == 0) ? "board.tileLight" : "board.tileDark";
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

    private void placeRook(int x, int y, boolean isWhite) {
        String icon = isWhite ? "piece.rookWhite" : "piece.rookBlack";
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Rook, icon);
    }

    private void placeKnight(int x, int y, boolean isWhite) {
        String icon = isWhite ? "piece.knightWhite" : "piece.knightBlack";
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Knight, icon);
    }

    private void placeBishop(int x, int y, boolean isWhite) {
        String icon = isWhite ? "piece.bishopWhite" : "piece.bishopBlack";
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Bishop, icon);
    }

    private void placeQueen(int x, int y, boolean isWhite) {
        String icon = isWhite ? "piece.queenWhite" : "piece.queenBlack";
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Queen, icon);
    }

    private void placeKing(int x, int y, boolean isWhite) {
        String icon = isWhite ? "piece.kingWhite" : "piece.kingBlack";
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.King, icon);
    }

    private void placePawn(int x, int y, boolean isWhite) {
        String icon = isWhite ? "piece.pawnWhite" : "piece.pawnBlack";
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Pawn, icon);
    }

    private void placePiece(int x, int y, boolean isWhite, PieceMoveRules.PieceType pieceType, String iconId) {
        Entity tile = getEntityAtPosition(x, y);
        if (tile == null) {
            logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
            return;
        }

        PieceIdentifier pieceIdentifier = new PieceIdentifier(
                pieceType.getId(),
                pieceType.getShortName(),
                iconId,
                isWhite ? 0 : 1,
                isWhite ? 0 : 180
        );

        PieceComponent piece = new PieceComponent();
        piece.identifier = pieceIdentifier;
        piece.moveSet = new PieceMoveRules(pieceType, pieceIdentifier);
        tile.piece = piece;
    }

}
