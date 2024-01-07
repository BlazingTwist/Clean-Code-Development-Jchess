package jchess.gamemode.square2p;

import dx.schema.types.Vector2I;
import jchess.common.BaseChessGame;
import jchess.common.components.MarkerType;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.components.TileComponent;
import jchess.common.events.PieceMoveEvent;
import jchess.common.theme.IIconKey;
import jchess.ecs.Entity;
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
        pieceMoveEvent.addListener(event -> {
            // TODO erja, update the move history here.
        });
    }

    @Override
    public dx.schema.types.Entity applyPerspective(dx.schema.types.Entity tile, int playerIndex) {
        if (playerIndex < 0 || playerIndex > 1) {
            throw new IllegalArgumentException("playerIndex must be 0 or 1, but was " + playerIndex);
        }
        if (playerIndex == 0) return tile;
        if (tile == null || tile.getTile() == null || tile.getTile().getDisplayPos() == null) return tile;

        Vector2I displayPos = tile.getTile().getDisplayPos();
        displayPos.setX(numTiles - displayPos.getX());
        displayPos.setY(numTiles - displayPos.getY());
        return tile;
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
    public void createPiece(Entity targetTile, dx.schema.types.PieceType pieceType, int ownerId) {
        for (Square2pPieces pieceType : Square2pPieces.values()) {
            if (pieceType.getId() == pieceType) {
                placePiece(
                        targetTile, ownerId,
                        ownerId == 0 ? 0 : 180,
                        pieceType,
                        ownerId == 0 ? Theme.PieceColor.light : Theme.PieceColor.dark
                );
                return;
            }
        }
        logger.error("unable to place piece with pieceType '" + pieceType + "'. PieceType does not exist.");
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
        placePiece(x, y, isWhite, Square2pPieces.Rook, getColor(isWhite));
    }

    private void placeKnight(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, Square2pPieces.Knight, getColor(isWhite));
    }

    private void placeBishop(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, Square2pPieces.Bishop, getColor(isWhite));
    }

    private void placeQueen(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, Square2pPieces.Queen, getColor(isWhite));
    }

    private void placeKing(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, Square2pPieces.King, getColor(isWhite));
    }

    private void placePawn(int x, int y, boolean isWhite) {
        placePiece(x, y, isWhite, Square2pPieces.Pawn, getColor(isWhite));
    }

    private void placePiece(int x, int y, boolean isWhite, Square2pPieces pieceType, Theme.PieceColor pieceColor) {
        Entity tile = getEntityAtPosition(x, y);
        if (tile == null) {
            logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
            return;
        }

        placePiece(tile, isWhite ? 0 : 1, isWhite ? 0 : 180, pieceType, pieceColor);
    }

    private void placePiece(Entity tile, int ownerId, int direction, Square2pPieces pieceType, Theme.PieceColor pieceColor) {
        PieceIdentifier pieceIdentifier = new PieceIdentifier(
                pieceType.getPieceType(),
                pieceType.getPieceDefinition().shortName(),
                pieceType.getIcon().asIconKey(pieceColor), // TODO erja
                ownerId,
                direction
        );

        PieceComponent piece = new PieceComponent(this, pieceIdentifier, pieceType.getBaseMoves());
        piece.addSpecialMoves(pieceType.getSpecialRules());
        tile.piece = piece;
    }

}
