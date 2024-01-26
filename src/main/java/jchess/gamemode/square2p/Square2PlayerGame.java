package jchess.gamemode.square2p;

import dx.schema.types.Vector2I;
import jchess.common.BaseChessGame;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.components.TileComponent;
import jchess.ecs.Entity;
import jchess.gamemode.IPieceLayoutProvider;
import jchess.gamemode.PieceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;

public class Square2PlayerGame extends BaseChessGame {
    private static final Logger logger = LoggerFactory.getLogger(Square2PlayerGame.class);
    private static final int numTiles = 8;

    private final Entity[][] tiles = new Entity[numTiles][numTiles];


    public Square2PlayerGame(PieceStore pieceStore, IPieceLayoutProvider layoutProvider) {
        super(2, pieceStore, layoutProvider);
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
    protected Entity getEntityAtPosition(int x, int y) {
        if (x < 0 || x >= numTiles) return null;
        if (y < 0 || y >= numTiles) return null;

        return tiles[y][x];
    }

    @Override
    public void createPiece(Entity targetTile, dx.schema.types.PieceType pieceType, int ownerId) {
        for (Square2pPieces piece : Square2pPieces.values()) {
            if (piece.getPieceType() == pieceType) {
                placePiece(
                        targetTile, ownerId,
                        ownerId == 0 ? 0 : 180,
                        piece
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
                TileComponent tile = new TileComponent(new Point(x, y), (x + y) % 2);

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
    }

    private void placePiece(Entity tile, int ownerId, int direction, Square2pPieces pieceType) {
        PieceIdentifier pieceIdentifier = new PieceIdentifier(
                pieceType.getPieceType(),
                pieceType.getPieceDefinition().shortName(),
                ownerId,
                direction
        );

        PieceComponent piece = new PieceComponent(this, pieceIdentifier, pieceType.getPieceDefinition().baseMoves());
        piece.addSpecialMoves(pieceType.getPieceDefinition().specialRules());
        tile.piece = piece;
    }

}
