package jchess.gamemode.hex2p;

import dx.schema.types.PieceType;
import dx.schema.types.Vector2I;
import jchess.common.BaseChessGame;
import jchess.common.components.PieceComponent;
import jchess.common.components.PieceIdentifier;
import jchess.common.components.TileComponent;
import jchess.ecs.Entity;
import jchess.gamemode.IPieceLayoutProvider;
import jchess.gamemode.PieceStore;
import jchess.gamemode.PieceStore.IPieceDefinitionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class Hex2PlayerGame extends BaseChessGame {

    private static final Logger logger = LoggerFactory.getLogger(Hex2PlayerGame.class);

    private static final int numTilesHorizontal = 6 + 5;

    private static final int numTilesVertical = 21;

    private final Entity[][] tiles = new Entity[numTilesVertical][numTilesHorizontal];

    public Hex2PlayerGame(PieceStore pieceStore, IPieceLayoutProvider layoutProvider) {
        super(2, pieceStore, layoutProvider);
    }

    @Override
    public dx.schema.types.Entity applyPerspective(dx.schema.types.Entity tile, int playerIndex) {
        if (playerIndex < 0 || playerIndex > 1) {
            throw new IllegalArgumentException("playerIndex must be 0 or 1, but was " + playerIndex);
        }
        if (playerIndex == 0) return tile;
        if (tile == null || tile.getTile() == null || tile.getTile().getDisplayPos() == null) return tile;

        final double strideX = 1.73205;
        final double strideY = 3;
        final double cosine = -0.5;
        final double sine = 0.8660254;

        Vector2I displayPos = tile.getTile().getDisplayPos();
        double scaledX = (displayPos.getX() - 5) * strideX;
        double scaledY = (displayPos.getY() - 10) * strideY;
        double rotatedX = (scaledX * cosine) - (scaledY * sine);
        double rotatedY = (scaledX * sine) + (scaledY * cosine);
        displayPos.setX((int) Math.round(rotatedX / strideX) + 5);
        displayPos.setY((int) Math.round(rotatedY / strideY) + 10);
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
        IPieceDefinitionProvider pieceProvider = pieceStore.getPiece(pieceType);
        if (pieceProvider == null) {
            logger.error("unable to place piece with pieceType '" + pieceType + "'. PieceType does not exist.");
            return;
        }

        int direction = ((ownerId - 2) * (-120)) % 360;
        placePiece(targetTile, ownerId, direction, pieceProvider);
    }

    // Helper method to create TileComponent and set neighbors
    private void createTileAndSetNeighbors(Entity entity, int x, int y) {
        TileComponent tile = new TileComponent(new Point(x, y), y % 3);
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

        entity.tile = tile;
    }

    @Override
    protected void generateBoard() {

        for (int y = 0; y < numTilesVertical; y++) {
            Entity[] tileRow = tiles[y];
            int x_start, x_end;

            if (y < 5) { // top part
                x_start = 5 - y;
                x_end = x_start + 2 * (y + 1);
            } else if (y < 16) { // middle part
                x_start = (y % 2 == 0) ? 1 : 0;
                x_end = numTilesHorizontal;
            } else { // bottom part
                int tmp_y = y - 15;
                x_start = tmp_y;
                x_end = tmp_y + (6 - tmp_y) * 2;
            }

            for (int x = x_start; x < x_end; x += 2) {
                tileRow[x] = entityManager.createEntity();
                createTileAndSetNeighbors(tileRow[x], x, y);
            }
        }
    }

    private void placePiece(Entity tile, int ownerId, int direction, IPieceDefinitionProvider pieceProvider) {
        PieceStore.PieceDefinition pieceDefinition = pieceProvider.getPieceDefinition();
        PieceIdentifier pieceIdentifier = new PieceIdentifier(
                pieceProvider.getPieceType(),
                pieceDefinition.shortName(),
                ownerId,
                direction
        );

        PieceComponent pieceComp = new PieceComponent(this, pieceIdentifier, pieceDefinition.baseMoves());
        pieceComp.addSpecialMoves(pieceDefinition.specialRules());
        tile.piece = pieceComp;
    }

}
