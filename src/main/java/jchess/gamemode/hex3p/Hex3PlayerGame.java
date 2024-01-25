package jchess.gamemode.hex3p;

import dx.schema.types.PieceType;
import dx.schema.types.Vector2I;
import jchess.common.BaseChessGame;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.components.TileComponent;
import jchess.common.events.PieceMoveEvent;
import jchess.ecs.Entity;
import jchess.gamemode.PieceStore;
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
        pieceMoveEvent.addListener(event -> {
            // TODO erja, update the move history here.
        });
    }

    @Override
    public dx.schema.types.Entity applyPerspective(dx.schema.types.Entity tile, int playerIndex) {
        if (playerIndex < 0 || playerIndex > 2) {
            throw new IllegalArgumentException("playerIndex must be 0, 1 or 2, but was " + playerIndex);
        }
        if (playerIndex == 0) return tile;
        if (tile == null || tile.getTile() == null || tile.getTile().getDisplayPos() == null) return tile;

        final double strideX = 1.73205;
        final double strideY = 3;
        final double cosine = -0.5;
        final double sine = (playerIndex == 1 ? 1 : -1) * 0.8660254;

        Vector2I displayPos = tile.getTile().getDisplayPos();
        double scaledX = (displayPos.getX() - 16) * strideX;
        double scaledY = (displayPos.getY() - 8) * strideY;
        double rotatedX = (scaledX * cosine) - (scaledY * sine);
        double rotatedY = (scaledX * sine) + (scaledY * cosine);
        displayPos.setX((int) Math.round(rotatedX / strideX) + 16);
        displayPos.setY((int) Math.round(rotatedY / strideY) + 8);
        return tile;
    }

    @Override
    protected Entity getEntityAtPosition(int x, int y) {
        if (x < 0 || x >= numTilesHorizontal) return null;
        if (y < 0 || y >= numTilesVertical) return null;

        return tiles[y][x];
    }

    @Override
    public void createPiece(Entity targetTile, PieceType pieceType, int ownerId) {
        for (Hex3pPieces piece : Hex3pPieces.values()) {
            if (piece.getPieceType() == pieceType) {
                int direction = ((ownerId - 3) * (-120)) % 360; // [0, 240, 120]
                placePiece(targetTile, ownerId, direction, piece);
                return;
            }
        }
        logger.error("unable to place piece with pieceType '" + pieceType + "'. PieceType does not exist.");
    }

    @Override
    protected void generateBoard() {
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
                TileComponent tile = new TileComponent(new Point(x, y), x % 3);

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

        placePawn(7, 15, PLAYER_LIGHT);
        placePawn(25, 15, PLAYER_LIGHT);

        placePawn(31, 9, PLAYER_MEDIUM);
        placePawn(22, 0, PLAYER_MEDIUM);

        placePawn(10, 0, PLAYER_DARK);
        placePawn(1, 9, PLAYER_DARK);

        for (int i = 0; i < 9; i++) {
            placePawn(8 + (i * 2), 14, PLAYER_LIGHT);
            placePawn(29 - i, 9 - i, PLAYER_MEDIUM);
            placePawn(11 - i, 1 + i, PLAYER_DARK);
        }

        placeArcher(9, 15, PLAYER_LIGHT);
        placeArcher(15, 15, PLAYER_LIGHT);
        placeArcher(17, 15, PLAYER_LIGHT);
        placeArcher(23, 15, PLAYER_LIGHT);

        placeArcher(30, 8, PLAYER_MEDIUM);
        placeArcher(27, 5, PLAYER_MEDIUM);
        placeArcher(26, 4, PLAYER_MEDIUM);
        placeArcher(23, 1, PLAYER_MEDIUM);

        placeArcher(9, 1, PLAYER_DARK);
        placeArcher(6, 4, PLAYER_DARK);
        placeArcher(5, 5, PLAYER_DARK);
        placeArcher(2, 8, PLAYER_DARK);

        placePegasus(11, 15, PLAYER_LIGHT);
        placePegasus(21, 15, PLAYER_LIGHT);

        placePegasus(29, 7, PLAYER_MEDIUM);
        placePegasus(24, 2, PLAYER_MEDIUM);

        placePegasus(8, 2, PLAYER_DARK);
        placePegasus(3, 7, PLAYER_DARK);

        placeCatapult(13, 15, PLAYER_LIGHT);
        placeCatapult(19, 15, PLAYER_LIGHT);

        placeCatapult(28, 6, PLAYER_MEDIUM);
        placeCatapult(25, 3, PLAYER_MEDIUM);

        placeCatapult(7, 3, PLAYER_DARK);
        placeCatapult(4, 6, PLAYER_DARK);
    }

    private void placeRook(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.Rook);
    }

    private void placeKnight(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.Knight);
    }

    private void placeBishop(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.Bishop);
    }

    private void placeQueen(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.Queen);
    }

    private void placeKing(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.King);
    }

    private void placePawn(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.Pawn);
    }

    private void placeArcher(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.Archer);
    }

    private void placePegasus(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.Pegasus);
    }

    private void placeCatapult(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, Hex3pPieces.Catapult);
    }

    private void placePiece(int x, int y, int playerColor, Hex3pPieces piece) {
        Entity tile = getEntityAtPosition(x, y);
        if (tile == null) {
            logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
            return;
        }

        int direction = ((playerColor - 3) * (-120)) % 360; // [0, 240, 120]
        placePiece(tile, playerColor, direction, piece);
    }

    private void placePiece(Entity tile, int ownerId, int direction, Hex3pPieces piece) {
        PieceStore.PieceDefinition pieceDefinition = piece.getPieceDefinition();
        PieceIdentifier pieceIdentifier = new PieceIdentifier(
                piece.getPieceType(),
                pieceDefinition.shortName(),
                ownerId,
                direction
        );

        PieceComponent pieceComp = new PieceComponent(this, pieceIdentifier, pieceDefinition.baseMoves());
        pieceComp.addSpecialMoves(pieceDefinition.specialRules());
        tile.piece = pieceComp;
    }
}
